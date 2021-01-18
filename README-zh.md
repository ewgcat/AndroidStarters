### AndroidStarters



#### 简介

`AndroidStarters` 是一个基于图结构，支持同异步依赖任务初始化 Android 启动框架。其锚点提供 "勾住" 依赖的功能，能灵活解决初始化过程中复杂的同步问题。参考 `alpha` 并改进其部分细节, 更贴合 Android 启动的场景, 同时支持优化依赖初始化流程, 选择较优的路径进行初始化。


较 `alpha` 的优势

* 支持配置 AndroidStarters 等待任务链，常用于 application#onCreate 前保证某些初始化任务完成之后再进入 activity 生命周期回调。
* 支持主动请求阻塞等待任务，常用于任务链上的某些初始化任务需要用户逻辑确认。
* 支持同异步任务链

#### 使用需知

> 1. AndroidStarters的设计是为了 app 启动时候做复杂的初始化工作能高效便捷完成，而不是用于业务逻辑中用于初始化某些依赖。
> 2. api 中设置 Starter 会阻塞等待直到 Starter 完成之后才继续走 AndroidStartersManager#start 后的代码块，application 中 之所以能这么处理是因为没有频繁的 ui 操作。 Starter 之后的任务会在自主由框架调度，同步任务会通过 handler#post 发送到主线程排队处理，异步任务会通过框架内线程池驱动。
> 3. 等待功能在不设置 Starter 的场景下使用。如果设置了 Starter ，则等待任务应该是后置于 Starter 避免 uiThead 阻塞。
> 4. 同异步混合链及 Starter 功能的结合使用，可以灵活处理很多复杂初始化场景，但是要充分理解使用功能时的线程背景。

#### 使用方法
1. 在项目根目路添加 jcenter 仓库

	```
	buildscript {
	    repositories {
	        jcenter()  
	    }
	}
	allprojects {
	    repositories {
	        jcenter()
	    }   
	}
	```

2. 在 **app** 模块下添加依赖

	```
	implementation 'com.lishuaihua.android:AndroidStarters:1.1.3'
	```

3. 添加依赖图并启动

 `Datas` 类来实现 kotlin 逻辑。以 "图" 的形式接收启动依赖集，图的构建节点的链接来实现。
   
    
    构建一个 Project，Project 是 Task 子类，用于描述多 Task 场景，由于使用 <TaskName> 构建，传递一个工厂统一处理
    以下构建  task1 <- task2 <- task3 <- task4 逻辑， A -> B 表示 A 依赖 B
  
    ==> koltin 也支持上述所有流程，同时也提供了 dsl 的构建形式构建依赖图，代码可参考 Datas 类
    通过调用 graphics 方法来描述一张依赖图，使用 <TaskName> 构建，传递一个工厂统一处理
    AndroidStartersManager.getInstance()
        .debuggable { true }
        .taskFactory { TestTaskFactory() }     //根据id生成task的工厂
        .AndroidStarters { arrayOf(TASK_13, TASK_10) } //Starter 对应的 task id
        .block("TASK_10000") {			       // block 场景的 task id 及 处理监听的 lambda
            //根据业务进行  it.smash() or it.unlock()
        }
        .graphics {							      // 构建依赖图
            UITHREAD_TASK_A.sons(
                    TASK_10.sons(
                            TASK_11.sons(
                                    TASK_12.sons(
                                            TASK_13))),
                    UITHREAD_TASK_B.alsoParents(TASK_12),
            )
            arrayOf(UITHREAD_TASK_A)
        }
        .startUp()
    其中 StarterYouNeed 为你所需要添加的锚点, waitTaskYouNeed 为你所需要等待的任务,dependencyGraphHead 为依赖图的头部。
    ```

#### app

代码逻辑请参考 **app** 模块下的 app。

下面针对 demo 中涉及的主要场景做下阐述。

* 多进程初始化

	**StarterDemoApplication.class** 中针对多进程进行实践，满足绝大部分初始化场景。```StarterDemoApplication#onCreate```
	会在涉及新进程业务启动时被再次调用，所以不同进程的初始化场景可根据进程名称进行特定定制。
	代码可参考 ```StarterDemoApplication#initDependenciesCompatMultiProcess```  . 
	触发拉起新进程可参考 ```MainActivity#testPrivateProcess```  或者  ```MainActivity#testPublicProcess``` 。

* 某初始化链中间节点需要等待响应

	某些非常苛刻的初始化链可能需要等待某些条件。（注意：这里的响应应该是 UI 线程的响应，如果是异步响应，则可以作为一个节点提前主动初始化了。）比如某些 app 初始化的时候需要用户选择 ”兴趣场景“ 进而初始化后续页面的所有逻辑等。代码可参考 ```MainActivity#testUserChoose```

* 某初始化链完成之后可能会再启动另一条新链

	这类功能也支持，但是实际上框架更提倡在 application 中统一管理所有初始化链。因为框架强调的是 **任意初始化任务应该是属于业务重量级初始化代码或者第三方SDK初始化** 。
	代码可参考 ```MainActivity#testRestartNewDependenciesLink``` 。


#### Debug 信息

**debuggale** 模式下能打印不同维度的 log 作为调试信息输出，同时针对每个依赖任务做 `Trace` 追踪, 可以通过 *python systrace.py* 来输出 **trace.html** 进行性能分析。

`AndroidStarters` 定义不同的 **TAG** 用于过滤 log, 需要打开 Debug 模式。

* `AndroidStarters`, 最基础的 TAG
* `TASK_DETAIL`, 过滤依赖任务的详情

	```
	2021-01-18 14:19:45.687 22493-22493/com.lishuaihua.starter.demo D/TASK_DETAIL: TASK_DETAIL
	======================= task (UITHREAD_TASK_A ) =======================
	| 依赖任务 :
	| 是否是锚点任务 : false
	| 线程信息 : main
	| 开始时刻 : 1552889985401 ms
	| 等待运行耗时 : 85 ms
	| 运行任务耗时 : 200 ms
	| 结束时刻 : 1552889985686
	==============================================
	```
* `Starter_DETAIL`, 过滤输出锚点任务信息

	```
	2021-01-18 14:42:33.354 24719-24719/com.lishuaihua.starter.demo W/Starter_DETAIL: Starter "TASK_100" no found !
	2021-01-18 14:42:33.354 24719-24719/com.lishuaihua.starter.demo W/Starter_DETAIL: Starter "TASK_E" no found !
	2021-01-18 14:42:33.355 24719-24719/com.lishuaihua.starter.demo D/Starter_DETAIL: has some AndroidStarters！( "TASK_13" )
	2021-01-18 14:42:34.188 24719-24746/com.lishuaihua.starter.demo D/Starter_DETAIL: TASK_DETAIL
    ======================= task (TASK_13 ) =======================
    | 依赖任务 : TASK_12
    | 是否是锚点任务 : true
    | 线程信息 : AndroidStarters Thread #7
    | 开始时刻 : 1552891353984 ms
    | 等待运行耗时 : 4 ms
    | 运行任务耗时 : 200 ms
    | 结束时刻 : 1552891354188
    ==============================================
	2021-01-18 14:42:34.194 24719-24719/com.lishuaihua.starter.demo D/Starter_DETAIL: All AndroidStarters were released！
	```
	
* `LOCK_DETAIL`, 过滤输出等待信息
* `DEPENDENCE_DETAIL`, 过滤依赖图信息


#### 效果对比

下面是没有使用锚点和使用锚点场景下, **Trace** 给出的执行时间

依赖图中有着一条 `UITHREAD_TASK_A -> TASK_10 -> TASK_91 -> Task_13`依赖。假设我们的这条依赖路径是后续业务的前置条件,则我们需要等待该业务完成之后再进行自身的业务代码。如果不是则我们不关系他们的结束时机。在使用锚点功能时，我们勾住 `TASK_13`，则从始端到该锚点的优先级将被提升。从上图可以看到执行该依赖链的时间缩短了。

> 依赖图用于解决任务执行时任务间的依赖关系，而锚点设置则是用于解决执行依赖与代码调用点之间的同步关系。



package com.lishuaihua.android.starter

import androidx.annotation.MainThread
import com.lishuaihua.android.starter.log.Logger.d
import com.lishuaihua.android.starter.util.Utils.assertMainThread
import com.lishuaihua.android.starter.util.Utils.insertAfterTask
import com.lishuaihua.android.starter.log.Logger
import com.lishuaihua.android.starter.task.lock.LockableTask
import com.lishuaihua.android.starter.task.project.Project
import com.lishuaihua.android.starter.task.Task
import com.lishuaihua.android.starter.task.lock.LockableStarter
import java.util.*
import java.util.concurrent.ExecutorService
import kotlin.collections.HashMap

/**
 * 非单例，支持扩展到任何场景
 */
class StartersManager {

    var debuggable = false
    private var StarterTaskIds: MutableSet<String> = HashSet()
    private var blockStarters = HashMap<String, LockableStarter?>()
    private var currentBlockStarter: LockableStarter? = null
    private val StartersRuntime: StartersRuntime

    private constructor(executor: ExecutorService? = null) {
        this.StartersRuntime = StartersRuntime(executor)
    }

    companion object {
        @JvmStatic
        @JvmOverloads
        fun getInstance(executor: ExecutorService? = null): StartersManager {
            return StartersManager(executor)
        }
    }

    fun getLockableStarters(): Map<String, LockableStarter?> {
        return blockStarters
    }

    fun getStartersRuntime(): StartersRuntime {
        return StartersRuntime
    }

    fun debuggable(debuggable: Boolean): StartersManager {
        this.debuggable = debuggable
        return this
    }

    /**
     * 扩展支持 https://github.com/YummyLau/Starters/issues/7   暂停机制
     * 调用前须知：
     * 1. 请充分理解 Starter 的作用并明白，为何 application sleep 频繁等待代码块执行的原因
     * 2. 如果调用 requestBlockWhenFinish 则意味着任务链在 task 执行完毕之后会进入等待阶段，如果此时等待的 task 在[初始节点，Starters]链中则可能导致界面卡主
     * 3. 在调用 requestBlockWhenFinish 设置等待任务的前提下务必保证 Starters 已经解锁 或者 任务链上没有 Starters。
     * @param task block目标task
     * @return
     */
    fun requestBlockWhenFinish(task: Task): LockableStarter {
        val lockableStarter = LockableStarter(StartersRuntime.handler)
        val lockableTask = LockableTask(task, lockableStarter)
        insertAfterTask(lockableTask, task)
        blockStarters[task.id] = lockableStarter
        lockableStarter.addReleaseListener(object : LockableStarter.ReleaseListener {
            override fun release() {
                blockStarters[task.id] = null
            }
        })
        return lockableStarter
    }

    //用于兼容旧版本java
    fun addStarters(vararg taskIds: String): StartersManager {
        if (taskIds.isNotEmpty()) {
            for (id in taskIds) {
                if (id.isNotEmpty()) {
                    StarterTaskIds.add(id)
                }
            }
        }
        return this
    }

    fun addStarter(vararg taskIds: String): StartersManager {
        if (taskIds.isNotEmpty()) {
            for (id in taskIds) {
                if (id.isNotEmpty()) {
                    StarterTaskIds.add(id)
                }
            }
        }
        return this
    }

    @MainThread
    fun start(task: Task?) {
        assertMainThread()
        if (task == null) {
            throw RuntimeException("can no run a task that was null !")
        }
        syncConfigInfoToRuntime()
        var startTask = task
        if (startTask is Project) {
            startTask = (task as Project).startTask
        }
        StartersRuntime.traversalDependenciesAndInit(startTask)
        val logEnd = logStartWithStartersInfo()
        startTask.start()
        while (StartersRuntime.hasStarterTasks()) {
            try {
                Thread.sleep(10)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            while (StartersRuntime.hasRunTasks()) {
                StartersRuntime.tryRunBlockRunnable()
            }
        }
        if (logEnd) {
            logEndWithStartersInfo()
        }
    }

    private fun syncConfigInfoToRuntime() {
        StartersRuntime.clear()
        StartersRuntime.debuggable = debuggable;
        StartersRuntime.addStarterTasks(StarterTaskIds)
        debuggable = false
        StarterTaskIds.clear()
    }


    private fun logStartWithStartersInfo(): Boolean {
        if (!debuggable) {
            return false
        }
        val stringStartersManagerBuilder = StringBuilder()
        val hasStarterTask = StartersRuntime.hasStarterTasks()
        if (hasStarterTask) {
            stringStartersManagerBuilder.append(Constants.HAS_Starter)
            stringStartersManagerBuilder.append("( ")
            for (taskId in StartersRuntime.StarterTaskIds) {
                stringStartersManagerBuilder.append("\"$taskId\" ")
            }
            stringStartersManagerBuilder.append(")")
        } else {
            stringStartersManagerBuilder.append(Constants.NO_Starter)
        }
        if (debuggable) {
            d(Constants.StarterS_INFO_TAG, stringStartersManagerBuilder.toString())
        }
        return hasStarterTask
    }

    private fun logEndWithStartersInfo() {
        if (!debuggable) {
            return
        }
        if (debuggable) {
            d(Constants.StarterS_INFO_TAG, Constants.Starter_RELEASE)
        }
    }
}


internal object StartersManagerBuilder {
    var debuggable = false
    var Starters: MutableList<String> = mutableListOf()
    var factory: Project.TaskFactory? = null
    var blocks = mutableMapOf<String, ((lockableStarter: LockableStarter) -> Unit)>()
    val allTask: MutableSet<Task> = mutableSetOf();
    var sons: Array<String>? = null

    fun setUp() {
        debuggable = false
        Starters.clear()
        factory = null
        blocks.clear()
        sons = null
        allTask.clear()
    }

    fun makeTask(taskId: String): Task? = factory?.getTask(taskId)
}


fun StartersManager.debuggable(init: () -> Boolean): StartersManager {
    StartersManagerBuilder.debuggable = init.invoke()
    return this
}

fun StartersManager.Starters(init: () -> Array<String>): StartersManager {
    val StarterList = init.invoke()
    if (StarterList.isNotEmpty()) {
        for (taskId in StarterList) {
            if (taskId.isNotEmpty()) {
                StartersManagerBuilder.Starters.add(taskId)
            }
        }
    }
    return this
}

fun StartersManager.taskFactory(init: () -> Project.TaskFactory): StartersManager {
    StartersManagerBuilder.factory = init.invoke()
    return this
}

fun StartersManager.block(block: String, listener: (lockableStarter: LockableStarter) -> Unit): StartersManager {
    StartersManagerBuilder.blocks[block] = listener
    return this
}

fun StartersManager.graphics(graphics: () -> Array<String>): StartersManager {
    StartersManagerBuilder.sons = graphics.invoke();
    return this
}


fun StartersManager.startUp(): StartersManager {

    debuggable = StartersManagerBuilder.debuggable

    if (StartersManagerBuilder.Starters.isNotEmpty()) {
        for (taskId in StartersManagerBuilder.Starters) {
            addStarter(taskId)
        }
    }

    requireNotNull(StartersManagerBuilder.factory) { "kotlin dsl-build should set TaskFactory with invoking StartersManager#taskFactory()" }

    requireNotNull(StartersManagerBuilder.sons) { "kotlin dsl-build should set graphics with invoking StartersManager#graphics()" }

    val sons = StartersManagerBuilder.sons

    if (sons.isNullOrEmpty()) {
        Logger.w("No task is run ！")
        return this
    }

    val setUp = object : Task("inner_start_up_task") {
        override fun run(name: String) {
            Logger.d("task(inner_start_up_task) start !")
        }
    }

    val validSon = mutableListOf<Task>()

    if (sons.isNotEmpty()) {
        for (taskId in sons) {
            if (taskId.isNotEmpty()) {
                val son = StartersManagerBuilder.makeTask(taskId)
                if (son == null) {
                    Logger.w("can find task's id = $taskId in factory,skip this son")
                    continue
                }
                validSon.add(son);
            }
        }
    }

    if (validSon.isEmpty()) {
        Logger.w("No task is run ！")
        return this
    }

    if (!StartersManagerBuilder.blocks.isNullOrEmpty()) {
        StartersManagerBuilder.blocks.forEach {
            val blockTask = StartersManagerBuilder.makeTask(it.key)
            if (blockTask == null) {
                Logger.w("can find task's id = ${it.key} in factory")
            } else {
                val lock = requestBlockWhenFinish(blockTask)
                lock.setLockListener(object : LockableStarter.LockListener {
                    override fun lockUp() {
                        it.value.invoke(lock)
                    }
                })
            }
        }
    }

    if (validSon.size == 1) {
        start(validSon[0])
    } else {
        for (task in validSon) {
            setUp.behind(task)
        }
        start(setUp)
    }
    StartersManagerBuilder.setUp()
    return this
}

fun String.sons(vararg taskIds: String): String {
    val curTask = StartersManagerBuilder.makeTask(this)
    if (curTask == null) {
        Logger.w("can find task's id = $this in factory,skip it's all sons")
        return this
    }
    if (!StartersManagerBuilder.allTask.contains(curTask)) {
        StartersManagerBuilder.allTask.add(curTask)
    }
    if (taskIds.isNotEmpty()) {
        for (taskId in taskIds) {
            if (taskId.isNotEmpty()) {
                val task = StartersManagerBuilder.makeTask(taskId)
                if (task == null) {
                    Logger.w("can find task's id = $taskId in factory,skip this son")
                    continue
                }
                if (!StartersManagerBuilder.allTask.contains(task)) {
                    StartersManagerBuilder.allTask.add(task)
                }
                task.dependOn(curTask)
            }
        }
    }
    return this;
}

fun String.alsoParents(vararg taskIds: String): String {
    val curTask = StartersManagerBuilder.makeTask(this)
    if (curTask == null) {
        Logger.w("can find task's id = $this in factory,skip it's all sons")
        return this
    }
    if (!StartersManagerBuilder.allTask.contains(curTask)) {
        StartersManagerBuilder.allTask.add(curTask)
    }
    if (taskIds.isNotEmpty()) {
        for (taskId in taskIds) {
            if (taskId.isNotEmpty()) {
                val task = StartersManagerBuilder.makeTask(taskId)
                if (task == null) {
                    Logger.w("can find task's id = $taskId in factory,skip this son")
                    continue
                }
                if (!StartersManagerBuilder.allTask.contains(task)) {
                    StartersManagerBuilder.allTask.add(task)
                }
                task.behind(curTask)
            }
        }
    }
    return this
}



package com.lishuaihua.starter.demo.data

import com.lishuaihua.android.starter.*
import com.lishuaihua.android.starter.task.lock.LockableStarter
import com.lishuaihua.starter.demo.BuildConfig

class Datas {
    /**
     * 可通过DEPENDENCE_DETAIL 查看到有一下任务链
     *
     * 校验log：当且仅当Starter执行完毕，解除阻塞
     */
    fun startFromApplicationOnMainProcessByDsl() {
        StartersManager.getInstance()
                .debuggable { BuildConfig.DEBUG }
                .taskFactory { TestTaskFactory() }
                .Starters { arrayOf(TASK_13, "TASK_E", TASK_10) }
                .block("TASK_10000") {

                    //根据业务进行  it.smash() or it.unlock()
                }
                .graphics {
                    UITHREAD_TASK_A.sons(
                            TASK_10.sons(
                                    TASK_11.sons(
                                            TASK_12.sons(
                                                    TASK_13))),
                            UITHREAD_TASK_B.alsoParents(TASK_12)
                    )
                    arrayOf(UITHREAD_TASK_A)
                }
                .startUp()
    }

    fun startFromApplicationOnPrivateProcess() {
        StartersManager.getInstance()
                .debuggable { BuildConfig.DEBUG }
                .taskFactory { TestTaskFactory() }
                .Starters { arrayOf(TASK_12) }
                .graphics {
                    UITHREAD_TASK_A.sons(
                            TASK_10.sons(
                                    TASK_11,
                                    TASK_12.sons(TASK_13)),
                            UITHREAD_TASK_B)
                    arrayOf(UITHREAD_TASK_A)
                }
                .startUp()
    }

    fun startFromApplicationOnPublicProcess() {
        StartersManager.getInstance()
                .debuggable { BuildConfig.DEBUG }
                .taskFactory { TestTaskFactory() }
                .Starters { arrayOf(TASK_13) }
                .graphics {
                    UITHREAD_TASK_A.sons(
                            TASK_10.sons(
                                    TASK_11,
                                    TASK_12.sons(TASK_13)),
                            UITHREAD_TASK_B)
                    arrayOf(UITHREAD_TASK_A)
                }
                .startUp()
    }

    fun startForTestLockableStarterByDsl(listener: (lockableStarter: LockableStarter) -> Unit): LockableStarter? {
        val manager = StartersManager.getInstance()
                .debuggable { BuildConfig.DEBUG }
                .taskFactory { TestTaskFactory() }
                .block(TASK_10) {
                    listener.invoke(it)
                }
                .graphics {
                    arrayOf(TASK_10.sons(TASK_11.sons(TASK_12.sons(TASK_13))))
                }
                .startUp()
        return manager.getLockableStarters()[TASK_10]
    }

    fun startForLinkOneByDsl(runnable: Runnable) {
        val factory = TestTaskFactory();
        val end = factory.getTask(TASK_13);
        end.addTaskListener {
            onRelease {
                runnable.run()
            }
        }
        val manager = StartersManager.getInstance()
                .debuggable { BuildConfig.DEBUG }
                .taskFactory { factory }
                .graphics {
                    arrayOf(UITHREAD_TASK_A.sons(TASK_10.sons(TASK_11.sons(TASK_12.sons(TASK_13)))))
                }
                .startUp()
    }

    fun startForLinkTwoByDsl() {
        val manager = StartersManager.getInstance()
                .debuggable { true }
                .taskFactory { TestTaskFactory() }
                .graphics {
                    arrayOf(UITHREAD_TASK_A.sons(TASK_10.sons(TASK_11.sons(TASK_12.sons(TASK_13)))))
                }
                .startUp()
    }

    companion object {
        const val PROJECT_1 = "PROJECT_1"
        const val TASK_10 = "TASK_10"
        const val TASK_11 = "TASK_11"
        const val TASK_12 = "TASK_12"
        const val TASK_13 = "TASK_13"
        const val UITHREAD_TASK_A = "UITHREAD_TASK_A"
        const val UITHREAD_TASK_B = "UITHREAD_TASK_B"
        const val ASYNC_TASK_1 = "ASYNC_TASK_1"
        const val ASYNC_TASK_2 = "ASYNC_TASK_2"
    }
}
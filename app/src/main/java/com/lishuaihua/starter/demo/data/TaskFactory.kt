package com.lishuaihua.starter.demo.data

import com.lishuaihua.android.starter.task.project.Project.TaskFactory
import com.lishuaihua.android.starter.task.Task
import com.lishuaihua.android.starter.task.TaskCreator
import java.util.*

abstract class TestTask(
    id: String,
    isAsyncTask: Boolean = false //是否是异步存在
) : Task(id, isAsyncTask) {

    fun doJob(millis: Long) {
        val nowTime = System.currentTimeMillis()
        while (System.currentTimeMillis() < nowTime + millis) {
            //程序阻塞指定时间
            val min = 10
            val max = 99
            val random = Random()
            val num = random.nextInt(max) % (max - min + 1) + min
        }
    }
}

class TASK_10 : TestTask(Datas.TASK_10, true) {
    override fun run(name: String) {
        doJob(1000)
    }
}

class TASK_11 : TestTask(Datas.TASK_11, true) {
    override fun run(name: String) {
        doJob(200)
    }
}

class TASK_12 : TestTask(Datas.TASK_12, true) {
    override fun run(name: String) {
        doJob(200)
    }
}

class TASK_13 : TestTask(Datas.TASK_13, false) {
    override fun run(name: String) {
        doJob(200)
    }
}


class UITHREAD_TASK_A : TestTask(Datas.UITHREAD_TASK_A) {
    override fun run(name: String) {
        doJob(200)
    }
}

class UITHREAD_TASK_B : TestTask(Datas.UITHREAD_TASK_B) {
    override fun run(name: String) {
        doJob(200)
    }
}


class ASYNC_TASK_1 : TestTask(Datas.ASYNC_TASK_1, true) {
    override fun run(name: String) {
        doJob(200)
    }
}

class ASYNC_TASK_2 : TestTask(Datas.ASYNC_TASK_2, true) {
    override fun run(name: String) {
        doJob(200)
    }
}



object TestTaskCreator : TaskCreator {
    override fun createTask(taskName: String): Task {
        when (taskName) {
            Datas.TASK_10 -> {
                val task_10 = TASK_10()
                task_10.priority = 10
                return task_10
            }
            Datas.TASK_11 -> {
                val TASK_11 = TASK_11()
                TASK_11.priority = 10
                return TASK_11
            }
            Datas.TASK_12 -> {
                val TASK_12 = TASK_12()
                TASK_12.priority = 10
                return TASK_12
            }
            Datas.TASK_13 -> {
                val TASK_13 = TASK_13()
                TASK_13.priority = 10
                return TASK_13
            }
            Datas.UITHREAD_TASK_A -> {
                return UITHREAD_TASK_A()
            }
            Datas.UITHREAD_TASK_B -> {
                return UITHREAD_TASK_B()
            }

            Datas.ASYNC_TASK_1 -> {
                return ASYNC_TASK_1()
            }
            Datas.ASYNC_TASK_2 -> {
                return ASYNC_TASK_2()
            }

        }
        return ASYNC_TASK_2()
    }
}

class TestTaskFactory : TaskFactory(TestTaskCreator)
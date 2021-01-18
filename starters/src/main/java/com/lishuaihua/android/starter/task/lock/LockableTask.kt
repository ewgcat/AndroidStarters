package com.lishuaihua.android.starter.task.lock

import com.lishuaihua.android.starter.task.Task

internal class LockableTask(wait: Task, lockableStarter: LockableStarter) : Task(wait.id + "_waiter", true) {
    private val lockableStarter: LockableStarter
    override fun run(name: String) {
        lockableStarter.lock()
    }

    fun successToUnlock(): Boolean {
        return lockableStarter.successToUnlock()
    }

    init {
        lockableStarter.setTargetTaskId(wait.id)
        this.lockableStarter = lockableStarter
    }
}
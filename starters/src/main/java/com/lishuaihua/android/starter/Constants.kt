package com.lishuaihua.android.starter

internal object Constants {
    // log tag
    const val TAG = "Starters"
    const val TASK_DETAIL_INFO_TAG = "TASK_DETAIL"
    const val StarterS_INFO_TAG = "Starter_DETAIL"
    const val DEPENDENCE_TAG = "DEPENDENCE_DETAIL"
    const val LOCK_TAG = "LOCK_DETAIL"
    //StarterS_INFO_TAG
    const val NO_Starter = "has no any Starter！"
    const val HAS_Starter = "has some Starters！"
    const val Starter_RELEASE = "All Starters were released！"
    //TASK_DETAIL_INFO_TAG
    const val START_METHOD = " -- onStart -- "
    const val RUNNING_METHOD = " -- onRunning -- "
    const val FINISH_METHOD = " -- onFinish -- "
    const val RELEASE_METHOD = " -- onRelease -- "
    const val LINE_STRING_FORMAT = "| %s : %s "
    const val MS_UNIT = "ms"
    const val HALF_LINE_STRING = "======================="
    const val DEPENDENCIES = "依赖任务"
    const val THREAD_INFO = "线程信息"
    const val START_TIME = "开始时刻"
    const val START_UNTIL_RUNNING = "等待运行耗时"
    const val RUNNING_CONSUME = "运行任务耗时"
    const val FINISH_TIME = "结束时刻"
    const val IS_Starter = "是否是锚点任务"
    const val WRAPPED = "\n"
}
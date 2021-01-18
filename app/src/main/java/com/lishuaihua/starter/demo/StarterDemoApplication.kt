package com.lishuaihua.starter.demo

import android.app.Application
import android.app.Dialog
import android.app.Service
import android.os.StrictMode
import android.util.Log
import android.view.View
import com.lishuaihua.starter.demo.data.Datas
import com.lishuaihua.starter.demo.util.ProcessUtils
import leakcanary.AppWatcher
import leakcanary.LeakCanary


class StarterDemoApplication : Application() {
    //用于存放视图
    val viewMap = ArrayList<View>()
    override fun onCreate() {
        super.onCreate()


        Log.d(TAG, "StarterDemoApplication#onCreate process Id is " + ProcessUtils.processId)
        Log.d(TAG, "StarterDemoApplication#onCreate process Name is " + ProcessUtils.processName)
        Log.d(TAG, "StarterDemoApplication#onCreate - start")
        initDependenciesCompatMultiProcess()
        Log.d(TAG, "StarterDemoApplication#onCreate - end")
    }

    private fun initDependenciesCompatMultiProcess() {
        val processName = ProcessUtils.processName ?: return

        //主进程 com.lishuaihua.starter.demo
        when {
            processName == packageName -> {
                Log.d(TAG, "StarterDemoApplication#initDependenciesCompatMutilProcess - startFromApplicationOnMainProcess")
                Datas().startFromApplicationOnMainProcessByDsl()

                //私有进程 com.lishuaihua.starter.demo:remote
            }
            processName.startsWith(packageName) -> {
                Log.d(TAG, "StarterDemoApplication#initDependenciesCompatMutilProcess - startFromApplicationOnPrivateProcess")
                Datas().startFromApplicationOnPrivateProcess()

                //公有进程 .public
            }
            else -> {
                Log.d(TAG, "StarterDemoApplication#initDependenciesCompatMutilProcess - startFromApplicationOnPublicProcess")
                Datas().startFromApplicationOnPublicProcess()
            }
        }
    }

    companion object {
        private val TAG: String = "StarterDemoApplication"
    }
}
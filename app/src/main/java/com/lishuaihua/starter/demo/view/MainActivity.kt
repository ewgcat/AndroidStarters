package com.lishuaihua.starter.demo.view;

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.lishuaihua.starter.demo.R
import com.lishuaihua.starter.demo.data.Datas
import com.lishuaihua.starter.demo.util.ProcessUtils

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "MainActivity#onCreate process Id is " + ProcessUtils.processId)
        Log.d(TAG, "MainActivity#onCreate process Name is " + ProcessUtils.processName)
        findViewById<View>(R.id.test_leak_canary).setOnClickListener {
            val intent = Intent(MainActivity@this, SecondActivity::class.java)
            startActivity(intent)
        }
        //测试用户选择
        testUserChoose()
        //测试重启新链接
        testRestartNewDependenciesLink()
    }


    private fun testUserChoose() {
        findViewById<View>(R.id.test_user_Starter).setOnClickListener {
            Log.d("MainActivity", "Demo1 - testUserChoose")
            Datas().startForTestLockableStarterByDsl {
                val lockableStarter = it
                CusDialog.Builder(this@MainActivity)
                        .title("任务(" + lockableStarter.lockId + ")已进入等待状态，请求响应")
                        .left("终止任务", View.OnClickListener {
                            lockableStarter.smash()
                        })
                        .right("继续执行", View.OnClickListener {
                            lockableStarter.unlock()
                        }).build().show()
            }
            Log.d("MainActivity", "Demo1 - testUserChoose")
        }
    }

    private fun testRestartNewDependenciesLink() {
        findViewById<View>(R.id.test_restart).setOnClickListener {
            Log.d("MainActivity", "Demo2 - testRestartNewDependenciesLink - startLinkOne")
            Datas().startForLinkOneByDsl(
                    Runnable {
                        Log.d("MainActivity", "Demo2 - testRestartNewDependenciesLink - endLinkOne")
                        Log.d("MainActivity", "Demo2 - testRestartNewDependenciesLink - startLinkTwo")
                        Handler(Looper.getMainLooper()).post {
                            Datas().startForLinkTwoByDsl()
                        }
                        Log.d("MainActivity", "Demo2 - testRestartNewDependenciesLink - endLinkTwo")
                    })
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
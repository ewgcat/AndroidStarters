package com.lishuaihua.starter.demo.view

import android.app.Activity
import android.os.Bundle
import android.view.View
import com.lishuaihua.starter.demo.R
import com.lishuaihua.starter.demo.data.Singleton

class SecondActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
    }
     fun onLeakCanary(view: View?){
        val singleton = Singleton.newInstance(SecondActivity@this)
         finish()
    }
     fun onNoLeakCanary(view: View?){
        val singleton = Singleton.newInstance(applicationContext)
         finish()
     }
}
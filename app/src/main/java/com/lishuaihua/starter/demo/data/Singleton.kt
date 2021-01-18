package com.lishuaihua.starter.demo.data

import android.content.Context

class Singleton private constructor(private val context: Context) {
    companion object {
        private var singleton: Singleton? = null
        fun newInstance(context: Context): Singleton? {
            if (singleton == null) {
                synchronized(Singleton::class.java) {
                    if (singleton == null) { //双重检查锁定
                        singleton = Singleton(context)
                    }
                }
            }
            return singleton
        }
    }
}
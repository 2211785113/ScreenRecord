package com.example.ruru.screenrecord

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.enjoy.screenpush.thread.ScreenLiveRunnable
import com.tbruyelle.rxpermissions3.RxPermissions

@Deprecated("在子线程中启动录屏方法废弃，改用开启服务启动录屏")
class MainThreadAct : AppCompatActivity() {

    private lateinit var mScreenLive: ScreenLiveRunnable
    private val RTMP_URL =
        "rtmp://send3.douyu.com/live/8131799rv4lkHsac?wsSecret=401293db727a2a47cb8c47fd85f40ea5&wsTime=5e4a6502&wsSeek=off&wm=0&tw=0&roirecognition=0"
    private val RTMP_URL1 = "rtmp://192.168.8.126/live/ryrUg3UmL"

    private val rxPermissions = RxPermissions(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_thread)

        initPermission()
    }

    open fun startLive(view: View) {
        mScreenLive = ScreenLiveRunnable()
        mScreenLive.startLive(this, RTMP_URL)
    }

    open fun stopLive(view: View) {
        mScreenLive.stoptLive()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let { data ->
            mScreenLive.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun initPermission() {
        rxPermissions
            .request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
            .subscribe { granted ->
                if (granted) {

                } else {

                }
            }
    }
}

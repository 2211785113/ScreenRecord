package com.example.ruru.screenrecord

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.annotation.RequiresApi
import com.enjoy.screenpush.service.ScreenLiveService
import com.tbruyelle.rxpermissions3.RxPermissions

class MainServiceAct : AppCompatActivity() {

    private val rxPermissions: RxPermissions = RxPermissions(this)
    private var mMediaProjectionManager: MediaProjectionManager? = null
    private var mMediaProjection: MediaProjection? = null
    private val RESULT_CODE = 0x0000003
    private val SERVICE_ACTION = "cc.abto.server"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_service)
        initPermission()
    }

    open fun startLive(view: View) {
        mMediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager?
        startActivityForResult(mMediaProjectionManager?.createScreenCaptureIntent(), RESULT_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_CODE) {
            mMediaProjection = mMediaProjectionManager?.getMediaProjection(resultCode, data!!)
            //开启服务(mnml用的这个)
           /* startService()
            ScreenLiveService.launch(this, mMediaProjection)*/

            //绑定服务(AIDL用的这个)
            bindService(Intent(this, ScreenLiveService::class.java).apply {
                action = SERVICE_ACTION
            }, object : ServiceConnection {
                override fun onServiceDisconnected(name: ComponentName?) {

                }

                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

                }
            }, Context.BIND_AUTO_CREATE)
        }
    }

    open fun stopLive(view: View) {
        unbindService(object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {
            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            }
        })
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
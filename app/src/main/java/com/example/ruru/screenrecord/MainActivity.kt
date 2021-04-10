package com.example.ruru.screenrecord

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.enjoy.screenpush.ScreenLive

/**
 * 注意：
 * 1.build.gradle(app)中加入以下代码：
 * ndk {
 * abiFilters 'armeabi-v7a'
 * }
 * 2.添加网络/存储/录音权限
 */
class MainActivity : AppCompatActivity() {

    private var mPermissions =
        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)

    private lateinit var mScreenLive: ScreenLive

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initPermission()
    }

    open fun startLive(view: View) {
        mScreenLive = ScreenLive()

        val url =
            "rtmp://send3.douyu.com/live/8131799rv4lkHsac?wsSecret=401293db727a2a47cb8c47fd85f40ea5&wsTime=5e4a6502&wsSeek=off&wm=0&tw=0&roirecognition=0"
//        val url = "rtmp://192.168.8.126/live/ryrUg3UmL"

        mScreenLive.startLive(this, url)
    }

    open fun stopLive(view: View) {
        mScreenLive.stoptLive()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let { data ->
            mScreenLive.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun initPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            var permissionList = arrayListOf<String>()
            for (item in mPermissions) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        item
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionList.add(item)
                }
            }

            var requestCode = 100
            if (permissionList.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, mPermissions, requestCode)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (grantResults.isNotEmpty()) {
                for (item in grantResults) {
                    if (item != PackageManager.PERMISSION_GRANTED) {
                        initPermission()
                        break
                    }
                }
            }
        }
    }
}

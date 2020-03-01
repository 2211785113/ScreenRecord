package com.enjoy.screenpush

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import java.util.concurrent.LinkedBlockingQueue

class ScreenLive : Runnable {

    private lateinit var url: String
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var isLiving: Boolean = false
    private val queue = LinkedBlockingQueue<RTMPPackage>()
    private var mediaProjection: MediaProjection? = null

    init {
        System.loadLibrary("native-lib")
    }

    open fun startLive(activity: Activity, url: String) {
        this.url = url
        // 投屏管理器
        this.mediaProjectionManager = activity
                .getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        // 创建截屏请求intent
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        activity.startActivityForResult(captureIntent, 100)
    }

    open fun stoptLive() {
        addPackage(RTMPPackage.EMPTY_PACKAGE)
        isLiving = false
    }

    open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        // 用户授权
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            // 获得截屏器
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
            LiveTaskManager.getInstance().execute(this)
        }
    }

    fun addPackage(rtmpPackage: RTMPPackage) {
        if (!isLiving) {
            return
        }
        queue.add(rtmpPackage)
    }

    override fun run() {
        //连接服务器
        if (!connect(url)) {
            return
        }
        isLiving = true

        val videoCodec = VideoCodec(this)
        mediaProjection?.let {mediaProjection->
            videoCodec.startLive(mediaProjection)
        }

        val audioCodec = AudioCodec(this)
        audioCodec.startLive()

        var isSend = true
        while (isLiving && isSend) {
            var rtmpPackage: RTMPPackage? = null
            try {
                rtmpPackage = queue.take()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            if (null == rtmpPackage) {
                break
            }
            if (rtmpPackage.buffer != null && rtmpPackage.buffer!!.size != 0) {
                isSend = sendData(rtmpPackage.buffer!!, rtmpPackage.buffer!!
                        .size, rtmpPackage
                        .type, rtmpPackage.tms)
            }
        }
        isLiving = false
        videoCodec.stopLive()
        audioCodec.stopLive()
        queue.clear()
        disConnect()
    }

    private external fun connect(url: String): Boolean

    private external fun disConnect()

    private external fun sendData(data: ByteArray, len: Int, type: Int, tms: Long): Boolean
}
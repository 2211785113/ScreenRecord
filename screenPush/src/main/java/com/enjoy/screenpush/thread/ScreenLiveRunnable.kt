package com.enjoy.screenpush.thread

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.enjoy.screenpush.codec.AudioCodec
import com.enjoy.screenpush.data.RTMPPackage
import com.enjoy.screenpush.codec.VideoCodec
import com.enjoy.screenpush.service.ScreenLiveService
import java.util.concurrent.LinkedBlockingQueue

/**
 * 录屏任务放在一个子线程中 或者 放在一个Service中：
 * 录屏-编码-封包-发送
 */
class ScreenLiveRunnable : Runnable {

    private lateinit var url: String
    private lateinit var context: Context
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var isLiving: Boolean = false
    private val queue = LinkedBlockingQueue<RTMPPackage>()
    private var mediaProjection: MediaProjection? = null

    private var REQUEST_CODE = 0x00000002

    init {
        System.loadLibrary("native-lib")
    }

    /**
     * 获取Intent，发起录屏请求，获取到请求结果后开始录屏
     */
    open fun startLive(activity: Activity, url: String) {
        this.context = activity
        this.url = url
        // 投屏管理器
        this.mediaProjectionManager =
            activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        // 创建截屏请求intent
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        activity.startActivityForResult(captureIntent, REQUEST_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        // 用户授权
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            /* //在子线程中开启一个服务？？？
             var intent = Intent(context,ScreenLiveService::class.java)
             intent.putExtra("code", resultCode)
             intent.putExtra("data", data)
             context.startForegroundService(intent)*/
            //此处：报错异常：要求放在一个前台服务中 那就不用子线程这种方式啦
            // 获得截屏器
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
            LiveTaskManager.getInstance().execute(this)//线程池任务管理类。负责初始化线程任务，提交该录屏任务，传this。
        }
    }

    open fun stoptLive() {
        addPackage(RTMPPackage.EMPTY_PACKAGE)
        isLiving = false
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
        mediaProjection?.let { mediaProjection ->
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
                isSend = sendData(
                    rtmpPackage.buffer!!, rtmpPackage.buffer!!
                        .size, rtmpPackage
                        .type, rtmpPackage.tms
                )
            }
        }
        isLiving = false
        videoCodec.stopLive()
        audioCodec.stopLive()
        queue.clear()
        disConnect()
    }

    /**
     * 用于声明某个方法不由 kotlin 实现（与 java 的 native 类似）
     */
    private external fun connect(url: String): Boolean

    private external fun disConnect()

    private external fun sendData(data: ByteArray, len: Int, type: Int, tms: Long): Boolean
}
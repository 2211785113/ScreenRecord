package com.enjoy.screenpush.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat.startForegroundService
import com.enjoy.screenpush.IMyAidlInterface
import com.enjoy.screenpush.R
import java.util.*

/**
 * Created by lyr on 2021/4/10 & content is 录屏服务
 */
class ScreenLiveService : Service() {

    private var mResultCode = -1
    private var mResultData: Intent? = null
    private var mMediaProjection: MediaProjection? = null
    private var isStarted = false

    //此处思考把mediaProject放到一个Capture类里
    /*constructor(mediaProjection: MediaProjection?) {
        this.mMediaProjection = mediaProjection
    }*/

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startRecording()
//        createNotificationChannel()
        mResultCode = intent?.getIntExtra("code", -1) ?: -1
        mResultData = intent?.getParcelableExtra("data")

//        mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData)

        return super.onStartCommand(intent, flags, startId)
    }

    private fun startRecording() {
        if (isStarted) return
        isStarted = true
    }

    /**
     * 第一步：onBind绑定
     */
    override fun onBind(intent: Intent?): IBinder? {
        return ServiceLiveBinder()
    }

    /**
     * 第二步：
     * 创建一个内部类，继承 AIDL 里的 Stub 类，并实现接口方法，在onBind 返回内部类的实例
     */
    class ServiceLiveBinder : IMyAidlInterface.Stub() {
        override fun basicTypes(
            anInt: Int,
            aLong: Long,
            aBoolean: Boolean,
            aFloat: Float,
            aDouble: Double,
            aString: String?
        ) {

        }

        override fun getName(): String {
            return "test"
        }
    }

    companion object {
        /*fun launch(context: Context, mediaProjection: MediaProjection?) {
            startForegroundService(context, Intent(this, ScreenLiveService::class.java).apply {
                putExtra("code", resultCode)
                putExtra("data", data)
            })
        }*/
    }

}
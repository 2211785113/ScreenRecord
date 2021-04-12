package com.enjoy.screenpush.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.enjoy.screenpush.IMyAidlInterface
import com.enjoy.screenpush.R

/**
 * Created by lyr on 2021/4/10 & content is 录屏放在一个Service中 提供录屏服务-前台服务
 */
class ScreenLiveService : Service() {

    /**
     * 前台服务
     */
    override fun onCreate() {
        super.onCreate()

        val notification =
            Notification(R.mipmap.ic_launcher, "Notification comes", System.currentTimeMillis())
        val notificationIntent = Intent(this, ::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        notification.setLatestEventInfo(this, "This is title", "This is content", pendingIntent)
        startForeground(1, notification)
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
}
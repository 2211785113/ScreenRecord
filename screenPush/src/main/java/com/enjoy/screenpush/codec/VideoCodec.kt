package com.enjoy.screenpush.codec

import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.os.Bundle
import com.enjoy.screenpush.data.RTMPPackage
import com.enjoy.screenpush.thread.ScreenLiveRunnable
import java.io.IOException

/**
 * 视频编码
 */
class VideoCodec : Thread {

    private var screenLive: ScreenLiveRunnable

    private var mediaCodec: MediaCodec? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var isLiving: Boolean = false
    private var timeStamp: Long = 0
    private var startTime: Long = 0
    private var mediaProjection: MediaProjection? = null

    constructor(screenLive: ScreenLiveRunnable) {
        this.screenLive = screenLive
    }

    open fun startLive(mediaProjection: MediaProjection) {
        this.mediaProjection = mediaProjection

        // 配置编码参数
        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 360, 640)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        format.setInteger(MediaFormat.KEY_BIT_RATE, 400000)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 15)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)

        try {
            // 创建编码器
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            mediaCodec?.let { mediaCodec ->
                mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                // 从编码器创建一个画布, 画布上的图像会被编码器自动编码
                val surface = mediaCodec.createInputSurface()

                virtualDisplay = mediaProjection.createVirtualDisplay(
                        "screen-codec",
                        360, 640, 1,
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                        surface, null, null)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        start()
    }

    open fun stopLive() {
        isLiving = false
        try {
            join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun run() {
        isLiving = true
        mediaCodec?.let { mediaCodec ->
            mediaCodec.start()
        }

        val bufferInfo = MediaCodec.BufferInfo()
        while (isLiving) {
            if (timeStamp != 0L) {
                //2000毫秒 手动触发输出关键帧
                if (System.currentTimeMillis() - timeStamp >= 2000) {
                    val params = Bundle()
                    //立即刷新 让下一帧是关键帧
                    params.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0)
                    mediaCodec?.let { mediaCodec ->
                        mediaCodec.setParameters(params)
                        timeStamp = System.currentTimeMillis()
                    }
                }
            } else {
                timeStamp = System.currentTimeMillis()
            }

            mediaCodec?.let { mediaCodec ->
                val index = mediaCodec.dequeueOutputBuffer(bufferInfo, 10)
                if (index >= 0) {
                    val buffer = mediaCodec.getOutputBuffer(index)
                    val outData = ByteArray(bufferInfo.size)
                    buffer!!.get(outData)
                    //这样也能拿到 sps pps
                    //                ByteBuffer sps = mediaCodec.getOutputFormat().getByteBuffer
                    //                        ("csd-0");
                    //                ByteBuffer pps = mediaCodec.getOutputFormat().getByteBuffer
                    //                        ("csd-1");
                    if (startTime == 0L) {
                        // 微妙转为毫秒
                        startTime = bufferInfo.presentationTimeUs / 1000
                    }
                    val rtmpPackage = RTMPPackage()
                    rtmpPackage.buffer = outData
                    rtmpPackage.type =
                        RTMPPackage.RTMP_PACKET_TYPE_VIDEO
                    val tms = bufferInfo.presentationTimeUs / 1000 - startTime
                    rtmpPackage.tms = tms
                    screenLive.addPackage(rtmpPackage)
                    mediaCodec.releaseOutputBuffer(index, false)
                }
            }
        }
        isLiving = false
        startTime = 0

        mediaCodec?.let { mediaCodec ->
            mediaCodec.stop()
            mediaCodec.release()
            mediaCodec == null
        }

        virtualDisplay?.let { virtualDisplay ->
            virtualDisplay.release()
            virtualDisplay == null
        }

        mediaProjection?.let { mediaProjection ->
            mediaProjection.stop()
            mediaProjection == null
        }
    }
}
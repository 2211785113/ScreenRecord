package com.enjoy.screenpush.codec

import android.media.*
import com.enjoy.screenpush.data.RTMPPackage
import com.enjoy.screenpush.thread.ScreenLiveRunnable
import java.io.IOException

/**
 * 音频编码
 */
class AudioCodec : Thread {

    private lateinit var screenLive: ScreenLiveRunnable

    private var mediaCodec: MediaCodec? = null
    private var audioRecord: AudioRecord? = null
    private var isRecoding: Boolean = false

    private var startTime: Long = 0
    private var minBufferSize: Int = 0

    constructor(screenLive: ScreenLiveRunnable) {
        this.screenLive = screenLive
    }

    open fun startLive() {
        val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 44100,
                1)
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel
                .AACObjectLC)
        format.setInteger(MediaFormat.KEY_BIT_RATE, 64000)
        try {
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)

            mediaCodec?.let { mediaCodec ->
                mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                mediaCodec.start()
            }

            /**
             * 获得创建AudioRecord所需的最小缓冲区
             * 采样+单声道+16位pcm
             */
            minBufferSize = AudioRecord.getMinBufferSize(44100,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT)
            /**
             * 创建录音对象
             * 麦克风+采样+单声道+16位pcm+缓冲区大小
             */
            audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC, 44100,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, minBufferSize)


        } catch (e: IOException) {
            e.printStackTrace()
        }

        start()
    }

    fun stopLive() {
        isRecoding = false
        try {
            join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

    override fun run() {
        isRecoding = true

        var rtmpPackage = RTMPPackage()
        val audioDecoderSpecificInfo = byteArrayOf(0x12, 0x08)
        rtmpPackage.buffer = audioDecoderSpecificInfo
        rtmpPackage.type =
            RTMPPackage.RTMP_PACKET_TYPE_AUDIO_HEAD
        screenLive.addPackage(rtmpPackage)

        audioRecord?.let { audioRecord ->
            audioRecord.startRecording()
        }

        val bufferInfo = MediaCodec.BufferInfo()
        val buffer = ByteArray(minBufferSize)

        audioRecord?.let { audioRecord ->
            while (isRecoding) {
                val len = audioRecord.read(buffer, 0, buffer.size)
                if (len <= 0) {
                    continue
                }
                //立即得到有效输入缓冲区
                mediaCodec?.let { mediaCodec ->
                    var index = mediaCodec.dequeueInputBuffer(0)
                    if (index >= 0) {
                        val inputBuffer = mediaCodec.getInputBuffer(index)
                        inputBuffer!!.clear()
                        inputBuffer.put(buffer, 0, len)
                        //填充数据后再加入队列
                        mediaCodec.queueInputBuffer(index, 0, len,
                                System.nanoTime() / 1000, 0)
                    }
                    index = mediaCodec.dequeueOutputBuffer(bufferInfo, 0)
                    while (index >= 0 && isRecoding) {
                        val outputBuffer = mediaCodec.getOutputBuffer(index)
                        val outData = ByteArray(bufferInfo.size)
                        outputBuffer!!.get(outData)

                        if (startTime == 0L) {
                            startTime = bufferInfo.presentationTimeUs / 1000
                        }

                        rtmpPackage = RTMPPackage()
                        rtmpPackage.buffer = outData
                        rtmpPackage.type =
                            RTMPPackage.RTMP_PACKET_TYPE_AUDIO_DATA
                        val tms = bufferInfo.presentationTimeUs / 1000 - startTime
                        rtmpPackage.tms = tms
                        screenLive.addPackage(rtmpPackage)
                        mediaCodec.releaseOutputBuffer(index, false)
                        index = mediaCodec.dequeueOutputBuffer(bufferInfo, 0)
                    }
                }
            }
        }

        audioRecord?.let { audioRecord ->
            audioRecord.stop()
            audioRecord.release()
            audioRecord == null

            mediaCodec?.let { mediaCodec ->
                mediaCodec.stop()
                mediaCodec.release()
                mediaCodec == null
            }

            startTime = 0
            isRecoding = false
        }
    }
}
package com.enjoy.screenpush.data

/**
 * 发送数据类
 */
open class RTMPPackage {

    open var buffer: ByteArray? = null
        get() = field
        set(value) {
            field = value
        }

    open var type: Int = 0
        get() = field
        set(value) {
            field = value
        }

    open var tms: Long = 0L
        get() = field
        set(value) {
            field = value
        }

    companion object {
        open val RTMP_PACKET_TYPE_VIDEO = 0
        open val RTMP_PACKET_TYPE_AUDIO_HEAD = 1
        open val RTMP_PACKET_TYPE_AUDIO_DATA = 2

        open var EMPTY_PACKAGE = RTMPPackage()
    }
}
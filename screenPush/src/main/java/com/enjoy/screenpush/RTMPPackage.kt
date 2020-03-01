package com.enjoy.screenpush

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
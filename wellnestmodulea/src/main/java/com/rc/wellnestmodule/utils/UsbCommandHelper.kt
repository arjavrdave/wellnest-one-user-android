package com.rc.wellnestmodule.utils

object UsbCommandHelper {

    val instance: UsbCommandHelper = UsbCommandHelper



    fun sendStreamCmd(): ByteArray {
        return "+STREAM=0\r\n".toByteArray()
    }

    fun sendStartCmd():ByteArray{
        return "+START\r\n".toByteArray()
    }

    fun sendStatusCmd():ByteArray{
        return "+STATUS=4,0,1\r\n".toByteArray()
    }

    fun authorizeCmd():ByteArray {
        return "+AUTHRZ\r\n".toByteArray()
    }

    fun sendCmd():ByteArray {
       return "+TSEND\r\n".toByteArray()
    }

    fun startStreamCmd():ByteArray{
        return "+GETSTR=1\r\n".toByteArray()
    }

    fun stopStreamCmd():ByteArray{
        return "+GETSTR=0\r\n".toByteArray()
    }

    fun setSampleCommand(s:Int):ByteArray{
        return "+SAMPLE=$s\r\n".toByteArray()
    }

    fun getBatteryStatus():ByteArray{
        return "+GETSTA\r\n".toByteArray()
    }

    fun getDid() : ByteArray {
        return "+GETDID\r\n".toByteArray()
    }
}
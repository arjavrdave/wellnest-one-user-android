package com.rc.wellnestmodule.utils

import com.rc.wellnestmodule.interfaces.IWellNestUtil

object WellNestUtilFactory {
    fun getWellNestUtil(): IWellNestUtil {
            return  WellNestUtil()
    }
}
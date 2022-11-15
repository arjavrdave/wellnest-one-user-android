package com.wellnest.one.utils

import com.flyco.tablayout.listener.CustomTabEntity

/**
 * Created by Hussain on 10/11/22.
 */

class TabEntity(title: String, selectedIcon: Int, unSelectedIcon: Int) :
    CustomTabEntity {
    var title: String
    var selectedIcon: Int
    var unSelectedIcon: Int

    init {
        this.title = title
        this.selectedIcon = selectedIcon
        this.unSelectedIcon = unSelectedIcon
    }

    override fun getTabTitle(): String {
        return title
    }

    override fun getTabSelectedIcon(): Int {
        return selectedIcon
    }

    override fun getTabUnselectedIcon(): Int {
        return unSelectedIcon
    }
}
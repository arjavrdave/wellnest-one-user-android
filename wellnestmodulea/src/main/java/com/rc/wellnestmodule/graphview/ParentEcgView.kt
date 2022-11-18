package com.rc.wellnestmodule.graphview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

abstract class ParentEcgView:LinearLayout {

    constructor(context: Context?) : super(context){
        this.initialize(context)
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        this.initialize(context)
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ){
        this.initialize(context)
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes){
        this.initialize(context)
    }

    abstract fun initialize(context: Context?)
}
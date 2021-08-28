package com.jaytalekar.algoviz.ui.searchingsorting

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout

class BarGraphView : ConstraintLayout {

    companion object {
        const val TAG = "BarGraphView"
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {

    }
}
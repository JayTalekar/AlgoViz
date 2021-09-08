package com.jaytalekar.algoviz.ui

import android.view.View
import androidx.constraintlayout.widget.ConstraintSet

fun boundStartToParentStart(view: View, set: ConstraintSet) {
    set.connect(
        view.id,
        ConstraintSet.START,
        ConstraintSet.PARENT_ID,
        ConstraintSet.START
    )
}

fun boundEndToParentEnd(view: View, set: ConstraintSet) {
    set.connect(
        view.id,
        ConstraintSet.END,
        ConstraintSet.PARENT_ID,
        ConstraintSet.END
    )
}

fun boundBottomToParentBottom(view: View, set: ConstraintSet) {
    set.connect(
        view.id,
        ConstraintSet.BOTTOM,
        ConstraintSet.PARENT_ID,
        ConstraintSet.BOTTOM
    )
}

fun boundTopToParentTop(view: View, set: ConstraintSet) {
    set.connect(
        view.id,
        ConstraintSet.TOP,
        ConstraintSet.PARENT_ID,
        ConstraintSet.TOP
    )
}

fun boundEndToStartOf(firstView: View, secondView: View, set: ConstraintSet) {
    set.connect(
        firstView.id,
        ConstraintSet.END,
        secondView.id,
        ConstraintSet.START
    )
}

//            if (i == 0){
//                boundStartToParentStart(viewList[i], startSet)
//                boundStartToParentStart(viewList[i], endSet)
//
//                boundEndToStartOf(viewList[i], viewList[i + 1], startSet)
//                boundEndToStartOf(viewList[i], viewList[i + 1], endSet)
//            }else{
//                if (i == adapter.count - 1){
//                    boundEndToParentEnd(viewList[i], startSet)
//                    boundEndToParentEnd(viewList[i], endSet)
//                }else{
//                    boundEndToStartOf(viewList[i], viewList[i + 1], startSet)
//                    boundEndToStartOf(viewList[i], viewList[i + 1], endSet)
//                }
//            }


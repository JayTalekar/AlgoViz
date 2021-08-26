package com.jaytalekar.algoviz.ui.datastructures

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.jaytalekar.algoviz.R
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random

class StackFragment : Fragment() {

    companion object {
        const val TAG = "StackFragment"
        const val BOTTOM_OFFSET = 240
        const val ELEMENT_HEIGHT = 120
    }

    private lateinit var rootView: View

    private lateinit var stackRelativeLayout: RelativeLayout

    private lateinit var tvPush: TextView
    private lateinit var tvPop: TextView
    private lateinit var tvPeek: TextView
    private lateinit var ivStackBucket: ImageView
    private lateinit var tvNotifier: TextView

    private val elementViewIds = ArrayList<Int>()

    private val stack = Stack<Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.stack_fragment, container, false)

        init()

        return rootView
    }

    fun init() {
        stackRelativeLayout = rootView.findViewById(R.id.stack_frame_layout)

        tvPush = rootView.findViewById(R.id.tv_push)
        tvPop = rootView.findViewById(R.id.tv_pop)
        tvPeek = rootView.findViewById(R.id.tv_peek)
        ivStackBucket = rootView.findViewById(R.id.iv_stack_bucket)
        tvNotifier = rootView.findViewById(R.id.tv_notifier)

        tvPush.setOnClickListener(pushOnClickListener)
        tvPop.setOnClickListener(popOnClickListener)
        tvPeek.setOnClickListener(peekOnClickListener)
    }

    private val pushOnClickListener = View.OnClickListener { tvPush ->
        if (stack.size == 7)
            tvNotifier.text = "Stack Overflow"
        else {
            val element = getElement()

            val value = element.text.toString().toInt()
            stack.push(value)
            tvNotifier.text = "Pushed Element $value"

            elementViewIds.add(element.id)

            element.addToParentView()

            val startY = -ELEMENT_HEIGHT.toFloat()
            val endY =
                (ivStackBucket.bottom - BOTTOM_OFFSET - ELEMENT_HEIGHT * (stack.size - 1)).toFloat()

            animateElementEntry(element, startY, endY)
        }
    }

    private val popOnClickListener = View.OnClickListener { tvPop ->
        if (stack.isEmpty())
            tvNotifier.text = "Stack Underflow"
        else {
            val element = rootView.findViewById<TextView>(elementViewIds.last())

            elementViewIds.removeAt(elementViewIds.size - 1)
            val value = stack.pop()
            tvNotifier.text = "Popped Element $value"

            val endY = -ELEMENT_HEIGHT.toFloat()

            animateElementExit(element, endY)
        }
    }

    private val peekOnClickListener = View.OnClickListener { tvPeek ->
        if (stack.isEmpty())
            tvNotifier.text = "Stack Empty"
        else {
            val element = rootView.findViewById<TextView>(elementViewIds.last())

            tvNotifier.text = "Top Element is " + element.text.toString()

            val startX = -30f
            val endX = 30f

            animateElement(element, startX, endX)
        }
    }

    private fun getElement(): TextView {
        val element = layoutInflater.inflate(R.layout.tv_element_layout, null) as TextView
        element.id = View.generateViewId()
        element.text = Random.nextInt(100).toString()

        return element
    }

    private fun TextView.addToParentView() {
        val params = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE)

        stackRelativeLayout.addView(this, params)
    }

    private fun animateElementEntry(view: View, startY: Float, endY: Float) {
        val mover = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, startY, endY)
        mover.duration = 1000

        mover.start()
    }

    private fun animateElementExit(view: View, endY: Float) {
        val mover = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, endY)
        mover.duration = 1000

        mover.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                stackRelativeLayout.removeView(view)
            }
        })

        mover.start()
    }

    private fun animateElement(view: View, startX: Float, endX: Float) {
        val animator = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 0f, startX, endX)
        animator.repeatMode = ObjectAnimator.REVERSE
        animator.repeatCount = 1
        animator.duration = 250

        animator.start()
    }


}
package com.jaytalekar.algoviz.ui.datastructures

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
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
import kotlin.random.Random

class QueueFragment : Fragment() {

    companion object {
        const val TAG = "QueueFragment"
        const val RIGHT_OFFSET = 300
        const val ELEMENT_WIDTH = 120
    }

    private lateinit var rootView: View

    private lateinit var queueRelativeLayout: RelativeLayout

    private lateinit var tvEnqueue: TextView
    private lateinit var tvDequeue: TextView
    private lateinit var tvPeek: TextView
    private lateinit var ivQueue: ImageView
    private lateinit var tvNotifier: TextView

    private val elementViewIds = ArrayList<Int>()

    private val queue = ArrayList<Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_queue, container, false)

        init()

        return rootView
    }

    private fun init() {
        queueRelativeLayout = rootView.findViewById(R.id.queue_relative_layout)

        tvEnqueue = rootView.findViewById(R.id.tv_enqueue)
        tvDequeue = rootView.findViewById(R.id.tv_dequeue)
        tvPeek = rootView.findViewById(R.id.tv_peek)
        ivQueue = rootView.findViewById(R.id.iv_queue)
        tvNotifier = rootView.findViewById(R.id.tv_q_notifier)

        tvEnqueue.setOnClickListener(onClickListener)
        tvDequeue.setOnClickListener(onClickListener)
        tvPeek.setOnClickListener(onClickListener)
    }

    private val onClickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.tv_enqueue -> {
                if (queue.size == 7)
                    tvNotifier.text = "Queue Full"
                else {
                    val element = getElement()

                    val value = element.text.toString().toInt()
                    queue.add(value)
                    tvNotifier.text = "Enqueued Element $value"

                    elementViewIds.add(element.id)

                    element.addToParentView()

                    val startX = -ELEMENT_WIDTH.toFloat()
                    val endX =
                        (ivQueue.right - RIGHT_OFFSET - ELEMENT_WIDTH * (queue.size - 1)).toFloat()

                    animateElementEntry(element, startX, endX)
                }
            }

            R.id.tv_dequeue -> {
                if (queue.isEmpty())
                    tvNotifier.text = "Queue Empty"
                else {
                    val element = rootView.findViewById<TextView>(elementViewIds.first())

                    elementViewIds.removeAt(0)
                    val value = queue.removeAt(0)
                    tvNotifier.text = "Dequeued Element $value"

                    val endX = queueRelativeLayout.width.toFloat()

                    animateElementExit(element, endX)
                    animateElementsOnExit()
                }
            }

            R.id.tv_peek -> {
                if (queue.isEmpty())
                    tvNotifier.text = "Queue Empty"
                else {
                    val element = rootView.findViewById<TextView>(elementViewIds.first())

                    tvNotifier.text = "Element at Front is " + element.text.toString()

                    val startY = -30f
                    val endY = 30f

                    animateElement(element, startY, endY)
                }
            }
        }
    }

    fun getElement(): TextView {
        val element = layoutInflater.inflate(R.layout.tv_element_layout, null) as TextView
        element.id = View.generateViewId()
        element.text = Random.nextInt(100).toString()
        element.rotation = 90f

        return element
    }

    private fun TextView.addToParentView() {
        val params = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE)

        queueRelativeLayout.addView(this, params)
    }

    private fun animateElementEntry(view: View, startX: Float, endX: Float) {
        val mover = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, startX, endX)
        mover.duration = 1000

        mover.start()
    }

    private fun animateElementExit(view: View, endY: Float) {
        val mover = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, endY)
        mover.duration = 1000

        mover.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                queueRelativeLayout.removeView(view)
            }
        })

        mover.start()
    }

    private fun animateElementsOnExit() {
        val animatorList = ArrayList<Animator>()
        var c = 0
        for (id in elementViewIds) {
            val element = rootView.findViewById<TextView>(id)
            val mover = ObjectAnimator.ofFloat(
                element,
                View.TRANSLATION_X,
                (ivQueue.right - RIGHT_OFFSET - c * ELEMENT_WIDTH).toFloat()
            )
            animatorList.add(mover)
            c++
        }

        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(animatorList)
        animatorSet.duration = (elementViewIds.size * 50).toLong()

        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                tvDequeue.isClickable = false
            }

            override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                tvDequeue.isClickable = true
            }
        })

        animatorSet.start()
    }

    private fun animateElement(view: View, startY: Float, endY: Float) {
        val animator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0f, startY, endY)
        animator.repeatMode = ObjectAnimator.REVERSE
        animator.repeatCount = 1
        animator.duration = 250

        animator.start()
    }
}
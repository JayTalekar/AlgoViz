package com.jaytalekar.algoviz.ui.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.jaytalekar.algoviz.R

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.home_fragment, container, false) as MotionLayout
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        val tvDataStructures: TextView = rootView.findViewById(R.id.tv_ds)
        tvDataStructures.setOnClickListener(object : View.OnClickListener {
            override fun onClick(tvDS: View?) {
                Navigation.findNavController(rootView)
                    .navigate(R.id.action_homeFragment_to_dataStructures)
            }
        })
    }

    override fun onStart() {
        super.onStart()

        val header: TextView = rootView.findViewById(R.id.tv_header)
        val animator = ObjectAnimator.ofFloat(header, View.ALPHA, 0f, 1f)
        animator.duration = 2000
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                header.isClickable = false
            }

            override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                header.isClickable = true
            }
        })
        animator.start()

        header.setOnClickListener { view ->
            animator.start()
        }
    }
}
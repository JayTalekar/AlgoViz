package com.jaytalekar.algoviz.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.jaytalekar.algoviz.R

class DataStructures : Fragment() {

    private lateinit var rootView: View

    private lateinit var tvStack: TextView
    private lateinit var ivStack: ImageView
    private lateinit var tvQueue: TextView
    private lateinit var ivQueue: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.data_structures_fragment, container, false)

        init()

        return rootView
    }

    fun init() {
        tvStack = rootView.findViewById(R.id.tv_stack)
        ivStack = rootView.findViewById(R.id.iv_stack_bucket)
        tvQueue = rootView.findViewById(R.id.tv_queue)
        ivQueue = rootView.findViewById(R.id.iv_queue)

        tvStack.setOnClickListener(stackOnClickListener)
        ivStack.setOnClickListener(stackOnClickListener)
        tvQueue.setOnClickListener(queueOnClickListener)
        ivQueue.setOnClickListener(queueOnClickListener)
    }

    private val stackOnClickListener = View.OnClickListener {
        Navigation.findNavController(this.rootView)
            .navigate(R.id.action_dataStructures_to_stackFragment)
    }

    private val queueOnClickListener = View.OnClickListener {
        Navigation.findNavController(this.rootView)
            .navigate(R.id.action_dataStructures_to_queueFragment)
    }

}
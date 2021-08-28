package com.jaytalekar.algoviz.ui.searchingsorting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.jaytalekar.algoviz.R

class SortingFragment : Fragment() {

    private lateinit var rootView: View

    private lateinit var tvRandomData: TextView
    private lateinit var tvCustomData: TextView
    private lateinit var tvSort: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_sorting, container, false)

        init()

        return rootView
    }

    fun init() {
        tvRandomData = rootView.findViewById(R.id.tv_random_data)
        tvCustomData = rootView.findViewById(R.id.tv_add_custom_data)
        tvSort = rootView.findViewById(R.id.tv_sort)
    }
}
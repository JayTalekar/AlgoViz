package com.jaytalekar.algoviz.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.jaytalekar.algoviz.R

class SearchingSorting : Fragment() {

    private lateinit var rootView: View

    private lateinit var tvSearching: TextView
    private lateinit var tvSorting: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_searching_sorting, container, false)

        init()

        return rootView
    }

    fun init() {
        tvSorting = rootView.findViewById(R.id.tv_sorting)
        tvSorting.setOnClickListener {
            Navigation.findNavController(rootView)
                .navigate(R.id.action_searchingSorting_to_sortingFragment)
        }

        tvSearching = rootView.findViewById(R.id.tv_searching)
        tvSearching.setOnClickListener {
            Navigation.findNavController(rootView)
                .navigate(R.id.action_searchingSorting_to_searchFragment)
        }
    }

}
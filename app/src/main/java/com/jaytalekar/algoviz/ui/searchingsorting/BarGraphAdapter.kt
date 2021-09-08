package com.jaytalekar.algoviz.ui.searchingsorting

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.jaytalekar.algoviz.R

class BarGraphAdapter(
    private val valueList: MutableList<Int>,
    private val context: Context
) : BaseAdapter() {

    override fun getCount(): Int = valueList.size

    override fun getItem(position: Int): Int = valueList[position]

    fun setItem(position: Int, value: Int) {
        valueList[position] = value
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long = position.toLong()

    fun getAllItems(): List<Int> {
        return valueList
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = TextView(context)
        view.setTextColor(context.resources.getColor(R.color.black))
        view.setBackground(context.resources.getDrawable(R.drawable.bg_light_border))
        view.textSize = 24f
        view.gravity = Gravity.CENTER
        view.text = valueList[position].toString()
        return view
    }
}
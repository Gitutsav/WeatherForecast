package com.example.weatherforecast.adapters

import android.content.Context

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherforecast.R
import com.example.weatherforecast.models.TemperatureItem
import kotlin.collections.ArrayList


class HoursAdapter(
    context: Context,
    dataList: ArrayList<TemperatureItem>,
    ) : RecyclerView.Adapter<HoursAdapter.ViewHolder>() {
    private var recyclerDataList: ArrayList<TemperatureItem>
    private var context: Context

    init {
        this.recyclerDataList = dataList
        this.context = context
    }

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder { // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.hours_item, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemModel = recyclerDataList[position]
        setItem(itemModel, holder, position)
    }

    private fun setItem(itemModel: TemperatureItem, holder: ViewHolder, position: Int) {
        holder.label.text = itemModel.label
        holder.temperature.text = itemModel.temp
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return recyclerDataList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val label: TextView = itemView.findViewById(R.id.tv_label)
        val temperature: TextView = itemView.findViewById(R.id.tv_temperature)

    }

}
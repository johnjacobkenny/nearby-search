package me.kennyj.nearbysearch.presentation

import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import me.kennyj.nearbysearch.R

class HistoryListAdapter(private val data: List<Location>):RecyclerView.Adapter<HistoryListAdapter.ViewHolder>()  {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val historyTextView: TextView = view.findViewById(R.id.tvHistory)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_item, parent, false)

        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val location = data[position]
        holder.historyTextView.text = "${location.latitude}, ${location.longitude}"
    }

    override fun getItemCount(): Int = data.size
}
package edu.vt.cs5254.dreamcatcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs5254.dreamcatcher.databinding.ListItemDreamEntryBinding


class DreamEntryHolder(private val binding: ListItemDreamEntryBinding):
    RecyclerView.ViewHolder(binding.root){


    lateinit var boundEntry: DreamEntry
        private set

    fun bind(entry: DreamEntry) {
        binding.dreamEntryButton.configureForEntry(entry)
        boundEntry = entry
    }

    private fun Button.configureForEntry(entry: DreamEntry) {

        text = entry.kind.toString()
        visibility = View.VISIBLE

        when(entry.kind) {
            DreamEntryKind.REFLECTION -> {
                text = entry.text
                isAllCaps = false
                setBackgroundWithContrastingText("teal")


            }
            DreamEntryKind.DEFERRED -> {
                setBackgroundWithContrastingText("lightgray")


            }
            DreamEntryKind.FULFILLED -> {
                setBackgroundWithContrastingText("blue")


            }
            DreamEntryKind.CONCEIVED -> {
                setBackgroundWithContrastingText("green")

            }
        }
    }

    }

class DreamEntryAdapter(private val entries: List<DreamEntry>): RecyclerView.Adapter<DreamEntryHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DreamEntryHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemDreamEntryBinding.inflate(inflater,parent,false)
        return DreamEntryHolder(binding)
    }

    override fun getItemCount(): Int {
        return entries.size
    }

    override fun onBindViewHolder(holder: DreamEntryHolder, position: Int) {
        holder.bind(entries[position])
    }

}
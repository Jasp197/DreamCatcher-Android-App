package edu.vt.cs5254.dreamcatcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.vt.cs5254.dreamcatcher.databinding.ListItemDreamBinding
import java.util.UUID

class DreamHolder(private val binding: ListItemDreamBinding) : RecyclerView.ViewHolder(binding.root) {

    lateinit var boundDream: Dream
        private set



    fun bind(dream:Dream, onDreamClicked: (UUID) -> Unit) {

        boundDream = dream
        binding.root.setOnClickListener {
            onDreamClicked(dream.id)
        }


        binding.listItemTitle.text = dream.title
        val reflectionCount = dream.entries.count {it.kind == DreamEntryKind.REFLECTION}
        val refCountString = binding.root.context.getString(R.string.reflection_count, reflectionCount)

        binding.listItemReflectionCount.text = refCountString

        when {
            dream.isDeferred -> {
                binding.listItemImage.visibility = View.VISIBLE
                binding.listItemImage.setImageResource(R.drawable.ic_dream_deferred)
            }
            dream.isFulfilled -> {
                binding.listItemImage.visibility = View.VISIBLE
                binding.listItemImage.setImageResource(R.drawable.ic_dream_fulfilled)
            }
            else -> {
                binding.listItemImage.visibility = View.GONE

            }
        }
        binding.listItemImage
    }
}


class DreamListAdapter(
    private val dreams: List<Dream>,
    private val onDreamClicked: (UUID) -> Unit
): RecyclerView.Adapter<DreamHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DreamHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemDreamBinding.inflate(inflater, parent, false)

        return DreamHolder(binding)

    }

    override fun getItemCount(): Int {
        return dreams.size
    }

    override fun onBindViewHolder(holder: DreamHolder, position: Int) {
        holder.bind(dreams[position], onDreamClicked)
    }
}
package net.bjoernpetersen.qbert.view.config.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.bjoernpetersen.musicbot.api.config.Config
import net.bjoernpetersen.qbert.android.createView
import net.bjoernpetersen.qbert.databinding.ItemConfigEntryBinding

class ConfigEntryListAdapter : RecyclerView.Adapter<ConfigEntryViewHolder>() {
    var entries: List<Config.Entry<*>> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConfigEntryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemConfigEntryBinding.inflate(inflater, parent, false)
        return ConfigEntryViewHolder(binding)
    }

    override fun getItemCount(): Int = entries.size

    override fun onBindViewHolder(holder: ConfigEntryViewHolder, position: Int) {
        holder.setEntry(entries[position])
    }
}

class ConfigEntryViewHolder(
    private val binding: ItemConfigEntryBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun setEntry(entry: Config.Entry<*>) {
        binding.name = entry.key
        binding.description = entry.description
        binding.isValid = entry.checkError() == null

        val view = entry.createView(itemView.context) {
            binding.isValid = entry.checkError() == null
        }
        binding.container.removeAllViews()
        binding.container.addView(view)
    }
}

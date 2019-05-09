package net.bjoernpetersen.qbert.view.config.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.bjoernpetersen.musicbot.spi.plugin.Plugin
import net.bjoernpetersen.qbert.databinding.ItemConfigPluginBinding

class ConfigPluginListAdapter(
    private val onClick: (Plugin) -> Unit
) : RecyclerView.Adapter<ConfigPluginListAdapter.ConfigPluginViewHolder>() {
    var plugins: List<Plugin> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConfigPluginViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemConfigPluginBinding.inflate(inflater, parent, false)
        return ConfigPluginViewHolder(binding)
    }

    override fun getItemCount(): Int = plugins.size

    override fun onBindViewHolder(holder: ConfigPluginViewHolder, position: Int) {
        holder.setPlugin(plugins[position])
    }

    inner class ConfigPluginViewHolder(
        private val binding: ItemConfigPluginBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private var plugin: Plugin? = null

        init {
            itemView.setOnClickListener { plugin?.let(onClick) }
        }

        fun setPlugin(plugin: Plugin) {
            this.plugin = plugin
            binding.name = plugin.name
            binding.description = plugin.description
        }
    }
}



package net.bjoernpetersen.qbert.view.config.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import net.bjoernpetersen.musicbot.spi.plugin.Plugin
import net.bjoernpetersen.qbert.databinding.ItemImplementationSelectBinding

class ImplementationSelectAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val default: MutableLiveData<Plugin>,
    private val onEnable: (Plugin) -> Unit
) : RecyclerView.Adapter<ImplementationSelectAdapter.ImplementationSelectViewHolder>() {
    var items: List<Plugin> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ImplementationSelectViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemImplementationSelectBinding.inflate(inflater, parent, false)
        return ImplementationSelectViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ImplementationSelectViewHolder, position: Int) {
        holder.plugin = items[position]
    }

    inner class ImplementationSelectViewHolder(
        private val binding: ItemImplementationSelectBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        var plugin: Plugin? = null
            set(value) {
                field = value
                binding.plugin = value
                binding.radioButton.isChecked = default.value == value
            }

        init {
            default.observe(lifecycleOwner) {
                binding.radioButton.isChecked = it == plugin
            }
            binding.root.setOnClickListener { binding.radioButton.isChecked = true }
            binding.radioButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) onEnable(plugin!!)
            }
        }
    }
}



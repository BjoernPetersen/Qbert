package net.bjoernpetersen.qbert.view.config.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.bjoernpetersen.musicbot.spi.plugin.Plugin
import net.bjoernpetersen.qbert.databinding.ItemDependencyListBinding

class DependencyListAdapter(
    private val onClick: (String) -> Unit,
    private val onDependency: (String) -> Unit
) : RecyclerView.Adapter<DependencyListAdapter.ActiveListViewHolder>() {
    var items: List<PluginInfo> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActiveListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDependencyListBinding.inflate(inflater, parent, false)
        return ActiveListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ActiveListViewHolder, position: Int) {
        holder.info = items[position]
    }

    inner class ActiveListViewHolder(
        private val binding: ItemDependencyListBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        var info: PluginInfo?
            get() = binding.info
            set(value) {
                binding.info = value
            }

        init {
            binding.editImplementation.setOnClickListener { info?.let { onClick(it.idClassName) } }
            binding.selectDependencies.setOnClickListener {
                info?.let { onDependency(it.active!!::class.qualifiedName!!) }
            }
        }
    }
}

class PluginInfo(
    val idClassName: String,
    val displayName: String,
    val active: Plugin?,
    val hasDependencies: Boolean,
    val isSatisfied: Boolean
)

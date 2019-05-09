package net.bjoernpetersen.qbert.view.config.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import net.bjoernpetersen.musicbot.spi.plugin.Plugin
import net.bjoernpetersen.qbert.android.PluginType
import net.bjoernpetersen.qbert.databinding.FragmentConfigPluginListBinding
import net.bjoernpetersen.qbert.impl.createBrowserOpener
import net.bjoernpetersen.qbert.view.config.adapter.ConfigPluginListAdapter
import net.bjoernpetersen.qbert.view.config.viewmodel.ConfigPluginListViewModel

class ConfigPluginListFragment : Fragment(), CoroutineScope by MainScope() {
    private val viewModel by viewModels<ConfigPluginListViewModel>()
    private val type by lazy {
        val typeName = requireArguments().getString(KEY_TYPE)!!
        PluginType.valueOf(typeName)
    }
    private lateinit var binding: FragmentConfigPluginListBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConfigPluginListBinding.inflate(inflater, container, false)
        binding.list.layoutManager = LinearLayoutManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = ConfigPluginListAdapter(::onClick)
        binding.list.adapter = adapter
        launch {
           adapter.plugins =  viewModel.findPlugins(requireActivity().createBrowserOpener(), type)
        }
    }

    private fun onClick(plugin: Plugin) {
        val action = ConfigOverviewFragmentDirections.configurePlugin(plugin::class.qualifiedName!!)
        findNavController().navigate(action)
    }

    companion object {
        private const val KEY_TYPE = "type"

        operator fun invoke(type: PluginType) = ConfigPluginListFragment().apply {
            arguments = bundleOf(KEY_TYPE to type.name)
        }
    }
}

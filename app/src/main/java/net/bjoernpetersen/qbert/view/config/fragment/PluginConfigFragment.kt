package net.bjoernpetersen.qbert.view.config.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import net.bjoernpetersen.qbert.android.type
import net.bjoernpetersen.qbert.databinding.FragmentPluginConfigBinding
import net.bjoernpetersen.qbert.impl.createBrowserOpener
import net.bjoernpetersen.qbert.view.config.adapter.PluginConfigPagerAdapter
import net.bjoernpetersen.qbert.view.config.viewmodel.ConfigPluginListViewModel

class PluginConfigFragment : Fragment(), CoroutineScope by MainScope() {
    private lateinit var binding: FragmentPluginConfigBinding
    private val viewModel by viewModels<ConfigPluginListViewModel>()
    private val args by navArgs<PluginConfigFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPluginConfigBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = PluginConfigPagerAdapter(childFragmentManager)
        binding.viewPager.adapter = adapter
        binding.viewPager.currentItem = minOf(1, maxOf(0, args.tab))

        val browserOpener = requireActivity().createBrowserOpener()
        launch {
            val plugin = viewModel.getPlugin(browserOpener, args.pluginClassName)
            binding.name = plugin.name
            binding.description = plugin.description
            binding.type = plugin::class.type
            adapter.setEntries(viewModel.findConfigs(browserOpener, plugin))
        }
    }
}

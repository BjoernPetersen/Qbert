package net.bjoernpetersen.qbert.view.config.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import net.bjoernpetersen.musicbot.spi.plugin.Plugin
import net.bjoernpetersen.musicbot.spi.plugin.idName
import net.bjoernpetersen.musicbot.spi.plugin.isIdBase
import net.bjoernpetersen.qbert.android.type
import net.bjoernpetersen.qbert.databinding.FragmentImplementationSelectBinding
import net.bjoernpetersen.qbert.view.config.adapter.ImplementationSelectAdapter
import net.bjoernpetersen.qbert.view.config.viewmodel.ImplementationSelectViewModel
import kotlin.reflect.KClass

class ImplementationSelectFragment : Fragment(), CoroutineScope by MainScope() {
    private lateinit var binding: FragmentImplementationSelectBinding

    private val args by navArgs<ImplementationSelectFragmentArgs>()

    private val base: KClass<out Plugin> by lazy {
        @Suppress("UNCHECKED_CAST")
        Class.forName(args.base).kotlin as KClass<out Plugin>
    }
    private val viewModel: ImplementationSelectViewModel by viewModels {
        ImplementationSelectViewModel.Factory(base, requireActivity().filesDir)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImplementationSelectBinding.inflate(inflater, container, false)
        binding.list.layoutManager = LinearLayoutManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.baseClassName = base.qualifiedName!!
        binding.baseDisplayName = if (base.isIdBase) base.idName else base.simpleName
        binding.type = base.type

        val adapter = ImplementationSelectAdapter(this, viewModel.default) {
            viewModel.setDefault(it)
        }
        binding.list.adapter = adapter

        binding.disableButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && binding.isLoaded) viewModel.setDefault(null)
        }

        viewModel.implementations.observe(this) { adapter.items = it }
        viewModel.default.observe(this) { default: Plugin? ->
            binding.isDisabled = default == null
        }

        if (viewModel.implementations.value!!.isNotEmpty()) {
            binding.isLoaded = true
        } else refresh()
    }

    private fun refresh() {
        launch {
            viewModel.refreshImplementations()
            binding.isLoaded = true
        }
    }
}

package net.bjoernpetersen.qbert.view.config.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import net.bjoernpetersen.qbert.R
import net.bjoernpetersen.qbert.android.ActivePluginType
import net.bjoernpetersen.qbert.databinding.FragmentColdBinding
import net.bjoernpetersen.qbert.impl.Insanity
import net.bjoernpetersen.qbert.impl.MissingDependency
import net.bjoernpetersen.qbert.impl.NoProvider
import net.bjoernpetersen.qbert.impl.PluginConfigError
import net.bjoernpetersen.qbert.view.config.viewmodel.ColdViewModel

class ColdFragment : Fragment(), CoroutineScope by MainScope() {
    private lateinit var binding: FragmentColdBinding

    private val viewModel: ColdViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentColdBinding.inflate(inflater, container, false)
        binding.start.setOnClickListener { start() }
        binding.configure.setOnClickListener { configure() }
        binding.dependencies.setOnClickListener { dependencies() }
        binding.fixConfig.setOnClickListener {
            val insanity = viewModel.insanities.value!!
            fixConfig(insanity)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.insanities.observe(this) {
            binding.isValid = it?.isEmpty() ?: false
            binding.isChecked = it != null
            binding.infoTextId = if (it.isNullOrEmpty()) 0 else it.first().errorMessage
        }
    }

    override fun onPause() {
        viewModel.insanities.postValue(null)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.infoTextId = R.string.checking_running
        if (viewModel.isRunning()) start()
        else {
            binding.infoTextId = R.string.checking_sanity
            viewModel.checkSanity()
        }
    }

    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }

    private fun start() {
        findNavController().navigate(R.id.start_bot)
    }

    private fun configure() {
        findNavController().navigate(R.id.view_config)
    }

    private fun dependencies() {
        findNavController().navigate(R.id.view_dependencies)
    }

    private fun fixConfig(insanity: List<Insanity>) {
        val first = insanity.first()
        // TODO go through all errors
        val action = when (first) {
            NoProvider ->
                ColdFragmentDirections.viewDependencies(ActivePluginType.PROVIDER)
            is PluginConfigError ->
                ColdFragmentDirections.configurePlugin(first.pluginClassName, first.tab)
            is MissingDependency ->
                ColdFragmentDirections.selectImplementation(first.baseClassName)
        }

        findNavController().navigate(action)
    }
}

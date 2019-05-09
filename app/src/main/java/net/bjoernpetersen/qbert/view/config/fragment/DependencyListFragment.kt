package net.bjoernpetersen.qbert.view.config.fragment

import android.app.AlertDialog
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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.bjoernpetersen.qbert.R
import net.bjoernpetersen.qbert.databinding.FragmentDependencyListBinding
import net.bjoernpetersen.qbert.view.config.adapter.DependencyListAdapter
import net.bjoernpetersen.qbert.view.config.viewmodel.DependencyListViewModel

class DependencyListFragment : Fragment(), CoroutineScope by MainScope() {
    private val bases: List<String> by lazy {
        requireArguments().getStringArray(ARG_BASES)!!.toList()
    }
    private lateinit var binding: FragmentDependencyListBinding

    private val viewModel: DependencyListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDependencyListBinding.inflate(inflater, container, false)
        binding.isLoaded = false
        binding.isEmpty = false
        binding.list.layoutManager = LinearLayoutManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = DependencyListAdapter(::onItemClick, ::onDependencyClick)
        binding.list.adapter = adapter
        viewModel.info.observe(this) {
            adapter.items = it
            binding.isEmpty = it.isEmpty()
        }
        if (viewModel.info.value!!.isNotEmpty()) {
            binding.isLoaded = true
        } else refresh()
    }

    private fun refresh() {
        launch {
            viewModel.refreshInfo(requireActivity().filesDir, bases)
            binding.isLoaded = true
        }
    }

    private fun onItemClick(baseClassName: String) {
        val action = DependencyListFragmentDirections.selectImplementation(baseClassName)
        findNavController().navigate(action)
    }

    private fun onDependencyClick(implClassName: String) {
        val dialog = AlertDialog.Builder(requireContext())
            .setCancelable(false)
            .setMessage(R.string.loading)
            .show()

        launch {
            val bases = viewModel.getDependencies(implClassName)
            dialog.hide()
            val action = DependencyListFragmentDirections.selectDependencies(bases.toTypedArray())
            findNavController().navigate(action)
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }

    companion object {
        private const val ARG_BASES = "bases"

        operator fun invoke(bases: List<String>): DependencyListFragment {
            return DependencyListFragment().apply {
                arguments = bundleOf(ARG_BASES to bases.toTypedArray())
            }
        }
    }
}

package net.bjoernpetersen.qbert.view.config.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import net.bjoernpetersen.qbert.databinding.FragmentConfigEntryBinding
import net.bjoernpetersen.qbert.view.config.adapter.ConfigEntryListAdapter
import net.bjoernpetersen.qbert.view.config.viewmodel.ConfigEntryViewModel

class ConfigEntryFragment : Fragment() {
    private val viewModel: ConfigEntryViewModel by viewModels()
    private lateinit var binding: FragmentConfigEntryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConfigEntryBinding.inflate(inflater, container, false)
        binding.list.layoutManager = LinearLayoutManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = ConfigEntryListAdapter()
        binding.list.adapter = adapter
        viewModel.entries.observe(this) {
            adapter.entries = it
        }
    }
}

package net.bjoernpetersen.qbert.view.config.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import net.bjoernpetersen.qbert.databinding.FragmentDependencyOverviewBinding
import net.bjoernpetersen.qbert.view.config.adapter.ActiveListPagerAdapter
import net.bjoernpetersen.qbert.view.config.viewmodel.DependencyOverviewViewModel

class DependencyOverviewFragment : Fragment() {
    private lateinit var binding: FragmentDependencyOverviewBinding
    private val args by navArgs<DependencyOverviewFragmentArgs>()
    private val viewModel: DependencyOverviewViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDependencyOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.refreshActiveBases(requireActivity().filesDir)
        val viewPager = binding.viewPager
        val adapter = ActiveListPagerAdapter(childFragmentManager)
        viewPager.adapter = adapter
        viewPager.currentItem = args.startTab.ordinal

        viewModel.bases.observe(this) {
            adapter.bases = it
        }
    }
}

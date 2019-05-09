package net.bjoernpetersen.qbert.view.config.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import net.bjoernpetersen.qbert.databinding.FragmentConfigOverviewBinding
import net.bjoernpetersen.qbert.view.config.adapter.ConfigOverviewPagerAdapter

class ConfigOverviewFragment : Fragment() {
    private lateinit var binding: FragmentConfigOverviewBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConfigOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.viewPager.adapter = ConfigOverviewPagerAdapter(childFragmentManager)
    }
}

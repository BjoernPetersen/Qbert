package net.bjoernpetersen.qbert.view.config.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import net.bjoernpetersen.qbert.databinding.FragmentLoadingBinding

class LoadingFragment : Fragment() {
    private lateinit var binding: FragmentLoadingBinding
    private var replacement: Fragment? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (replacement != null) {
            childFragmentManager.commit {
                replace(binding.container.id, replacement!!)
            }
            binding.isReplaced = true
        }
    }

    fun replace(other: Fragment) {
        replacement = other
        if (context != null) childFragmentManager.commit(true) {
            replace(binding.container.id, other)
            binding.isReplaced = true
        }
    }
}

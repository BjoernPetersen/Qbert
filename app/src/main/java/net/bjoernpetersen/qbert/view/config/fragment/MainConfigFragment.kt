package net.bjoernpetersen.qbert.view.config.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import net.bjoernpetersen.qbert.R
import net.bjoernpetersen.qbert.databinding.FragmentMainConfigBinding
import net.bjoernpetersen.qbert.impl.createBrowserOpener
import net.bjoernpetersen.qbert.view.config.viewmodel.ConfigEntryViewModel
import net.bjoernpetersen.qbert.view.config.viewmodel.ConfigPluginListViewModel

class MainConfigFragment : Fragment(), CoroutineScope by MainScope() {
    private lateinit var binding: FragmentMainConfigBinding
    private lateinit var fragment: Fragment
    private val viewModel by viewModels<ConfigPluginListViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainConfigBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fragment = ConfigEntryFragment()
        childFragmentManager.commit {
            replace(R.id.container, fragment)
        }
        val childModel by fragment.viewModels<ConfigEntryViewModel>()
        launch {
            val entries = viewModel.getMainConfigEntries(requireActivity().createBrowserOpener())
            childModel.entries.postValue(entries)
        }
    }
}

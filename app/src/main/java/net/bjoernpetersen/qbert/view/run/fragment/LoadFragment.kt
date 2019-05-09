package net.bjoernpetersen.qbert.view.run.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import net.bjoernpetersen.musicbot.spi.plugin.management.InitStateWriter
import net.bjoernpetersen.qbert.android.intent
import net.bjoernpetersen.qbert.databinding.FragmentLoadBinding
import net.bjoernpetersen.qbert.lifecycle.Lifecyclist
import net.bjoernpetersen.qbert.service.BotService
import net.bjoernpetersen.qbert.view.run.viewmodel.RunViewModel2

class LoadFragment : Fragment() {
    private lateinit var binding: FragmentLoadBinding
    private val viewModel by activityViewModels<RunViewModel2>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val writer = DumbWriter()
        viewModel.bot.observe(this) { bot ->
            if (bot != null) {
                bot.setInitStateWriterFactory { writer }
                bot.stage.observe(this) {
                    if (it >= Lifecyclist.Stage.Running) {
                        val action: NavDirections = LoadFragmentDirections.showRunning()
                        findNavController().navigate(action)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireContext().apply { startForegroundService(intent<BotService>()) }
    }

    private inner class DumbWriter : InitStateWriter {
        override fun state(state: String) {
            binding.message = state
        }

        override fun warning(warning: String) {
            binding.message = warning
        }
    }
}

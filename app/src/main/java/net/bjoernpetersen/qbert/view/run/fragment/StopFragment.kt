package net.bjoernpetersen.qbert.view.run.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import net.bjoernpetersen.qbert.android.intent
import net.bjoernpetersen.qbert.app
import net.bjoernpetersen.qbert.databinding.FragmentStopBinding
import net.bjoernpetersen.qbert.lifecycle.Lifecyclist
import net.bjoernpetersen.qbert.service.BotService
import net.bjoernpetersen.qbert.view.run.viewmodel.RunViewModel2

class StopFragment : Fragment() {
    private lateinit var binding: FragmentStopBinding
    private val viewModel by activityViewModels<RunViewModel2>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val action = StopFragmentDirections.returnToMain()
        val activity = requireActivity()
        if (!activity.app.isServiceRunning) return findNavController().navigate(action)
        else {
            viewModel.bot.observe(this) { bot ->
                if (bot == null) {
                    if (!requireActivity().app.isServiceRunning)
                        findNavController().navigate(action)
                } else bot.stage.observe(this) {
                    if (it == Lifecyclist.Stage.Stopped) {
                        findNavController().navigate(action)
                    }
                }
            }
        }

        activity.apply { stopService(intent<BotService>()) }
    }
}

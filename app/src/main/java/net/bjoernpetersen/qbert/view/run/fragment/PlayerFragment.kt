package net.bjoernpetersen.qbert.view.run.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import net.bjoernpetersen.qbert.databinding.FragmentPlayerBinding
import net.bjoernpetersen.qbert.lifecycle.Lifecyclist
import net.bjoernpetersen.qbert.view.run.viewmodel.RunViewModel2

class PlayerFragment : Fragment(), CoroutineScope by MainScope() {
    private lateinit var binding: FragmentPlayerBinding
    private val viewModel by activityViewModels<RunViewModel2>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.time = "Placeholder"
        viewModel.bot.observe(this) { bot ->
            if (bot == null) {
                findNavController().navigate(PlayerFragmentDirections.stop())
            } else bot.stage.observe(this) {
                if (it > Lifecyclist.Stage.Running) {
                    val action = PlayerFragmentDirections.stop()
                    findNavController().navigate(action)
                }
            }
        }
    }
}

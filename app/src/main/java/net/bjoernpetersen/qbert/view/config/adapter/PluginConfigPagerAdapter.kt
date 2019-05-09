package net.bjoernpetersen.qbert.view.config.adapter

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.viewModels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.bjoernpetersen.qbert.impl.ConfigEntries
import net.bjoernpetersen.qbert.view.config.fragment.ConfigEntryFragment
import net.bjoernpetersen.qbert.view.config.viewmodel.ConfigEntryViewModel

class PluginConfigPagerAdapter(
    manager: FragmentManager
) : FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val fragments = Array(2) { ConfigEntryFragment() }
    private val mutexes = Array(2) { Mutex(true) }
    suspend fun setEntries(config: ConfigEntries) {
        withContext(Dispatchers.Main) {
            fragments.forEachIndexed { index, fragment ->
                mutexes[index].withLock { }
                val entries = if (index == 0) config.plain else config.secret
                val viewModel by fragment.viewModels<ConfigEntryViewModel>()
                viewModel.entries.postValue(entries)
            }
        }
    }

    override fun getItem(position: Int): Fragment = fragments[position]
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return super.instantiateItem(container, position).also {
            runBlocking { mutexes[position].unlock() }
        }
    }

    override fun getCount(): Int = 2
    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Plain"
            1 -> "Secret"
            else -> throw IllegalArgumentException()
        }
    }

}

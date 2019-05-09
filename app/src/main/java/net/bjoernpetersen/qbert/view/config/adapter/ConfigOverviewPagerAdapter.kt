package net.bjoernpetersen.qbert.view.config.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import net.bjoernpetersen.qbert.android.PluginType
import net.bjoernpetersen.qbert.view.config.fragment.ConfigPluginListFragment
import net.bjoernpetersen.qbert.view.config.fragment.MainConfigFragment

class ConfigOverviewPagerAdapter(
    manager: FragmentManager
) : FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private fun getType(position: Int): PluginType = when (position) {
        1 -> PluginType.PROVIDER
        2 -> PluginType.SUGGESTER
        3 -> PluginType.PLAYBACK_FACTORY
        4 -> PluginType.GENERIC
        else -> throw IllegalArgumentException()
    }

    override fun getItem(position: Int): Fragment {
        return if (position == 0) MainConfigFragment()
        else ConfigPluginListFragment(getType(position))
    }

    override fun getCount(): Int = 5

    override fun getPageTitle(position: Int): CharSequence? {
        return if (position == 0) "Main"
        else getType(position).displayName
    }
}

package net.bjoernpetersen.qbert.view.config.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import net.bjoernpetersen.qbert.android.ActivePluginType
import net.bjoernpetersen.qbert.view.config.fragment.DependencyListFragment
import net.bjoernpetersen.qbert.view.config.fragment.LoadingFragment

class ActiveListPagerAdapter(
    manager: FragmentManager
) : FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val fragments = Array(count) { LoadingFragment() }
    var bases: Map<ActivePluginType, List<String>> = emptyMap()
        set(value) {
            field = value
            refresh()
        }

    private fun refresh() {
        ActivePluginType.values().forEachIndexed { index, type ->
            bases[type]?.let { bases ->
                val fragment = DependencyListFragment(bases)
                fragments[index].replace(fragment)
            }
        }
    }

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return ActivePluginType.values()[position].name
    }

    override fun getCount(): Int = ActivePluginType.values().size
}

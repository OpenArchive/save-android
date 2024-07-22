package net.opendasharchive.openarchive.features.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import net.opendasharchive.openarchive.db.Project
import net.opendasharchive.openarchive.features.settings.SettingsFragment

class PagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {

    var projects = listOf<Project>()

//    override fun getItemId(position: Int): Long {
//        return projects[position].id
//    }
//
//    override fun containsItem(itemId: Long): Boolean = projects.any { it.id == itemId }

    fun getProject(i: Int): Project? {
        return if (i > -1 && i < projects.size) projects[i] else null
    }

    val settingsIndex = 1
        //get() = max(1, projects.size)

    fun updateData(projects: List<Project>) {
        this.projects = projects
        notifyItemRangeChanged(0, projects.size)
    }

    fun getPageTitle(position: Int): CharSequence? {
        return getProject(position)?.description
    }

    override fun getItemCount(): Int {
        return 2
        // return max(1, projects.size) + 1
    }

    fun getProjectIndexById(id: Long, default: Int = 0): Int {
        val index = projects.indexOfFirst { it.id == id }
        return when (index) {
            -1 -> default
            else -> index
        }
    }

    fun notifyProjectChanged(project: Project) {
        val index = projects.indexOf(project)
        if (index != -1) {
            notifyItemChanged(index)
        }
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            settingsIndex -> SettingsFragment()
            else -> {
                val project = getProject(position)
                return MainMediaFragment.newInstance(project?.id ?: -1)
            }
        }
    }
}

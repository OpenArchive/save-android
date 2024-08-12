package net.opendasharchive.openarchive.features.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import net.opendasharchive.openarchive.db.Folder
import net.opendasharchive.openarchive.features.settings.SettingsFragment

class PagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {

    var folders = listOf<Folder>()

//    override fun getItemId(position: Int): Long {
//        return projects[position].id
//    }
//
//    override fun containsItem(itemId: Long): Boolean = projects.any { it.id == itemId }

    fun getFolder(i: Int): Folder? {
        return if (i > -1 && i < folders.size) folders[i] else null
    }

    val settingsIndex = 1
        //get() = max(1, projects.size)

    fun updateData(folders: List<Folder>) {
        this.folders = folders
        notifyItemRangeChanged(0, folders.size)
    }

    fun getPageTitle(position: Int): CharSequence? {
        return getFolder(position)?.description
    }

    override fun getItemCount(): Int {
        return 2
    }

    fun getProjectIndexById(id: Long, default: Int = 0): Int {
        val index = folders.indexOfFirst { it.id == id }
        return when (index) {
            -1 -> default
            else -> index
        }
    }

    fun notifyFolderChanged(folder: Folder) {
        val index = folders.indexOf(folder)
        if (index != -1) {
            notifyItemChanged(index)
        }
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            settingsIndex -> SettingsFragment()
            else -> {
                val folder = getFolder(position)
                return MainMediaFragment.newInstance(folder?.id ?: -1)
            }
        }
    }
}

package net.opendasharchive.openarchive.features.main

import android.content.Context
import android.util.AttributeSet
import android.view.MenuItem
import android.widget.ImageView
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import net.opendasharchive.openarchive.R

class OABottomTabBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BottomNavigationView(context, attrs, defStyleAttr) {

    private var noLabelItemId = NO_ID
    private var bigIconItemId = NO_ID

    fun setNoLabelItemId(itemId: Int) {
        noLabelItemId = itemId
        updateMenuItems()
    }

    fun setBigIconItemId(itemId: Int) {
        bigIconItemId = itemId
        updateMenuItems()
    }

    fun updateMenuItems() {
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)

            if (item.itemId == noLabelItemId) {
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                item.title = null
            }

            if (item.itemId == bigIconItemId) {
                setIconSize(item.itemId, 48f) // Adjust this value as needed
            } else {
                setIconSize(item.itemId, 24f) // Default icon size
            }
        }

        // Force a redraw of the menu
        menu.clear()
        inflateMenu(R.menu.menu_bottom_nav)
    }

    private fun setIconSize(itemId: Int, size: Float) {
        val menuView = getChildAt(0) as BottomNavigationMenuView
        val itemView = menuView.findViewById<BottomNavigationItemView>(itemId)
        val iconView = itemView.findViewById<ImageView>(com.google.android.material.R.id.navigation_bar_item_icon_view)
        val layoutParams = iconView.layoutParams
        layoutParams.height = dpToPx(size)
        layoutParams.width = dpToPx(size)
        iconView.layoutParams = layoutParams
    }

    private fun dpToPx(dp: Float): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
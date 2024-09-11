package net.opendasharchive.openarchive.features.main

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.MenuBaseAdapter
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import com.skydoves.powermenu.kotlin.createPowerMenu
import net.opendasharchive.openarchive.R

class IconPowerMenuItem(val icon: Drawable, val title: String)

class IconMenuAdapter : MenuBaseAdapter<IconPowerMenuItem?>() {
    override fun getView(index: Int, view: View?, viewGroup: ViewGroup): View {
        var view: View? = view
        val context = viewGroup.context

        if (view == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.prefs_title, viewGroup, false)
        }

        val item = getItem(index) as IconPowerMenuItem

//        val icon: ImageView = view.findViewById(R.id.)
//        icon.setImageDrawable(item.getIcon())

        val title: TextView = view!!.findViewById(R.id.title)
        title.text = item.title

        return super.getView(index, view, viewGroup)
    }
}

class FolderContextMenuFactory : PowerMenu.Factory() {
    override fun create(context: Context, lifecycle: LifecycleOwner): PowerMenu {
        return createPowerMenu(context) {
            addItem(PowerMenuItem("Edit", false))
            addItem(PowerMenuItem("Remove", false))
            setAutoDismiss(true)
            setLifecycleOwner(lifecycle)
            setAnimation(MenuAnimation.FADE)
            setTextColor(ContextCompat.getColor(context, R.color.c23_grey))
            setTextSize(16)
            setTextGravity(Gravity.START)
//            setTextTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL))
            setSelectedTextColor(Color.RED)
            setMenuColor(Color.WHITE)
            setInitializeRule(Lifecycle.Event.ON_CREATE, 0)
        }
    }
}
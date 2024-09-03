package net.opendasharchive.openarchive.features.main.ui.powermenu

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.skydoves.powermenu.MenuBaseAdapter
import net.opendasharchive.openarchive.R

//class IconMenuAdapter : MenuBaseAdapter<IconPowerMenuItem?>() {
//    override fun getView(index: Int, view: View?, viewGroup: ViewGroup): View {
//        var view: View? = view
//        val context: Context = viewGroup.context
//
//        if (view == null) {
//            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//            view = inflater.inflate(R.layout.item_icon_menu, viewGroup, false)
//        }
//
//        val item = getItem(index) as IconPowerMenuItem
//
//        val icon: ImageView = view.findViewById(R.id.item_icon)
//        icon.setImageDrawable(item.getIcon())
//
//        val title: TextView = view.findViewById(R.id.item_title)
//        title.setText(item.title)
//
//        return super.getView(index, view, viewGroup)
//    }
//}
package net.opendasharchive.openarchive.features.main.ui

//import android.content.Context
//import android.graphics.Color
//import android.graphics.drawable.ColorDrawable
//import android.view.Gravity
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.appcompat.app.AlertDialog
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import net.opendasharchive.openarchive.R
//
//class CustomPopupMenuDialog(
//    private val context: Context,
//    private val items: List<MenuItem>,
//    private val onItemClick: (MenuItem) -> Unit
//) {
//    data class MenuItem(val icon: Int, val text: String)
//
//    private var dialog: AlertDialog? = null
//
//    fun show(anchorView: View) {
//        val builder = AlertDialog.Builder(context)
//        val inflater = LayoutInflater.from(context)
//        val dialogView = inflater.inflate(R.layout.dialog_custom_menu, null)
//        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.menuRecyclerView)
//
//        recyclerView.layoutManager = LinearLayoutManager(context)
//        val adapter = MenuAdapter(items, onItemClick)
//        recyclerView.adapter = adapter
//
//        builder.setView(dialogView)
//        dialog = builder.create()
//
//        dialog?.window?.let { window ->
//            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//            window.attributes.gravity = Gravity.TOP or Gravity.START
//            val location = IntArray(2)
//            anchorView.getLocationOnScreen(location)
//            window.attributes.x = location[0]
//            window.attributes.y = location[1] + anchorView.height
//        }
//
//        dialog?.show()
//    }
//
//    fun dismiss() {
//        dialog?.dismiss()
//    }
//
//    private inner class MenuAdapter(
//        private val items: List<MenuItem>,
//        private val onItemClick: (MenuItem) -> Unit
//    ) : RecyclerView.Adapter<MenuAdapter.ViewHolder>() {
//
//        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//            private val iconView: ImageView = itemView.findViewById(R.id.icon)
//            private val textView: TextView = itemView.findViewById(R.id.text)
//
//            fun bind(item: MenuItem) {
//                iconView.setImageResource(item.icon)
//                textView.text = item.text
//                itemView.setOnClickListener {
//                    onItemClick(item)
//                    dismiss()
//                }
//            }
//        }
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//            val view = LayoutInflater.from(parent.context)
//                .inflate(R.layout.menu_item_row, parent, false)
//            return ViewHolder(view)
//        }
//
//        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//            holder.bind(items[position])
//        }
//
//        override fun getItemCount() = items.size
//    }
//}
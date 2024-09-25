package net.opendasharchive.openarchive.features.main.ui

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridSectionLayoutDecoration(
    private val headerBottomMargin: Int,
    private val gridTopMargin: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val adapter = parent.adapter as GridSectionAdapter

        // Reset
        outRect.set(0, 0, 0, 0)

        when {
            adapter.getItemViewType(position) == GridSectionAdapter.VIEW_TYPE_HEADER -> {
                outRect.bottom = headerBottomMargin
            }
            position > 0 && adapter.getItemViewType(position - 1) == GridSectionAdapter.VIEW_TYPE_HEADER -> {
                // Add top margin to the first item after a header
                outRect.top = gridTopMargin
            }
        }
    }
}
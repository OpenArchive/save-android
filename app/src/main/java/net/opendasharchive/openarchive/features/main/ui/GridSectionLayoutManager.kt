package net.opendasharchive.openarchive.features.main.ui

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager

class SectionedGridLayoutManager(
    context: Context,
    private val spanCount: Int,
    private val sectionedAdapter: GridSectionAdapter
) : GridLayoutManager(context, spanCount) {

    init {
        spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (sectionedAdapter.getItemViewType(position)) {
                    GridSectionAdapter.VIEW_TYPE_HEADER -> spanCount
                    else -> 1
                }
            }
        }
    }
}
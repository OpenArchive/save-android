package net.opendasharchive.openarchive.features.settings

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import net.opendasharchive.openarchive.R

class FooterPreference(context: Context, attrs: AttributeSet): Preference(context, attrs) {

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder.itemView.findViewById<Button>(R.id.ok_button)?.setOnClickListener {

        }
    }
}
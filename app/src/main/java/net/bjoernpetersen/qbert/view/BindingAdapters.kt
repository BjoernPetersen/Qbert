package net.bjoernpetersen.qbert.view

import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.databinding.BindingAdapter

object BindingAdapters {
    @JvmStatic
    @BindingAdapter("visibleIf")
    fun showGone(view: View, show: Boolean) {
        view.visibility = if (show) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("visibleHideIf")
    fun showHide(view: View, show: Boolean) {
        view.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }

    @JvmStatic
    @BindingAdapter("textId")
    fun setText(textView: TextView, @StringRes stringId: Int) {
        textView.text = if (stringId == 0) null else textView.resources.getText(stringId)
    }
}

package ru.geekbrains.android2.crosszerogame.utils

import android.content.res.Resources
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import ru.geekbrains.android2.crosszerogame.R
import ru.geekbrains.android2.crosszerogame.data.Gamer

/**
 * Adds an extra action button to this snackbar.
 * [aLayoutId] must be a layout with a Button as root element.
 * [aLabel] defines new button label string.
 * [aListener] handles our new button click event.
 */
fun Snackbar.addAction(
    @LayoutRes aLayoutId: Int,
    @StringRes aLabel: Int,
    aListener: View.OnClickListener?
): Snackbar {
    addAction(aLayoutId, context.getString(aLabel), aListener)
    return this
}

/**
 * Adds an extra action button to this snackbar.
 * [aLayoutId] must be a layout with a Button as root element.
 * [aLabel] defines new button label string.
 * [aListener] handles our new button click event.
 */
fun Snackbar.addAction(
    @LayoutRes aLayoutId: Int,
    aLabel: String,
    aListener: View.OnClickListener?
): Snackbar {
    // Add our button
    val button = LayoutInflater.from(view.context).inflate(aLayoutId, null) as Button
    // Using our special knowledge of the snackbar action button id we can hook our extra button next to it
    view.findViewById<Button>(R.id.snackbar_action).let {
        // Copy layout
        button.layoutParams = it.layoutParams
        // Copy colors
        (button as? Button)?.setTextColor(it.textColors)
        (it.parent as? ViewGroup)?.addView(button)
    }
    button.text = aLabel
    /** Ideally we should use [Snackbar.dispatchDismiss] instead of [Snackbar.dismiss] though that should do for now */
    //extraView.setOnClickListener {this.dispatchDismiss(BaseCallback.DISMISS_EVENT_ACTION); aListener?.onClick(it)}
    button.setOnClickListener { this.dismiss(); aListener?.onClick(it) }
    return this
}

fun Fragment.setSubtitle(value: String) {
    if (activity == null)
        return
    val act = activity as AppCompatActivity
    act.supportActionBar?.subtitle = value
}

fun Fragment.getTextColor(): Int {
    val typedValue = TypedValue()
    val theme: Resources.Theme = requireContext().theme
    theme.resolveAttribute(R.attr.colorOnSecondary, typedValue, true)
    return typedValue.data
}

fun Gamer.isCross() = chipImageId == 0
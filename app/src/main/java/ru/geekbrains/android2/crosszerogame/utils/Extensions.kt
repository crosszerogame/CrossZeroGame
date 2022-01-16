package ru.geekbrains.android2.crosszerogame.utils

import android.content.res.Resources
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import ru.geekbrains.android2.crosszerogame.R

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
package ru.geekbrains.android2.crosszerogame.utils

import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import ru.geekbrains.android2.crosszerogame.R

class BlinkAnimation(context: Context) : Animation.AnimationListener {
    private lateinit var view: View
    private val animation: Animation by lazy {
        AnimationUtils.loadAnimation(context, R.anim.blink).apply {
            setAnimationListener(this@BlinkAnimation)
        }
    }

    fun start(view: View) {
        this.view = view
        view.startAnimation(animation)
    }

    override fun onAnimationStart(animation: Animation?) {
        view.visibility = View.VISIBLE
    }

    override fun onAnimationEnd(animation: Animation?) {
        view.visibility = View.INVISIBLE
    }

    override fun onAnimationRepeat(animation: Animation?) {
    }
}
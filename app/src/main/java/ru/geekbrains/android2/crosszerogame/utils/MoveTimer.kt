package ru.geekbrains.android2.crosszerogame.utils

import android.os.CountDownTimer

class MoveTimer(private val callback: Callback, private val secForMove: Int): CountDownTimer(secForMove * 1000L, 1000) {
    private var time: Int = secForMove

    interface Callback {
        fun onTime(sec: Int)
        fun onTimeout()
    }

    fun run() {
        cancel()
        time = secForMove
        start()
    }

    override fun onTick(millisUntilFinished: Long) {
        time--
        callback.onTime(time)
    }

    override fun onFinish() {
        callback.onTimeout()
    }
}
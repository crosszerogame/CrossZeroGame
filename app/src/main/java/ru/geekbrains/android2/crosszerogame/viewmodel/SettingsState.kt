package ru.geekbrains.android2.crosszerogame.viewmodel

import ru.geekbrains.android2.crosszerogame.xdata.Gamer

sealed class SettingsState {
    data class Settings(
        val beginAsFirst: Boolean,
        val fieldSize: Int,
        val nick: String,
        val gameLevel: Int
    ) : SettingsState()

    object AvailableNick : SettingsState()
    object UnavailableNick : SettingsState()
    data class Opponents(val opponents: List<Gamer>) : SettingsState()
    data class Error(val error: Throwable) : SettingsState()
}
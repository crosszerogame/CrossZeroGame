package ru.geekbrains.android2.crosszerogame.viewmodel

import ru.geekbrains.android2.crosszerogame.structure.data.Game

sealed class SettingsState {
    data class Settings(
        val beginAsFirst: Boolean,
        val fieldSize: Int,
        val nick: String,
        val gameLevel: Int
    ) : SettingsState()
    object AvailableNick : SettingsState()
    object UnavailableNick : SettingsState()
    data class Games(val games: List<Game>) : SettingsState()
    data class Error(val error: Throwable) : SettingsState()
}
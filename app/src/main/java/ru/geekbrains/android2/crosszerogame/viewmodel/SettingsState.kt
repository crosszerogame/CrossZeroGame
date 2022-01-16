package ru.geekbrains.android2.crosszerogame.viewmodel

import ru.geekbrains.android2.crosszerogame.structure.data.Game

sealed class SettingsState {
    data class Settings(
        val beginAsFirst: Boolean,
        val fieldSize: Int,
        val nick: String,
        val gameLevel: Int,
        val moveTime: Int
    ) : SettingsState()

    data class MoveTime(val time: Int) : SettingsState()
    data class NewNick(val nick: String, val isAvailable: Boolean) : SettingsState()
    data class Games(val games: List<Game>) : SettingsState()
    data class Error(val error: Throwable) : SettingsState()
}
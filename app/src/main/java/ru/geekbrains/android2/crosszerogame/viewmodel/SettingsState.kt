package ru.geekbrains.android2.crosszerogame.viewmodel

import ru.geekbrains.android2.crosszerogame.data.Gamer

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
    data class Opponents(val opponents: List<Gamer>) : SettingsState()
    data class Error(val error: Throwable) : SettingsState()
}
package ru.geekbrains.android2.crosszerogame.viewmodel

import ru.geekbrains.android2.crosszerogame.structure.data.Game

sealed class SettingsState {
    object AvailableNick : SettingsState()
    object UnavailableNick : SettingsState()
    data class Games(val games: List<Game>) : SettingsState()
    data class Error(val error: Throwable) : SettingsState()
}
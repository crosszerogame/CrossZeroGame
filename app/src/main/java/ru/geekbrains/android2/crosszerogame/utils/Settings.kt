package ru.geekbrains.android2.crosszerogame.utils

import ru.geekbrains.android2.crosszerogame.xdata.GameConstants
import ru.geekbrains.android2.crosszerogame.xdata.Gamer

interface Settings {
    fun save()
    fun getBeginAsFirst(): Boolean
    fun setBeginAsFirst(value: Boolean)
    fun getFieldSize(): Int
    fun setFieldSize(value: Int)
    fun getNick(): String
    fun setNick(value: String)
    fun getGameLevel(): GameConstants.GameLevel
    fun setGameLevel(value: GameConstants.GameLevel)
    fun getOnline(): Boolean
    fun setOnline(value: Boolean)
    fun getGamer(): Gamer
    fun setGamer(value: Gamer)
}
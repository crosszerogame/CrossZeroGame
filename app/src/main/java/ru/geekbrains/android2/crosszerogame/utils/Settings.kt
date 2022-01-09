package ru.geekbrains.android2.crosszerogame.utils

interface Settings {
    fun save()
    fun getBeginAsFirst(): Boolean
    fun setBeginAsFirst(value: Boolean)
    fun getFieldSize(): Int
    fun setFieldSize(value: Int)
    fun getNick(): String
    fun setNick(value: String)
    fun getGameLevel(): Int
    fun setGameLevel(value: Int)
}
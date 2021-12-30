package ru.geekbrains.android2.crosszerogame.structure.data

data class Player(
    val id: Int = 0,
    val nick: String,
    val lastTimeActive: Long = 0
)
package ru.geekbrains.android2.crosszerogame.structure.data

data class Player(
    val id: String = "",
    val nick: String,
    var state: State = State.CREATED,
    var moveX: Int = -1,
    var moveY: Int = -1,
    var lastTimeActive: Long = 0
) {
   enum class State {
       CREATED, PLAYING, LEFT
   }
}
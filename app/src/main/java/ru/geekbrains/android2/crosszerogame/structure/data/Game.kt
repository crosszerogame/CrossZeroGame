package ru.geekbrains.android2.crosszerogame.structure.data

data class Game(
    val id: String = "",
    val fieldSize: Int,
    var chipsForWin: Int,
    val level: Int,
    val moveTime: Int,
    var state: State = State.WAIT_OPPONENT,
    var playerCross: Player?,
    var playerZero: Player?
) {
    enum class State {
        WAIT_OPPONENT, CONTINUE, FINISH
    }
}

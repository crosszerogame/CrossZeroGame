package ru.geekbrains.android2.crosszerogame.game.remote

import ru.geekbrains.android2.crosszerogame.game.remote.GameConstants.DOTS_TO_WIN1_SIZE1
import ru.geekbrains.android2.crosszerogame.game.remote.GameConstants.MIN_FIELD_SIZE
import ru.geekbrains.android2.crosszerogame.game.remote.GameConstants.MIN_TIME_FOR_TURN

data class GameRemote(
    var keyGame: String = "",
    var gameFieldSize: Int = MIN_FIELD_SIZE,
    var gameField: Array<Array<GameConstants.CellField>> = Array(1) {
        Array(1) { GameConstants.CellField.EMPTY }
    },
    var motionXIndex: Int = -1,
    var motionYIndex: Int = -1,
    var gameStatus: GameConstants.GameStatus = GameConstants.GameStatus.NEW_GAME,
    var dotsToWin: Int = DOTS_TO_WIN1_SIZE1,
    var turnOfGamer: Boolean = true,
    var timeForTurn: Int = MIN_TIME_FOR_TURN,
    var countOfTurn: Int = 0

) {

    fun revertGamerToOpponent() {
        turnOfGamer = !turnOfGamer
        gameStatus = when (gameStatus) {
            GameConstants.GameStatus.WIN_GAMER -> GameConstants.GameStatus.WIN_OPPONENT
            GameConstants.GameStatus.WIN_OPPONENT -> GameConstants.GameStatus.WIN_GAMER
            GameConstants.GameStatus.NEW_GAME_FIRST_GAMER -> GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT
            GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT -> GameConstants.GameStatus.NEW_GAME_FIRST_GAMER
            else -> gameStatus
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameRemote

        if (gameFieldSize != other.gameFieldSize) return false
        if (!gameField.contentDeepEquals(other.gameField)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = gameFieldSize
        result = 31 * result + gameField.contentDeepHashCode()
        return result
    }
}

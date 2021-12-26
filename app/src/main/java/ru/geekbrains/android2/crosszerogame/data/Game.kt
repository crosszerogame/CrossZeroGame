package ru.geekbrains.android2.crosszerogame.data

import ru.geekbrains.android2.crosszerogame.data.GameConstants.DOTS_TO_WIN1
import ru.geekbrains.android2.crosszerogame.data.GameConstants.DOTS_TO_WIN1_SIZE1
import ru.geekbrains.android2.crosszerogame.data.GameConstants.DOTS_TO_WIN1_SIZE2
import ru.geekbrains.android2.crosszerogame.data.GameConstants.DOTS_TO_WIN2
import ru.geekbrains.android2.crosszerogame.data.GameConstants.DOTS_TO_WIN2_SIZE1
import ru.geekbrains.android2.crosszerogame.data.GameConstants.DOTS_TO_WIN2_SIZE2
import ru.geekbrains.android2.crosszerogame.data.GameConstants.DOTS_TO_WIN3
import ru.geekbrains.android2.crosszerogame.data.GameConstants.DOTS_TO_WIN3_SIZE1
import ru.geekbrains.android2.crosszerogame.data.GameConstants.DOTS_TO_WIN3_SIZE2
import ru.geekbrains.android2.crosszerogame.data.GameConstants.MAX_FIELD_SIZE
import ru.geekbrains.android2.crosszerogame.data.GameConstants.MIN_FIELD_SIZE
import ru.geekbrains.android2.crosszerogame.data.GameConstants.MIN_TIME_FOR_TURN

data class Game(
    var keyGame: String = "",
    var gameFieldSize: Int = MIN_FIELD_SIZE,
    var gameField: Array<Array<GameConstants.CellField>> = Array(gameFieldSize) { Array(gameFieldSize) { GameConstants.CellField.EMPTY } },
    var motionXIndex: Int = -1,
    var motionYIndex: Int = -1,
    var gameStatus: GameConstants.GameStatus = GameConstants.GameStatus.NEW_GAME,
    var dotsToWin: Int = DOTS_TO_WIN1_SIZE1,
    var turnOfGamer: Boolean = true,
    var timeForTurn: Int = MIN_TIME_FOR_TURN,
    var countOfTurn:Int=0

) {
    init {
 //       if (motionXIndex in MIN_FIELD_SIZE..MAX_FIELD_SIZE
//            && motionYIndex in MIN_FIELD_SIZE..MAX_FIELD_SIZE
//        )
//            gameField[motionYIndex][motionXIndex] =
//                if (turnOfGamer) CellField.GAMER else CellField.OPPONENT

        if (gameFieldSize !in MIN_FIELD_SIZE..MAX_FIELD_SIZE)
            gameFieldSize = MIN_FIELD_SIZE

        dotsToWin = when (gameFieldSize) {
            in DOTS_TO_WIN1_SIZE1..DOTS_TO_WIN1_SIZE2 -> DOTS_TO_WIN1
            in DOTS_TO_WIN2_SIZE1..DOTS_TO_WIN2_SIZE2 -> DOTS_TO_WIN2
            in DOTS_TO_WIN3_SIZE1..DOTS_TO_WIN3_SIZE2 -> DOTS_TO_WIN3
            else -> 0
        }

    }

    fun revertGamerToOpponent() {
//        for ((j, arrCell) in gameField.withIndex()) {
//            for ((i,cell) in arrCell.withIndex()) if (cell == CellField.GAMER) gameField[j][i]= CellField.OPPONENT
//            else if (cell == CellField.OPPONENT) gameField[j][i]=CellField.GAMER
//        }
        turnOfGamer = !turnOfGamer
        gameStatus = when (gameStatus) {
            GameConstants.GameStatus.WIN_GAMER -> GameConstants.GameStatus.WIN_OPPONENT
            GameConstants.GameStatus.WIN_OPPONENT -> GameConstants.GameStatus.WIN_GAMER
            GameConstants.GameStatus.NEW_GAME_FIRST_GAMER -> GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT
            GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT -> GameConstants.GameStatus.NEW_GAME_FIRST_GAMER
            else -> gameStatus
        }
    }

    fun revertField() {
        for ((j, arrCell) in gameField.withIndex()) {
            for ((i, cell) in arrCell.withIndex()) if (cell == GameConstants.CellField.GAMER) gameField[j][i] =
                GameConstants.CellField.OPPONENT
            else if (cell == GameConstants.CellField.OPPONENT) gameField[j][i] = GameConstants.CellField.GAMER
        }
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Game

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


//enum class CellField {
//    GAMER, OPPONENT, EMPTY
//}

//enum class GameStatus {
//    GAME_IS_ON,
//    WIN_GAMER,
//    WIN_OPPONENT,
//    DRAWN_GAME,
//    ABORTED_GAME,
//    NEW_GAME,
//    NEW_GAME_FIRST_GAMER,
//    NEW_GAME_FIRST_OPPONENT,
//    NEW_GAME_ACCEPT
//
//}
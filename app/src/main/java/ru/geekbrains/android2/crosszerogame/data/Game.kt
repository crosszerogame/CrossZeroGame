package ru.geekbrains.android2.crosszerogame.data

import ru.geekbrains.android2.crosszerogame.data.ai.ArtIntelligence.Companion.DOTS_TO_WIN1
import ru.geekbrains.android2.crosszerogame.data.ai.ArtIntelligence.Companion.DOTS_TO_WIN1_SIZE1
import ru.geekbrains.android2.crosszerogame.data.ai.ArtIntelligence.Companion.DOTS_TO_WIN1_SIZE2
import ru.geekbrains.android2.crosszerogame.data.ai.ArtIntelligence.Companion.DOTS_TO_WIN2
import ru.geekbrains.android2.crosszerogame.data.ai.ArtIntelligence.Companion.DOTS_TO_WIN2_SIZE1
import ru.geekbrains.android2.crosszerogame.data.ai.ArtIntelligence.Companion.DOTS_TO_WIN2_SIZE2
import ru.geekbrains.android2.crosszerogame.data.ai.ArtIntelligence.Companion.DOTS_TO_WIN3
import ru.geekbrains.android2.crosszerogame.data.ai.ArtIntelligence.Companion.DOTS_TO_WIN3_SIZE1
import ru.geekbrains.android2.crosszerogame.data.ai.ArtIntelligence.Companion.DOTS_TO_WIN3_SIZE2
import ru.geekbrains.android2.crosszerogame.data.ai.ArtIntelligence.Companion.MAX_FIELD_SIZE
import ru.geekbrains.android2.crosszerogame.data.ai.ArtIntelligence.Companion.MIN_FIELD_SIZE

data class Game(
    val gameFieldSize: Int = MIN_FIELD_SIZE,
    var gameField: Array<Array<CellField>> = Array(gameFieldSize) { Array(gameFieldSize) { CellField.EMPTY } },
    var motionXIndex: Int = -1,
    var motionYIndex: Int = -1,
    var gameStatus: GameStatus = GameStatus.NEW_GAME,
    var dotsToWin: Int = DOTS_TO_WIN1_SIZE1,
    var turnOfGamer: Boolean = true
) {
    init {
        if (motionXIndex in MIN_FIELD_SIZE..MAX_FIELD_SIZE
            && motionYIndex in MIN_FIELD_SIZE..MAX_FIELD_SIZE
        )
            gameField[motionYIndex][motionXIndex] =
                if (turnOfGamer) CellField.GAMER else CellField.OPPONENT

        dotsToWin = when (gameFieldSize) {
            in DOTS_TO_WIN1_SIZE1..DOTS_TO_WIN1_SIZE2 -> DOTS_TO_WIN1
            in DOTS_TO_WIN2_SIZE1..DOTS_TO_WIN2_SIZE2 -> DOTS_TO_WIN2
            in DOTS_TO_WIN3_SIZE1..DOTS_TO_WIN3_SIZE2 -> DOTS_TO_WIN3
            else -> 0
        }

    }

    fun convertGamerToOpponent() {
        for (arrCell in gameField) {
            for (cell in arrCell) if (cell == CellField.GAMER) CellField.OPPONENT
            else if (cell == CellField.OPPONENT) CellField.GAMER
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


enum class CellField {
    GAMER, OPPONENT, EMPTY
}

enum class GameStatus {
    GAME_IS_ON,
    WIN_GAMER,
    WIN_OPPONENT,
    DRAWN_GAME,
    ABORTED_GAME,
    NEW_GAME,
    NEW_GAME_FIRST_GAMER,
    NEW_GAME_FIRST_OPPONENT

}
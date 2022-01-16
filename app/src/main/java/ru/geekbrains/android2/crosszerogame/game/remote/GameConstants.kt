package ru.geekbrains.android2.crosszerogame.game.remote

object GameConstants {
    const val MIN_FIELD_SIZE = 3
    const val DOTS_TO_WIN1_SIZE1 = 3
    const val MIN_TIME_FOR_TURN = 10
    const val REFRESH_INTERVAL_MS_GET_OPPONENT = 2000L
    const val REFRESH_INTERVAL_MS_GAME = 1000L

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
        NEW_GAME_FIRST_OPPONENT,
        NEW_GAME_ACCEPT
    }
}
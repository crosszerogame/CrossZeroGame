package ru.geekbrains.android2.crosszerogame.xdata

object GameConstants {
    const val MAX_FIELD_SIZE = 30
    const val MIN_FIELD_SIZE = 3
    const val DOTS_TO_WIN1_SIZE1 = 3
    const val DOTS_TO_WIN1_SIZE2 = 4
    const val DOTS_TO_WIN2_SIZE1 = 5
    const val DOTS_TO_WIN2_SIZE2 = 6
    const val DOTS_TO_WIN3_SIZE1 = 7
    const val DOTS_TO_WIN3_SIZE2 = 30
    const val DOTS_TO_WIN1 = 3
    const val DOTS_TO_WIN2 = 4
    const val DOTS_TO_WIN3 = 5
    const val MIN_TIME_FOR_TURN = 10
    const val MAX_TIME_FOR_TURN = 120
    const val REFRESH_INTERVAL_MS_GET_OPPONENT = 2000L
    const val REFRESH_INTERVAL_MS_GAME = 1000L
    const val REFRESH_INTERVAL_MS_OPPONENTS_LIST = 3000L
    const val MAX_ATTEMPTS_PUT_DATA_TO_SERVER = 10
    const val CLASS_GAMER = "Gamer"
    const val CLASS_GAME = "Game"
    val DEFAULT_LEVEL_GAMER = GameLevel.LOW
    const val DEFAULT_NICK_GAMER = "Gamer"
    const val DEFAULT_IS_FIRST = true
    const val DEFAULT_FIELD_SIZE = MIN_FIELD_SIZE
    const val DEFAULT_TIME_FOR_TURN = MIN_TIME_FOR_TURN

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

    enum class GameLevel {
        LOW,
        MIDDLE,
        HIGH
    }
}
package ru.geekbrains.android2.crosszerogame.data

import ru.geekbrains.android2.crosszerogame.data.ai.ArtIntelligence

data class Gamer(
    var keyGamer: String = "",
    val nikGamer: String = "gamer1",
    var gameFieldSize: Int = GameConstants.MIN_FIELD_SIZE,
    var levelGamer: Int = 999,
    var chipImageId: Int = 0,
    var timeForTurn: Int = GameConstants.MIN_TIME_FOR_TURN,
    var keyOpponent: String = "",
    var keyGame: String = "updated",
    var isOnLine:Boolean = false,
    var spareVariable1: String = "spare1_updated",
    var spareVariable2: String = "spare2_updated",
    var spareVariable3: String = "spare3_updated"
) {
    init {
        if (gameFieldSize !in ArtIntelligence.MIN_FIELD_SIZE..ArtIntelligence.MAX_FIELD_SIZE) gameFieldSize =
            ArtIntelligence.MIN_FIELD_SIZE
    }
}

/*

    var keyGamer: String = "",
    val nikGamer: String = "gamer",
    var gameFieldSize: Int = GameConstants.MIN_FIELD_SIZE,
    var levelGamer: Int = 1,
    var chipImageId: Int = 0,
    var timeForTurn: Int = GameConstants.MIN_TIME_FOR_TURN,
    var keyOpponent: String = "",
    var keyGame: String = "",
    var isOnLine:Boolean = false
 */
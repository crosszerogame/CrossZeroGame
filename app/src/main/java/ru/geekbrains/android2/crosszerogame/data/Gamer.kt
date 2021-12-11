package ru.geekbrains.android2.crosszerogame.data

import ru.geekbrains.android2.crosszerogame.data.ai.ArtIntelligence

data class Gamer(
    val keyGamer: Int = 1,
    val nikGamer: String = "gamer",
    var gameFieldSize: Int = 3,
    var levelGamer: Int = 1,
    var chipImageId: Int = 0
) {
    init {
        if (gameFieldSize !in ArtIntelligence.MIN_FIELD_SIZE..ArtIntelligence.MAX_FIELD_SIZE) gameFieldSize =
            ArtIntelligence.MIN_FIELD_SIZE
    }
}
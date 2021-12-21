package ru.geekbrains.android2.crosszerogame.model.localdb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games_table")
data class GameEntity(
    @PrimaryKey
    var keyGame: String,
    var gameFieldSize: Int,
    var gameField: String,
    var motionXIndex: Int,
    var motionYIndex: Int,
    var gameStatus: String,
    var dotsToWin: Int,
    var turnOfGamer: Boolean,
    var timeForTurn: Int
)




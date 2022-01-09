package ru.geekbrains.android2.crosszerogame.model.localdb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gamers_table")
data class GamerEntity(
    @PrimaryKey
    var keyGamer: String,
    var nikGamer: String,
    var gameFieldSize: Int,
    var levelGamer: Int,
    var chipImageId: Int,
    var timeForTurn: Int,
    var keyOpponent: String,
    var keyGame: String,
    var isOnLine:Boolean
)



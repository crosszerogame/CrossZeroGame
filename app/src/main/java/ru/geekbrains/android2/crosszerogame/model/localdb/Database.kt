package ru.geekbrains.android2.crosszerogame.model.localdb

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [GameEntity::class, GamerEntity::class], version = 1)
abstract class Database : RoomDatabase() {
    abstract fun gamerDao(): GamerDao
    abstract fun gameDao(): GameDao
}
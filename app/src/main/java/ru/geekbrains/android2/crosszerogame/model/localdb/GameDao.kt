package ru.geekbrains.android2.crosszerogame.model.localdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single

@Dao
interface GameDao {

    @Insert
    fun insertGamer(gameEntity: GameEntity)

    @Delete
    fun deleteGamer(gameEntity: GameEntity)

    @Query("SELECT * FROM games_table")
    fun getAllGamers(): Single<List<GameEntity>>

    @Query("DELETE FROM games_table")
    fun deleteAllGamers(): Completable

    @Query("SELECT * FROM games_table WHERE keyGame=:keyGame")
    fun getGameByKeyGame(keyGame: String): Maybe<GameEntity>
}


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
    fun insertGame(gameEntity: GameEntity)

    @Delete
    fun deleteGame(gameEntity: GameEntity)

    @Query("SELECT * FROM games_table")
    fun getAllGames(): Single<List<GameEntity>>

    @Query("DELETE FROM games_table")
    fun deleteAllGames(): Completable

    @Query("SELECT * FROM games_table WHERE keyGame=:keyGame")
    fun getGameByKeyGame(keyGame: String): Single<GameEntity>
}


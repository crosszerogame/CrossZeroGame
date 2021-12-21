package ru.geekbrains.android2.crosszerogame.model.localdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single

@Dao
interface GamerDao {

    @Insert
    fun insertGamer(gamerEntity: GamerEntity)

    @Delete
    fun deleteGamer(gamerEntity: GamerEntity)

    @Query("SELECT * FROM gamers_table")
    fun getAllGamers() : Single<List<GamerEntity>>

    @Query("DELETE FROM gamers_table")
    fun deleteAllGamers(): Completable

    @Query("SELECT * FROM gamers_table WHERE keyGamer=:keyGamer")
    fun getGamerByKeyGamer(keyGamer: String): Maybe<GamerEntity>

}


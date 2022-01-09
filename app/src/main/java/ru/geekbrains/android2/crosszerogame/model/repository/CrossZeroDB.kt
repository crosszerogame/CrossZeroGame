package ru.geekbrains.android2.crosszerogame.model.repository

import io.reactivex.rxjava3.core.Single
import ru.geekbrains.android2.crosszerogame.data.Game
import ru.geekbrains.android2.crosszerogame.data.Gamer
import ru.geekbrains.android2.crosszerogame.model.localdb.GameEntity
import ru.geekbrains.android2.crosszerogame.model.localdb.GamerEntity

interface CrossZeroDB {

    fun insertGamer(gamer: Gamer)
    fun updateGamer(gamer: Gamer)
    fun deleteGamer(gamer: Gamer)
    fun deleteAllGamers()
    fun getGamer(key: String): Single<GamerEntity>
    fun getListOfGamers(): Single<List<GamerEntity>>

    fun insertGame(game: Game)
    fun updateGame(game: Game)
    fun deleteGame(game: Game)
    fun deleteAllGames()
    fun getGame(key: String): Single<GameEntity>
    fun getListOfGames(): Single<List<GameEntity>>

}
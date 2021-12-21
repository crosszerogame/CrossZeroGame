package ru.geekbrains.android2.crosszerogame.model.repository

import android.util.Log
import io.reactivex.rxjava3.core.Completable
import ru.geekbrains.android2.crosszerogame.data.Game
import ru.geekbrains.android2.crosszerogame.data.Gamer
import ru.geekbrains.android2.crosszerogame.model.CrossZeroDB
import ru.geekbrains.android2.crosszerogame.model.localdb.GameDao
import ru.geekbrains.android2.crosszerogame.model.localdb.GamerDao
import io.reactivex.rxjava3.schedulers.Schedulers
import ru.geekbrains.android2.crosszerogame.model.localdb.GamerEntity

class Repo(
    private val gameDao: GameDao,
    private val gamerDao: GamerDao
) : CrossZeroDB {

    fun convertToGamerEntity(gamer: Gamer) =
        GamerEntity(
            gamer.nikGamer,
            gamer.nikGamer,
            gamer.gameFieldSize,
            gamer.levelGamer,
            gamer.chipImageId,
            "gamer.timeForTurn".toInt(),
            "gamer.keyOpponent",
            "gamer.keyGame",
            true
        )

    override fun insGamer(gamer: Gamer) {
        val _gamer = convertToGamerEntity(gamer)
        gamerDao.getAllGamers().subscribeOn(Schedulers.io())
            .subscribe({ repos ->
                println(repos)
                if (!repos.contains(_gamer)) {
                    Completable.fromRunnable {
                        gamerDao.insertGamer(_gamer)
                    }.subscribeOn(Schedulers.io()).subscribe()
                    Log.d("${_gamer.nikGamer} status", "Added")
                }
            }, {
                Log.d("Error: ", it.message!!)
            })
    }

    override fun updGamer(gamer: Gamer) {
        val _gamer = convertToGamerEntity(gamer)
        gamerDao.getGamerByKeyGamer(_gamer.keyGamer).subscribeOn(Schedulers.io())
            .subscribe({ repos ->
                println(repos)
                if (repos != null) {
                    Completable.fromRunnable{
                        gamerDao.deleteGamer(repos)
                    }.subscribeOn(Schedulers.io()).subscribe()
                    Completable.fromRunnable {
                        gamerDao.insertGamer(_gamer)
                    }.subscribeOn(Schedulers.io()).subscribe()
                    Log.d("${_gamer.nikGamer} status", "Updated")
                }
            }, {
                Log.d("Error: ", it.message!!)
            })
    }


}










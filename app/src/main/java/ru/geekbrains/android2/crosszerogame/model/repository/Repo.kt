package ru.geekbrains.android2.crosszerogame.model.repository

import android.util.Log
import com.parse.ParseObject
import com.parse.ParseQuery
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import ru.geekbrains.android2.crosszerogame.data.Game
import ru.geekbrains.android2.crosszerogame.data.Gamer
import ru.geekbrains.android2.crosszerogame.model.localdb.GameDao
import ru.geekbrains.android2.crosszerogame.model.localdb.GamerDao
import io.reactivex.rxjava3.schedulers.Schedulers
import ru.geekbrains.android2.crosszerogame.model.*
import ru.geekbrains.android2.crosszerogame.model.localdb.GameEntity
import ru.geekbrains.android2.crosszerogame.model.localdb.GamerEntity

class Repo(
    private val gameDao: GameDao,
    private val gamerDao: GamerDao
) : ObjectOperations(), CrossZeroDB, ServerOperations {

    private val columnName = "column"

    override fun insertGamer(gamer: Gamer) {
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

    override fun updateGamer(gamer: Gamer) {
        val _gamer = convertToGamerEntity(gamer)
        gamerDao.getGamerByKeyGamer(_gamer.keyGamer).subscribeOn(Schedulers.io())
            .subscribe({ repos ->
                println(repos)
                if (repos != null) {
                    Completable.fromRunnable {
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

    override fun deleteGamer(gamer: Gamer) {
        val _gamer = convertToGamerEntity(gamer)
        gamerDao.getAllGamers().subscribeOn(Schedulers.io())
            .subscribe({ repos ->
                println(repos)
                if (repos.contains(_gamer)) {
                    Completable.fromRunnable {
                        gamerDao.deleteGamer(_gamer)
                    }.subscribeOn(Schedulers.io()).subscribe()
                    Log.d("${_gamer.nikGamer} status", "Added")
                }
            }, {
                Log.d("Error: ", it.message!!)
            })
    }

    override fun deleteAllGamers() {
        Completable.fromRunnable {
            gamerDao.deleteAllGamers()
        }.subscribeOn(Schedulers.io()).subscribe()
    }

    override fun getGamer(key: String): Single<GamerEntity> =
        gamerDao.getGamerByKeyGamer(key)

    override fun getListOfGamers(): Single<List<GamerEntity>> =
        gamerDao.getAllGamers()

    override fun insertGame(game: Game) {
        val _game = convertToGameEntity(game)
        gameDao.getAllGames().subscribeOn(Schedulers.io())
            .subscribe({ repos ->
                println(repos)
                if (!repos.contains(_game)) {
                    Completable.fromRunnable {
                        gameDao.insertGame(_game)
                    }.subscribeOn(Schedulers.io()).subscribe()
                    Log.d("status", "Added")
                }
            }, {
                Log.d("Error: ", it.message!!)
            })
    }

    override fun updateGame(game: Game) {
        val _game = convertToGameEntity(game)
        gameDao.getGameByKeyGame(_game.keyGame).subscribeOn(Schedulers.io())
            .subscribe({ repos ->
                if (repos != null) {
                    Completable.fromRunnable {
                        gameDao.deleteGame(repos)
                    }.subscribeOn(Schedulers.io()).subscribe()
                    Completable.fromRunnable {
                        gameDao.insertGame(_game)
                    }.subscribeOn(Schedulers.io()).subscribe()
                    Log.d("status", "Updated")
                }
            }, {
                Log.d("Error: ", it.message!!)
            })
    }

    override fun deleteGame(game: Game) {

        val _game = convertToGameEntity(game)

        gameDao.getAllGames().subscribeOn(Schedulers.io())
            .subscribe({ repos ->
                println(repos)
                if (repos.contains(_game)) {
                    Completable.fromRunnable {
                        gameDao.deleteGame(_game)
                    }.subscribeOn(Schedulers.io()).subscribe()
                    Log.d(" status", "Added")
                }
            }, {
                Log.d("Error: ", it.message!!)
            })

    }

    override fun deleteAllGames() {
        gameDao.deleteAllGames()
    }

    override fun getGame(key: String): Single<GameEntity> =
        gameDao.getGameByKeyGame(key)

    override fun getListOfGames(): Single<List<GameEntity>> =
        gameDao.getAllGames()

    override fun insertVariableOnServer(variableName: String, variableValue: String) {
        val varName = "var$variableName"
        val query = ParseQuery.getQuery<ParseObject>(varName)
        query.orderByDescending("createdAt")
        query.findInBackground { objects, e ->
            if (e == null) {
                try {
                    updateObject(varName, objects[0].objectId, columnName, variableValue)
                } catch (e: Exception) {
                    e.printStackTrace()
                    createObject(varName, columnName, variableValue)
                }
            } else {
                Log.d("Error", e.message!!)
            }
        }
    }

    override fun getVariableFromServer(variableName: String): ParseQuery<ParseObject>? {
        val varName = "var$variableName"
        val query = ParseQuery.getQuery<ParseObject>(varName)
        return query

        /*
        query.orderByDescending("createdAt")
        query.findInBackground { objects, e ->
            if (e == null) {

            } else {
                Log.d("Error", e.message!!)
            }
        }
        */

    }

    override fun deleteVariableFromServer(variableName: String) {
        val varName = "var$variableName"
        val firstObject = ParseObject(varName)
        firstObject.deleteInBackground {
            if (it == null) {
                Log.d("Status", "Deleted")
            } else {
                Log.d("Error", it.message!!)
            }
        }
    }

    override fun insertTableToServer(tableName: String, list: List<ListData>) {
        val firstObject = ParseObject(tableName)
        for (i in list.indices) {
            firstObject.put(list[i].columnName, list[i].data)
            firstObject.saveInBackground {
                if (it != null) {
                    it.localizedMessage?.let { message -> Log.e("MainActivity", message) }
                } else {
                    Log.d("MainActivity", "Object saved.")
                }
            }
        }
    }

    override fun updateTableOnServer(tableName: String, list: List<ListData>) {
        deleteTableFromServer(tableName)
        insertTableToServer(tableName, list)
    }

    override fun deleteTableFromServer(tableName: String) {
        val firstObject = ParseObject(tableName)
        firstObject.deleteInBackground {
            if (it == null) {
                Log.d("Status", "Deleted")
            } else {
                Log.d("Error", it.message!!)
            }
        }
    }

    override fun getTableFromServer(tableName: String): ParseQuery<ParseObject>? {
        val query = ParseQuery.getQuery<ParseObject>(tableName)
        return query

        /*
        query.orderByDescending("createdAt")
        query.findInBackground { objects, e ->
            if (e == null) {
                // TODO как теперь отсюда данные передать в другой класс лучше???
            } else {
                Log.d("Error", e.message!!)
            }
        }
        */
        
    }

}










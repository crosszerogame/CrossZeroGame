package ru.geekbrains.android2.crosszerogame.model.repository

import android.util.Log
import com.parse.ParseException
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
) : ObjectOperations(), ServerOperations {

    override fun getGamersFromServer(): ParseQuery<ParseObject>? =
        ParseQuery.getQuery(gamerClass)

    override fun insertGamerToServer(gamer: Gamer) {
        val list = mutableListOf<String>()
        val query = ParseQuery.getQuery<ParseObject>(gamerClass)
        query.orderByDescending("createdAt")
        query.findInBackground { objects, e ->
            if (e == null) {
                for (i in 0 until objects.size) {
                    list.add(objects[i].get("var_nikGamer").toString())
                }
                for (i in 0 until list.size) {
                    println("list i : ${list[i]}, ${gamer.nikGamer}")
                }
                if (!list.contains(gamer.nikGamer)) {
                    insertGamerServer(gamer)
                    println("Insert")
                }
            } else {
                Log.d("Error", e.message!!)
            }
        }
    }

    override fun initKeyGamer() {
        val list = mutableListOf<Gamer>()
        val query = ParseQuery.getQuery<ParseObject>(gamerClass)
        query.orderByDescending("createdAt")
        query.findInBackground { objects, e ->
            if (e == null) {
                for (i in 0 until objects.size) {
                    list.add(
                        Gamer(
                            objects[i].objectId,
                            objects[i].getString("var_nikGamer")!!,
                            objects[i].getNumber("var_gameFieldSize")!!.toInt(),
                            objects[i].getNumber("var_levelGamer")!!.toInt(),
                            objects[i].getNumber("var_chipImageId")!!.toInt(),
                            objects[i].getNumber("var_timeForTurn")!!.toInt(),
                            objects[i].getString("var_keyOpponent")!!,
                            objects[i].getString("var_keyGame")!!,
                            objects[i].getBoolean("var_isOnLine")
                        )
                    )
                }
                println("list ${list}")
                for (i in 0 until list.size) {
                    updateGamerOnServer(list[i])
                }
            } else {
                Log.d("Error", e.message!!)
            }
        }
    }

    override fun updateGamerOnServer(gamer: Gamer) {
        if (gamer.keyGamer != "") {
            val query = ParseQuery.getQuery<ParseObject>(gamerClass)
            query.getInBackground(
                gamer.keyGamer
            ) { firstObject: ParseObject, e: ParseException? ->
                if (e == null) {
                    firstObject.put("var_keyGamer", gamer.keyGamer)
                    firstObject.put("var_nikGamer", gamer.nikGamer)
                    firstObject.put("var_gameFieldSize", gamer.gameFieldSize)
                    firstObject.put("var_levelGamer", gamer.levelGamer)
                    firstObject.put("var_chipImageId", gamer.chipImageId)
                    firstObject.put("var_timeForTurn", gamer.timeForTurn)
                    firstObject.put("var_keyOpponent", gamer.keyOpponent)
                    firstObject.put("var_keyGame", gamer.keyGame)
                    firstObject.put("var_isOnLine", gamer.isOnLine)

                    firstObject.put("var_spareVariable1", gamer.spareVariable1)
                    firstObject.put("var_spareVariable2", gamer.spareVariable2)
                    firstObject.put("var_spareVariable3", gamer.spareVariable3)

                    firstObject.saveInBackground {
                        if (it != null) {
                            it.localizedMessage?.let { message -> Log.e("MainActivity", message) }
                            it.printStackTrace()
                        } else {
                            Log.d("MainActivity", "Object updated")
                        }
                    }

                } else {
                    e.printStackTrace()
                }
            }
        } else {
            val query = ParseQuery.getQuery<ParseObject>(gamerClass)
            query.orderByDescending("createdAt")
            query.findInBackground { objects, e ->
                if (e == null) {
                    for (i in 0 until objects.size) {
                        println("find nick to update: ${objects[i].get("var_nikGamer")}, my nick : ${gamer.nikGamer}")
                        if (objects[i].get("var_nikGamer").toString() == gamer.nikGamer) {
                            println("found id: ${objects[i].objectId}")
                            val query = ParseQuery.getQuery<ParseObject>(gamerClass)
                            query.getInBackground(
                                objects[i].objectId
                            ) { firstObject: ParseObject, e: ParseException? ->
                                if (e == null) {

                                    println("e == null")

                                    firstObject.put("var_keyGamer", gamer.keyGamer)
                                    firstObject.put("var_nikGamer", gamer.nikGamer)
                                    firstObject.put("var_gameFieldSize", gamer.gameFieldSize)
                                    firstObject.put("var_levelGamer", gamer.levelGamer)
                                    firstObject.put("var_chipImageId", gamer.chipImageId)
                                    firstObject.put("var_timeForTurn", gamer.timeForTurn)
                                    firstObject.put("var_keyOpponent", gamer.keyOpponent)
                                    firstObject.put("var_keyGame", gamer.keyGame)
                                    firstObject.put("var_isOnLine", gamer.isOnLine)

                                    println("gamer.spareVariable1: ${gamer.spareVariable1}")

                                    firstObject.put("var_spareVariable1", gamer.spareVariable1)
                                    firstObject.put("var_spareVariable2", gamer.spareVariable2)
                                    firstObject.put("var_spareVariable3", gamer.spareVariable3)

                                    firstObject.saveInBackground {
                                        if (it != null) {
                                            it.localizedMessage?.let { message -> Log.e("MainActivity", message) }
                                            it.printStackTrace()
                                        } else {
                                            Log.d("MainActivity", "Object updated")
                                        }
                                    }

                                } else {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                } else {
                    Log.d("Error", e.message!!)
                }
            }
        }
    }


    override fun deleteGamerFromServer(gamer: Gamer) {
        if (gamer.keyGamer != "") {
            val query = ParseQuery.getQuery<ParseObject>(gamerClass)
            query.getInBackground(
                gamer.keyGamer
            ) { firstObject: ParseObject, e: ParseException? ->
                if (e == null) {
                    firstObject.deleteInBackground()
                    Log.d("$firstObject status", "Deleted")
                } else {
                    e.printStackTrace()
                }
            }
        } else {
            val query = ParseQuery.getQuery<ParseObject>(gamerClass)
            query.orderByDescending("createdAt")
            query.findInBackground { objects, e ->
                if (e == null) {
                    for (i in 0 until objects.size) {
                        if (objects[i].get("var_nikGamer").toString() == gamer.nikGamer) {
                            val query = ParseQuery.getQuery<ParseObject>(gamerClass)
                            query.getInBackground(
                                objects[i].objectId
                            ) { firstObject: ParseObject, e: ParseException? ->
                                if (e == null) {
                                    firstObject.deleteInBackground()
                                    Log.d("$firstObject status", "Deleted")
                                } else {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                } else {
                    Log.d("Error", e.message!!)
                }
            }
        }
    }

}










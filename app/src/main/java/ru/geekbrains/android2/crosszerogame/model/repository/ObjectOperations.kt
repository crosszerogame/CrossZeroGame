package ru.geekbrains.android2.crosszerogame.model.repository

import android.util.Log
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery
import ru.geekbrains.android2.crosszerogame.data.Gamer
import ru.geekbrains.android2.crosszerogame.model.localdb.GamerEntity

data class VariableData(val variableName: String, val variableValue: String)

data class ListData(val columnName: String, val data: String)

open class ObjectOperations {

    protected val variablesClass = "Variables"

    protected val gamerClass = "Gamer"

    protected val gameClass = "Game"

    protected fun updateObject(
        objectName: String,
        objectId: String,
        variableName: String,
        variableValue: String
    ) {
        val query = ParseQuery.getQuery<ParseObject>(objectName)
        query.getInBackground(
            objectId
        ) { obj: ParseObject, e: ParseException? ->
            if (e == null) {
                obj.put(variableName, variableValue)
                obj.saveInBackground()
            } else {
                e.printStackTrace()
            }
        }
    }

    fun insertGamerServer(gamer: Gamer) {

        println("insertGamerServer start")

        val firstObject = ParseObject(gamerClass)
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
        println("firstObject.objectId ${firstObject.objectId}")

        firstObject.saveInBackground {
            if (it != null) {
                it.localizedMessage?.let { message -> Log.e("MainActivity", message) }
                it.printStackTrace()
            } else {
                Log.d("MainActivity", "Object saved.")
            }
        }
    }

    protected fun updateGamerServer(gamer: Gamer, objectId: String) {
        println("gamer.keyGamer ${gamer.keyGamer}")

            val query = ParseQuery.getQuery<ParseObject>(gamerClass)
            query.getInBackground(
                objectId
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

                } else {
                    e.printStackTrace()
                }


    }
}

fun updateObject(
    objectName: String,
    objectId: String,
    variableName: String,
    variableValue: Int
) {
    val query = ParseQuery.getQuery<ParseObject>(objectName)
    query.getInBackground(
        objectId
    ) { obj: ParseObject, e: ParseException? ->
        if (e == null) {
            obj.put(variableName, variableValue)
            obj.saveInBackground()
        } else {
            e.printStackTrace()
        }
    }
}

fun deleteObject(
    objectName: String,
    objectId: String,
) {
    val query = ParseQuery.getQuery<ParseObject>(objectName)

    query.getInBackground(
        objectId
    ) { obj: ParseObject, e: ParseException? ->
        if (e == null) {
            obj.deleteInBackground()
            Log.d("$obj status", "Deleted")
        } else {
            e.printStackTrace()
        }
    }
}

fun createObject(
    objectName: String,
    variableName: String,
    variableValue: String
) {
    val firstObject = ParseObject(objectName)
    firstObject.put(variableName, variableValue)
    firstObject.saveInBackground {
        if (it != null) {
            it.localizedMessage?.let { message -> Log.e("MainActivity", message) }

        } else {
            Log.d("MainActivity", "Object saved.")
        }
    }
}

fun createObject(
    objectName: String,
    variableName: String,
    variableValue: Int
) {
    val firstObject = ParseObject(objectName)
    firstObject.put(variableName, variableValue)
    firstObject.saveInBackground {
        if (it != null) {
            it.localizedMessage?.let { message -> Log.e("MainActivity", message) }

        } else {
            Log.d("MainActivity", "Object saved.")
        }
    }
}


fun deleteVariableById(
    objects: List<ParseObject>,
    objectName: String,
    variableName: String
) {
    Log.d("getVariableId", "Start")

    Log.d("getVariableId varName", variableName)

    for (i in objects.indices) {
        if (objects[i].get(variableName) != null) {
            deleteObject(objectName, objects[i].objectId)
            return
        }
    }
}


fun getVariableId(objects: List<ParseObject>, variableName: String): String {
    Log.d("getVariableId", "Start")

    Log.d("getVariableId varName", variableName)

    for (i in objects.indices) {
        if (objects[i].get(variableName) != null) {

            return objects[i].objectId
        }
    }
    return "id"
}

fun insert(
    _gamer: GamerEntity,
    variable: String,
    variableValue: String,
    class_: ServerClasses
) {

//        var className = ServerClasses.variablesClass
//
//        when (class_) {
//            ServerClasses.gamerClass -> className = ServerClasses.gamerClass
//            ServerClasses.gameClass -> className = ServerClasses.gameClass
//        }
//
//        val varName = "${_gamer.nikGamer}_${variable}"
//        val query = ParseQuery.getQuery<ParseObject>(className)
//        query.orderByDescending("createdAt")
//        query.findInBackground { objects, e ->
//            if (e == null) {
//                val id = getVariableId(objects, varName)
//                if (id != "id") {
//                    updateObject(className, id, varName, variableValue)
//                } else {
//                    createObject(className, varName, variableValue)
//                }
//            } else {
//                Log.d("Error", e.message!!)
//            }
//        }

}

}



package ru.geekbrains.android2.crosszerogame.model.repository

import android.util.Log
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery
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

    protected fun updateObject(
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

    protected fun deleteObject(
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

    protected fun createObject(
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

    protected fun createObject(
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






    protected fun deleteVariableById(
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


    protected fun getVariableId(objects: List<ParseObject>, variableName: String): String {
        Log.d("getVariableId", "Start")

        Log.d("getVariableId varName", variableName)

        for (i in objects.indices) {
            if (objects[i].get(variableName) != null) {

                return objects[i].objectId
            }
        }
        return "id"
    }

    protected fun insert(
        _gamer: GamerEntity,
        variable: String,
        variableValue: String,
        class_: ServerClasses
    ) {

        var className = variablesClass

        when (class_) {
            ServerClasses.gamerClass -> className = gamerClass
            ServerClasses.gameClass -> className = gameClass
        }

        val varName = "${_gamer.nikGamer}_${variable}"
        val query = ParseQuery.getQuery<ParseObject>(className)
        query.orderByDescending("createdAt")
        query.findInBackground { objects, e ->
            if (e == null) {
                val id = getVariableId(objects, varName)
                if (id != "id") {
                    updateObject(className, id, varName, variableValue)
                } else {
                    createObject(className, varName, variableValue)
                }
            } else {
                Log.d("Error", e.message!!)
            }
        }

    }


}
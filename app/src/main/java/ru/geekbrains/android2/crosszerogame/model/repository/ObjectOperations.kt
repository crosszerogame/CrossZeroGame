package ru.geekbrains.android2.crosszerogame.model.repository

import android.util.Log
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery

data class VariableData(val variableName: String, val variableValue: String)

data class ListData(val columnName: String, val data: String)

open class ObjectOperations {

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


}
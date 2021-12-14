package ru.geekbrains.android2.crosszerogame.model

import android.util.Log
import com.parse.*

class ServerConnection : ServerOperations {

    override fun insertToServer(
        tableName: String,
        columnName: String,
        dataToInsert: String
    ) : String {
        val firstObject = ParseObject(tableName)
        firstObject.put(columnName, dataToInsert)
        firstObject.saveInBackground {
            if (it != null) {
                it.localizedMessage?.let { message -> Log.e("MainActivity", message) }
            } else {
                Log.d("MainActivity", "Object saved.")
            }
        }
        return "ParseObject(tableName).objectId"
    }
    
    override fun getObjectFromTableInServer(tableName: String, objectId: String) {
        val query = ParseQuery.getQuery<ParseObject>(tableName)
        query.getInBackground(
            ParseObject(tableName).objectId
        ) { obj: ParseObject?, e: ParseException? ->
            if (e == null) {
                // TODO
                println("Obj $obj")
                Log.d("Object :", obj.toString())
            } else {
                e.printStackTrace()
            }
        }
    }

    override fun getTableFromServer(tableName: String) {
        val query = ParseQuery.getQuery<ParseObject>(tableName)
        query.orderByDescending("createdAt")
        query.findInBackground { objects, e ->
            if (e == null) {
                println(objects)
                // TODO Отсюда через livedata передам вам данные
            } else {
                Log.d("Error", e.message!!)
            }
        }
    }

    override fun updateOnServer(
        tableName: String,
        objectId: String,
        columnName: String,
        dataToInsert: String
    ) {
        val query = ParseQuery.getQuery<ParseObject>(tableName)
        query.getInBackground(
            ParseObject(tableName).objectId
        ) { obj: ParseObject, e: ParseException? ->
            if (e == null) {
                // Update the fields we want to
                obj.put(columnName, dataToInsert)
                // All other fields will remain the same
                obj.saveInBackground()
            } else {
                e.printStackTrace()
            }
        }
    }

    override fun deleteFromServer(tableName: String) {
        val firstObject = ParseObject(tableName)
        firstObject.deleteInBackground {
            if (it == null) {
                Log.d("Status", "Deleted")
            } else {
                Log.d("Error", it.message!!)
            }

        }
    }

    override fun deleteObjectFromServer(tableName: String, objectId: String) {
        val query = ParseQuery.getQuery<ParseObject>(tableName)
        query.getInBackground(
            ParseObject(tableName).objectId
        ) { obj: ParseObject, e: ParseException? ->
            if (e == null) {
                obj.deleteInBackground { e2: ParseException? ->
                    if (e2 == null) {
                    } else {
                        e2.printStackTrace()
                    }
                }
            } else {
                e.printStackTrace()
            }
        }
    }

}
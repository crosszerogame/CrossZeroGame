package ru.geekbrains.android2.crosszerogame.model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleOnSubscribe

data class VariableData(val variableName: String, val variableValue: String)

data class ListData(val columnName: String, val data: String)

class ServerConnection : ServerOperations {

    val liveDataVariableValue: MutableLiveData<VariableData> = MutableLiveData()

    private val columnName = "column"

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

    override fun getVariableFromServer(variableName: String) {
        val varName = "var$variableName"
        val query = ParseQuery.getQuery<ParseObject>(varName)
        query.orderByDescending("createdAt")
        query.findInBackground { objects, e ->
            if (e == null) {
                println(objects)
                println("getVariableFromServer " + objects[0].get(columnName))
                // https://github.undefined.moe/androiddevnotesyoutube/callback-example-kotlin/tree/main/app
                // TODO как теперь отсюда данные передать в другой класс лучше???
                liveDataVariableValue.postValue(
                    VariableData(
                        variableName,
                        objects[0].get(columnName) as String
                    )
                )
            } else {
                Log.d("Error", e.message!!)
            }
        }
    }

    fun getVariableFromServerRx(variableName: String): Single<VariableData> {

        var obj: Single<VariableData> = Single.create(SingleOnSubscribe {

        })

        val varName = "var$variableName"
        val query = ParseQuery.getQuery<ParseObject>(varName)
        query.orderByDescending("createdAt")


        query.findInBackground { objects, e ->
            if (e == null) {
                println(objects)
                println("getVariableFromServer " + objects[0].get(columnName))
                // TODO как теперь отсюда данные передать в другой класс лучше???

                obj = Single.create(SingleOnSubscribe {

                })

                liveDataVariableValue.postValue(
                    VariableData(
                        variableName,
                        objects[0].get(columnName) as String
                    )
                )
            } else {
                Log.d("Error", e.message!!)
            }
        }
        return obj
    }


//    fun getRx(variableName: String): Single<VariableData> {
//
//        var obj: Single<VariableData> = Single.create(SingleOnSubscribe {
//
//        })
//
//        val varName = "var$variableName"
//        val query = ParseQuery.getQuery<ParseObject>(varName)
//        query.orderByDescending("createdAt")
//        query.findInBackground { objects, e ->
//            if (e == null) {
//                return Single.just(objects)
//                liveDataVariableValue.postValue(
//                    VariableData(
//                        variableName,
//                        objects[0].get(columnName) as String
//                    )
//                )
//            } else {
//                Log.d("Error", e.message!!)
//            }
//        }
//        return obj
//    }


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

    override fun getTableFromServer(tableName: String) {
        val query = ParseQuery.getQuery<ParseObject>(tableName)
        query.orderByDescending("createdAt")
        query.findInBackground { objects, e ->
            if (e == null) {
                // TODO как теперь отсюда данные передать в другой класс лучше???
            } else {
                Log.d("Error", e.message!!)
            }
        }
    }

    private fun updateObject(
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

    private fun createObject(
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
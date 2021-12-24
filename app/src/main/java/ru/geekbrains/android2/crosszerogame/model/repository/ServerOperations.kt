package ru.geekbrains.android2.crosszerogame.model.repository

import com.parse.ParseObject
import com.parse.ParseQuery
import ru.geekbrains.android2.crosszerogame.model.repository.ListData

interface ServerOperations {

    /**
     * Сохраняет переменную на сервере, variableName - имя переменной, variableValue - значение
     * Если вставить переменную, значение variableName которой уже хранится на сервере, то
     * переменная перезапишется на сервере.
     */
    fun insertVariableOnServer(variableName: String, variableValue: String)

    /**
     * Функция получения значения переменной с сервера.
     */
    fun getVariablesFromServer(): ParseQuery<ParseObject>?

    /**
     * Функция удаления переменной на сервере
     */
    fun deleteVariableFromServer(variableName: String)

    /**
     * Функция сохранения таблицы на сервере, где tableName - имя таблицы,
     * а list - список, содержащий в себе data class ListData, в котором
     * находятся 2 переменные: название колонки (columnName) и значение, которое
     * нужно записать в ячейку (data). Если вызвать функцию сохранения данных в одну и ту же
     * таблицу второй раз, то данные БУДУТ ДОБАВЛЕНЫ, А НЕ ОБНОВЛЕНЫ, для обновления есть
     * функция updateTableOnServer(tableName: String, list: List<ListData>)
     */
    fun insertTableToServer(tableName: String, list: List<ListData>)

    /**
     * Обновляет данные в таблице.
     */
    fun updateTableOnServer(tableName: String, list: List<ListData>)

    /**
     * Удаляет таблицу на сервере
     */
    fun deleteTableFromServer(tableName: String)

    /**
     * Функция получения таблицы с сервера
     */
    fun getTableFromServer(tableName: String): ParseQuery<ParseObject>?

}
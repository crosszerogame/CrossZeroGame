package ru.geekbrains.android2.crosszerogame.model

interface ServerOperations {

    /**
     * Сохраняет объект на сервере, для этого необходимо указать название таблицы (tableName),
     * куда необходимо сохранить данные, название колонки (columnName), сами данные (dataToInsert).
     * Пока данные можно сохранять только в формате Строки String, попозже постараюсь и для других типов
     * данных созать возможность сохранения.
     * В А Ж Н О:
     * Обратите внимание, данная функция возвращает данные в формате строки, это id сохранённого объекта,
     * его вам нужно запомнить, чтобы потом можно было рабоать с этим объектом
     */
    fun insertToServer(tableName: String, columnName: String, dataToInsert: String): String

    /**
     * Функция получает всю таблицу по названию с сервера и передаёт её LiveData
     */
    fun getTableFromServer(tableName: String)


    /**
     * Функция обновляет данные в таблицы на сервере.
     */
    fun updateOnServer(
        tableName: String,
        objectId: String,
        columnName: String,
        dataToInsert: String
    )

    /**
     * Функция удаляет всю таблицу на сервере.
     */
    fun deleteFromServer(tableName: String)

    /**
     * Функция удаляет объект в таблице на сервере
     */
    fun deleteObjectFromServer(tableName: String, objectId: String)

    /**
     * Функция получает объект с сервера из таблицы по уникальному id объекта
     */
    fun getObjectFromTableInServer(tableName: String, objectId: String)

}
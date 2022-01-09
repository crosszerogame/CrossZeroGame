package ru.geekbrains.android2.crosszerogame.model.repository

import com.parse.ParseObject
import com.parse.ParseQuery
import ru.geekbrains.android2.crosszerogame.data.Game
import ru.geekbrains.android2.crosszerogame.data.Gamer
import ru.geekbrains.android2.crosszerogame.model.repository.ListData

interface ServerOperations {

    fun getGamersFromServer(): ParseQuery<ParseObject>?

    fun insertGamerToServer(gamer: Gamer)

    /**
     * Функция присвает полю keyGamer значение id объекта, которое изначально
     * не может быть инициализировано, если это функцию не выполнить, то при выполнении
     * функций update и delete алгоритм будет сначала находить objectId объекта и лишь потом
     * рабоать с этим объектом.
     *
     * Пока может работать некорректно, лучше не использовать
     */

    fun initKeyGamer()

    fun updateGamerOnServer(gamer: Gamer)

    fun deleteGamerFromServer(gamer: Gamer)

    fun getGamesFromServer(): ParseQuery<ParseObject>?

    fun insertGameToServer(game: Game)

    fun updateGameOnServer(game: Game)

    fun deleteGameFromServer(game: Game)

    /**
     * Функция присвает полю keyGame значение id объекта, которое изначально
     * не может быть инициализировано, если это функцию не выполнить, то при выполнении
     * функций update и delete алгоритм будет сначала находить objectId объекта и лишь потом
     * рабоать с этим объектом.
     *
     * Пока не реализовал эту функцию!!!
     */
    fun initKeyGame()

}
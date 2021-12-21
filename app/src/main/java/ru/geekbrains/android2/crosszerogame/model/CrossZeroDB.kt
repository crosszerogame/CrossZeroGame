package ru.geekbrains.android2.crosszerogame.model

import ru.geekbrains.android2.crosszerogame.data.Game
import ru.geekbrains.android2.crosszerogame.data.Gamer

interface CrossZeroDB {

    fun insGamer(gamer: Gamer)
    fun updGamer(gamer: Gamer)
    fun delGamer(key: String): Boolean
    fun getGamer(key: String): Gamer?
    fun listGamer(): List<Gamer>?

    fun insGame(game: Game): String?
    fun updGame(key: String, game: Game): Boolean
    fun delGame(key: String): Boolean
    fun getGame(key: String): Game?
    fun listGame(): List<Game>?

}
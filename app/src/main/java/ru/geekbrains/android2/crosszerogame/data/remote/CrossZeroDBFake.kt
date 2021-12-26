package ru.geekbrains.android2.crosszerogame.data.remote

import ru.geekbrains.android2.crosszerogame.data.Game
import ru.geekbrains.android2.crosszerogame.data.Gamer

class CrossZeroDBFake:CrossZeroDB {
    private var listGamers = mutableListOf<Gamer>()
    private var listGames = mutableListOf<Game>()

    override fun insGamer(gamer: Gamer): String {
        val g = gamer.copy()
        listGamers.add(g)
        return (listGamers.size-1).toString()
    }

    override fun updGamer(key: String, gamer: Gamer): Boolean {
        val g = gamer.copy()
        listGamers[key.toInt()]=g
        return true
    }

    override fun delGamer(key: String): Boolean {
        return true
    }

    override fun getGamer(key: String): Gamer {
        val g =listGamers[key.toInt()]
        return g.copy()
    }

    override fun listGamer(): List<Gamer> {
        return listGamers
    }

    override fun insGame(game: Game): String {
        val g = game.copy()
            g.gameField = game.gameField.copyOf()
        listGames.add(g)
        return (listGames.size-1).toString()
    }

    override fun updGame(key: String, game: Game): Boolean {
        val g = game.copy()
        g.gameField = game.gameField.copyOf()
        listGames[key.toInt()]=g
        return true
    }

    override fun delGame(key: String): Boolean {
        return true
    }

    override fun getGame(key: String): Game {
        val g =listGames[key.toInt()]
        return g.copy()
    }

    override fun listGame(): List<Game> {
        return listGames
    }
}
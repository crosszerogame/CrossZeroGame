package ru.geekbrains.android2.crosszerogame.data.remote

import ru.geekbrains.android2.crosszerogame.data.Game
import ru.geekbrains.android2.crosszerogame.data.Gamer

class RemoteGame(private val db: CrossZeroDB) {

    suspend fun gamerRemote(
        gamer: Gamer
    ): Gamer? {
        var keyGamer = gamer.keyGamer
        if (keyGamer == "")
            keyGamer = db.insGamer(gamer) ?: ""
        else
            db.updGamer(keyGamer, gamer)
        return db.getGamer(keyGamer)
    }

    suspend fun getOpponentRemote(gamer: Gamer): Gamer? {
        val g = db.getGamer(gamer.keyGamer)
        if (g != null) {
            if (g.keyOpponent != "") {
                return db.getGamer(g.keyOpponent)
            } else return null
        } else return null
    }

    suspend fun setOpponentRemote(gamer: Gamer): Boolean {
        val g = db.getGamer(gamer.keyGamer)
        if (g != null) {
            if (gamer.keyOpponent != "") {
                val o = db.getGamer(gamer.keyOpponent)
                if (o != null) {
                    if (o.keyOpponent == "" || o.keyOpponent == g.keyGamer) {
                        o.keyOpponent = g.keyGamer

                        if (g.keyOpponent != gamer.keyOpponent) delOpponentWithGame(g)
                        g.keyOpponent = o.keyGamer
                        return (db.updGamer(o.keyGamer, o) && db.updGamer(g.keyGamer, g))
                    } else return false
                } else return false
            } else {
                //если keyOpponent==""
                return delOpponentWithGame(gamer)
            }
        } else return false
    }

    suspend fun delGamer(gamer: Gamer): Boolean {
        delOpponentWithGame(gamer)
        return db.delGamer(gamer.keyGamer)
    }

    private suspend fun delOpponentWithGame(gamer: Gamer): Boolean {
        val g = db.getGamer(gamer.keyGamer)
        if (g != null) {
            if (g.keyOpponent != "") {
                val o = db.getGamer(g.keyOpponent)
                if (o != null) {
                    if (o.keyOpponent == g.keyGamer) {
                        if (o.keyGame != "") db.delGame(o.keyGame)
                        o.keyGame = ""
                        o.keyOpponent = ""
                        db.updGamer(o.keyGamer, o)
                    }
                }
                if (g.keyGame != "") db.delGame(g.keyGame)
                g.keyOpponent = ""
                g.keyGame = ""
                db.updGamer(g.keyGamer, g)
            }
            return true
        } else return false
    }

    suspend fun opponentsListRemote(gamer: Gamer): List<Gamer>? = db.listGamer()?.filter {
        it.keyGamer != gamer.keyGamer && it.keyOpponent == "" && it.isOnLine
    }

    suspend fun setGameOpponentRemote(gamer: Gamer, game: Game): Boolean {
        val g = db.getGamer(gamer.keyGamer)
        val o = db.getGamer(gamer.keyOpponent)
        if (g != null && o != null) {
            if (g.keyOpponent == o.keyGamer && g.keyGamer == o.keyOpponent) {
                if (o.keyGame == "") {
                    o.keyGame = db.insGame(game) ?: ""
                }
                if (o.keyGame != "") {
                    if (db.updGamer(o.keyGamer, o)) {
                        game.keyGame = o.keyGame
                        return db.updGame(o.keyGame, game)
                    } else return false
                } else return false
            } else return false
        } else return false
    }

    suspend fun getGameOpponentRemote(gamer: Gamer): Game? {
        val g = db.getGamer(gamer.keyGamer)
        if (g != null) {
            if (g.keyGame != "") {
                val gm = db.getGame(g.keyGame)
                gm?.revertGamerToOpponent()
                return gm
            } else return null
        } else return null
    }

}
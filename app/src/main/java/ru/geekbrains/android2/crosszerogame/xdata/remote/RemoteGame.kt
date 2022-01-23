package ru.geekbrains.android2.crosszerogame.xdata.remote

import ru.geekbrains.android2.crosszerogame.xdata.Game
import ru.geekbrains.android2.crosszerogame.xdata.Gamer

class RemoteGame(private val db: CrossZeroDB) {

    suspend fun gamerRemote(
        gamer: Gamer
    ): Gamer? {
        val g = db.getGamer(gamer.keyGamer)
        g?.let {
            if (gamer.keyOpponent != "") {
                var keyOp = ""
                val o = db.getGamer(gamer.keyOpponent)
                o?.let {
                    if (o.keyOpponent == g.keyGamer) keyOp = o.keyGamer
                }
                gamer.keyOpponent = keyOp
            }
            gamer.keyGame = g.keyGame
            if (db.updGamer(g.keyGamer, gamer)) return db.getGamer(g.keyGamer)
        }
        val keyGame = db.insGame(Game())
        keyGame?.let {
            gamer.keyGame = it
            val keyGamer = db.insGamer(gamer)
            keyGamer?.let { return db.getGamer(keyGamer) }
        }
        return null
    }

    suspend fun getOpponentRemote(gamer: Gamer): Gamer? {
        val g = db.getGamer(gamer.keyGamer)
        var o: Gamer? = null
        g?.let {
            o = db.getGamer(g.keyOpponent)
        }
        return o
    }

    suspend fun setOpponentRemote(gamer: Gamer): Boolean {
        var ok = false
        val g = db.getGamer(gamer.keyGamer)
        g?.let {
            if (gamer.keyOpponent != "") {
                val o = db.getGamer(gamer.keyOpponent)
                o?.let {
                    if (o.isOnLine && (o.keyOpponent == "" || o.keyOpponent == g.keyGamer)) {
                        o.keyOpponent = g.keyGamer
                        g.keyOpponent = o.keyGamer
                        ok = (db.updGamer(o.keyGamer, o) && db.updGamer(g.keyGamer, g))
                    }
                }
            } else {
                g.keyOpponent = ""
                ok = db.updGamer(g.keyGamer, g)
            }
        }
        return ok
    }

    suspend fun delGamer(gamer: Gamer): Boolean {
        db.delGame(gamer.keyGame)
        return db.delGamer(gamer.keyGamer)
    }

    suspend fun opponentsListRemote(gamer: Gamer): List<Gamer>? = db.listGamer()?.filter {
        it.keyGamer != gamer.keyGamer && (it.keyOpponent == "" || it.keyOpponent == gamer.keyGamer) && it.isOnLine
    }

    suspend fun setGameOpponentRemote(gamer: Gamer, game: Game): Boolean {
        val g = db.getGamer(gamer.keyGamer)
        val o = db.getGamer(gamer.keyOpponent)
        var ok = false
        g?.let {
            o?.let {
                if (o.isOnLine && o.keyOpponent == g.keyGamer) {
                    ok = db.updGame(o.keyGame, game)
                } else {
                    g.keyOpponent = ""
                    db.updGamer(g.keyGamer, g)
                }
            }
        }
        return ok
    }

    suspend fun getGameOpponentRemote(gamer: Gamer): Game? {
        val g = db.getGamer(gamer.keyGamer)
        val o = db.getGamer(gamer.keyOpponent)
        var gm: Game? = null
        g?.let {
            gm = db.getGame(g.keyGame)
            gm?.revertGamerToOpponent()
            var notOk = o == null
            o?.let {
                notOk = !o.isOnLine || o.keyOpponent != g.keyGamer
            }
            if (notOk) {
                g.keyOpponent = ""
                db.updGamer(g.keyGamer, g)
            }
        }
        return gm
    }
}
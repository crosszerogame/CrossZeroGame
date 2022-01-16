package ru.geekbrains.android2.crosszerogame.xdata.remote.back4app

import com.parse.ParseObject
import com.parse.ParseQuery
import ru.geekbrains.android2.crosszerogame.xdata.Game
import ru.geekbrains.android2.crosszerogame.xdata.GameConstants
import ru.geekbrains.android2.crosszerogame.xdata.Gamer
import ru.geekbrains.android2.crosszerogame.xdata.remote.CrossZeroDB
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CrossZeroDBImpl : CrossZeroDB {

    private fun putParseGamer(parseObject: ParseObject, gamer: Gamer) {
        parseObject.put("nikGamer", gamer.nikGamer)
        parseObject.put("gameFieldSize", gamer.gameFieldSize)
        parseObject.put("levelGamer", gamer.levelGamer)
        parseObject.put("chipImageId", gamer.chipImageId)
        parseObject.put("timeForTurn", gamer.timeForTurn)
        parseObject.put("keyOpponent", gamer.keyOpponent)
        parseObject.put("keyGame", gamer.keyGame)
        parseObject.put("isOnLine", gamer.isOnLine)
    }

    private fun getParseGamer(obj: ParseObject) = Gamer(
        keyGamer = obj.objectId,
        nikGamer = obj.getString("nikGamer") ?: "",
        gameFieldSize = obj.getInt("gameFieldSize"),
        levelGamer = obj.getInt("levelGamer"),
        chipImageId = obj.getInt("chipImageId"),
        timeForTurn = obj.getInt("timeForTurn"),
        keyOpponent = obj.getString("keyOpponent") ?: "",
        keyGame = obj.getString("keyGame") ?: "",
        isOnLine = obj.getBoolean("isOnLine")
    )

    override suspend fun insGamer(gamer: Gamer): String? {
        var objectId: String? = null
        val parseObject = ParseObject("Gamer")
        parseObject.save()
        putParseGamer(parseObject, gamer)
        return suspendCoroutine { cont ->
            parseObject.saveInBackground {
                if (it == null) objectId = parseObject.objectId
                cont.resume(objectId)
            }
        }
    }

    override suspend fun updGamer(key: String, gamer: Gamer): Boolean {
        val parseObject = ParseObject("Gamer")
        parseObject.objectId = key
        putParseGamer(parseObject, gamer)
        return suspendCoroutine { cont ->
            parseObject.saveInBackground {
                cont.resume(it == null)
            }
        }
    }

    override suspend fun delGamer(key: String): Boolean {
        val parseObject = ParseObject("Gamer")
        parseObject.objectId = key
        return suspendCoroutine { cont ->
            parseObject.deleteInBackground {
                cont.resume(it == null)
            }
        }
    }

    override suspend fun getGamer(key: String): Gamer? {
        var gamer: Gamer? = null
        val query = ParseQuery<ParseObject>("Gamer")
        return suspendCoroutine { cont ->
            query.getInBackground(
                key
            ) { obj, e ->
                if (e == null)
                    gamer = getParseGamer(obj)
                cont.resume(gamer)
            }
        }
    }

    override suspend fun listGamer(): List<Gamer>? {
        var gamers: List<Gamer>? = null
        val query = ParseQuery.getQuery<ParseObject>("Gamer")
        query.orderByDescending("createdAt")
        return suspendCoroutine { cont ->
            query.findInBackground { objects, e ->
                if (e == null) gamers = getListParseGamer(objects)
                cont.resume(gamers)
            }
        }
    }

    private fun getListParseGamer(objects: List<ParseObject>) = objects.map {
        getParseGamer(it)
    }

    private fun putParseGame(parseObject: ParseObject, game: Game) {
        parseObject.put("gameFieldSize", game.gameFieldSize)
        parseObject.put("motionXIndex", game.motionXIndex)
        parseObject.put("motionYIndex", game.motionYIndex)
        parseObject.put("gameStatus", game.gameStatus.ordinal)
        parseObject.put("dotsToWin", game.dotsToWin)
        parseObject.put("turnOfGamer", game.turnOfGamer)
        parseObject.put("timeForTurn", game.timeForTurn)
        parseObject.put("countOfTurn", game.countOfTurn)
    }

    private fun getParseGame(obj: ParseObject) = Game(
        keyGame = obj.objectId,
        gameFieldSize = obj.getInt("gameFieldSize"),
        motionXIndex = obj.getInt("motionXIndex"),
        motionYIndex = obj.getInt("motionYIndex"),
        gameStatus = GameConstants.GameStatus.values()[obj.getInt("gameStatus")],
        dotsToWin = obj.getInt("dotsToWin"),
        turnOfGamer = obj.getBoolean("turnOfGamer"),
        timeForTurn = obj.getInt("timeForTurn"),
        countOfTurn = obj.getInt("countOfTurn")
    )


    override suspend fun insGame(game: Game): String? {
        var objectId: String? = null
        val parseObject = ParseObject("Game")
        parseObject.save()
        putParseGame(parseObject, game)
        return suspendCoroutine { cont ->
            parseObject.saveInBackground {
                if (it == null) objectId = parseObject.objectId
                cont.resume(objectId)
            }
        }
    }

    override suspend fun updGame(key: String, game: Game): Boolean {
        val parseObject = ParseObject("Game")
        parseObject.objectId = key
        putParseGame(parseObject, game)
        return suspendCoroutine { cont ->
            parseObject.saveInBackground {
                cont.resume(it == null)
            }
        }
    }

    override suspend fun delGame(key: String): Boolean {
        val parseObject = ParseObject("Game")
        parseObject.objectId = key
        return suspendCoroutine { cont ->
            parseObject.deleteInBackground {
                cont.resume(it == null)
            }
        }
    }

    override suspend fun getGame(key: String): Game? {
        var game: Game? = null
        val query = ParseQuery<ParseObject>("Game")
        return suspendCoroutine { cont ->
            query.getInBackground(
                key
            ) { obj, e ->
                if (e == null)
                    game = getParseGame(obj)
                cont.resume(game)
            }
        }
    }

    override suspend fun listGame(): List<Game>? {
        var games: List<Game>? = null
        val query = ParseQuery.getQuery<ParseObject>("Game")
        query.orderByDescending("createdAt")
        return suspendCoroutine { cont ->
            query.findInBackground { objects, e ->
                if (e == null) games = getListParseGame(objects)
                cont.resume(games)
            }
        }
    }

    private fun getListParseGame(objects: List<ParseObject>) = objects.map {
        getParseGame(it)
    }

}
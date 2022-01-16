package ru.geekbrains.android2.crosszerogame.game.remote

import com.parse.ParseObject
import com.parse.ParseQuery
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class RemoteDataBaseImpl : RemoteDataBase {

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

    override suspend fun postGamer(gamer: Gamer): String? {
        val parseObject = ParseObject("Gamer")
        if (gamer.keyGamer.isEmpty())
            parseObject.save()
        else
            parseObject.objectId = gamer.keyGamer
        putParseGamer(parseObject, gamer)

        return suspendCoroutine { cont ->
            parseObject.saveInBackground {
                if (it == null)
                    cont.resume(parseObject.objectId)
                else
                    cont.resume(null)

            }
        }
    }

    override suspend fun deleteGamer(key: String): Boolean {
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
            query.getInBackground(key) { obj, e ->
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

    private fun putParseGame(parseObject: ParseObject, game: GameRemote) {
        parseObject.put("gameFieldSize", game.gameFieldSize)
        parseObject.put("motionXIndex", game.motionXIndex)
        parseObject.put("motionYIndex", game.motionYIndex)
        parseObject.put("gameStatus", game.gameStatus.ordinal)
        parseObject.put("dotsToWin", game.dotsToWin)
        parseObject.put("turnOfGamer", game.turnOfGamer)
        parseObject.put("timeForTurn", game.timeForTurn)
        parseObject.put("countOfTurn", game.countOfTurn)
    }

    private fun getParseGame(obj: ParseObject) = GameRemote(
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


    override suspend fun postGame(game: GameRemote): String? {
        val parseObject = ParseObject("Game")
        if (game.keyGame.isEmpty())
            parseObject.save()
        else
            parseObject.objectId = game.keyGame
        putParseGame(parseObject, game)

        return suspendCoroutine { cont ->
            parseObject.saveInBackground {
                if (it == null)
                    cont.resume(parseObject.objectId)
                else
                    cont.resume(null)
            }
        }
    }

    override suspend fun deleteGame(key: String): Boolean {
        val parseObject = ParseObject("Game")
        parseObject.objectId = key
        return suspendCoroutine { cont ->
            parseObject.deleteInBackground {
                cont.resume(it == null)
            }
        }
    }

    override suspend fun getGame(key: String): GameRemote? {
        var game: GameRemote? = null
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

    override suspend fun listGame(): List<GameRemote>? {
        var games: List<GameRemote>? = null
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
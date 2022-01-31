package ru.geekbrains.android2.crosszerogame.xdata.remote.back4app

import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.livequery.ParseLiveQueryClient
import com.parse.livequery.SubscriptionHandling
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import ru.geekbrains.android2.crosszerogame.xdata.Game
import ru.geekbrains.android2.crosszerogame.xdata.GameConstants
import ru.geekbrains.android2.crosszerogame.xdata.Gamer
import ru.geekbrains.android2.crosszerogame.xdata.remote.CrossZeroDB
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CrossZeroDBImpl : CrossZeroDB {
    private var gamerParseLQClient: ParseLiveQueryClient? = null
    private var gamerSubscrHandling: SubscriptionHandling<ParseObject>? = null
    private var gameParseLQClient: ParseLiveQueryClient? = null
    private var gameSubscrHandling: SubscriptionHandling<ParseObject>? = null

    private fun putParseGamer(parseObject: ParseObject, gamer: Gamer) {
        parseObject.put("nikGamer", gamer.nikGamer)
        parseObject.put("gameFieldSize", gamer.gameFieldSize)
        parseObject.put("levelGamer", gamer.levelGamer.ordinal)
        parseObject.put("chipImageId", gamer.chipImageId)
        parseObject.put("timeForTurn", gamer.timeForTurn)
        parseObject.put("keyOpponent", gamer.keyOpponent)
        parseObject.put("keyGame", gamer.keyGame)
        parseObject.put("isOnLine", gamer.isOnLine)
        parseObject.put("isFirst", gamer.isFirst)
    }

    private fun getParseGamer(obj: ParseObject) = Gamer(
        keyGamer = obj.objectId,
        nikGamer = obj.getString("nikGamer") ?: "",
        gameFieldSize = obj.getInt("gameFieldSize"),
        levelGamer = GameConstants.GameLevel.values()[obj.getInt("levelGamer")],
        chipImageId = obj.getInt("chipImageId"),
        timeForTurn = obj.getInt("timeForTurn"),
        keyOpponent = obj.getString("keyOpponent") ?: "",
        keyGame = obj.getString("keyGame") ?: "",
        isOnLine = obj.getBoolean("isOnLine"),
        isFirst = obj.getBoolean("isFirst")
    )

    override suspend fun insGamer(gamer: Gamer): String? {
        var objectId: String? = null
        val parseObject = ParseObject(GameConstants.CLASS_GAMER)
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
        val parseObject = ParseObject(GameConstants.CLASS_GAMER)
        parseObject.objectId = key
        putParseGamer(parseObject, gamer)
        return suspendCoroutine { cont ->
            parseObject.saveInBackground {
                cont.resume(it == null)
            }
        }
    }

    override suspend fun delGamer(key: String): Boolean {
        val parseObject = ParseObject(GameConstants.CLASS_GAMER)
        parseObject.objectId = key
        return suspendCoroutine { cont ->
            parseObject.deleteInBackground {
                cont.resume(it == null)
            }
        }
    }

    override suspend fun getGamer(key: String): Gamer? {
        var gamer: Gamer? = null
        val query = ParseQuery<ParseObject>(GameConstants.CLASS_GAMER)
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
        val query = ParseQuery.getQuery<ParseObject>(GameConstants.CLASS_GAMER)
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
        val parseObject = ParseObject(GameConstants.CLASS_GAME)
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
        val parseObject = ParseObject(GameConstants.CLASS_GAME)
        parseObject.objectId = key
        putParseGame(parseObject, game)
        return suspendCoroutine { cont ->
            parseObject.saveInBackground {
                cont.resume(it == null)
            }
        }
    }

    override suspend fun delGame(key: String): Boolean {
        val parseObject = ParseObject(GameConstants.CLASS_GAME)
        parseObject.objectId = key
        return suspendCoroutine { cont ->
            parseObject.deleteInBackground {
                cont.resume(it == null)
            }
        }
    }

    override suspend fun getGame(key: String): Game? {
        var game: Game? = null
        val query = ParseQuery<ParseObject>(GameConstants.CLASS_GAME)
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
        val query = ParseQuery.getQuery<ParseObject>(GameConstants.CLASS_GAME)
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

    @ExperimentalCoroutinesApi
   override suspend fun gamerLiveQuery(key: String): Flow<Gamer> {
        gamerParseLQClient = ParseLiveQueryClient.Factory.getClient()
        val parseQuery = ParseQuery<ParseObject>(GameConstants.CLASS_GAMER)
        parseQuery.whereContains("objectId", key)
        return callbackFlow {
            gamerSubscrHandling = gamerParseLQClient!!.subscribe(parseQuery)
            gamerSubscrHandling!!.handleSubscribe {
                if (gamerSubscrHandling != null) {
                    gamerSubscrHandling!!.handleEvent(
                        SubscriptionHandling.Event.CREATE
                    ) { _: ParseQuery<ParseObject?>?, obj: ParseObject? ->
                        obj?.let { trySendBlocking(getParseGamer(obj)) }
                    }
                    gamerSubscrHandling!!.handleEvent(
                        SubscriptionHandling.Event.DELETE
                    ) { _: ParseQuery<ParseObject?>?, obj: ParseObject? ->
                        obj?.let {
                            val g = getParseGamer(obj)
                            g.keyGamer = ""
                            trySendBlocking(g)
                        }
                    }
                    gamerSubscrHandling!!.handleEvent(
                        SubscriptionHandling.Event.UPDATE
                    ) { _: ParseQuery<ParseObject?>?, obj: ParseObject? ->
                        obj?.let { trySendBlocking(getParseGamer(obj)) }
                    }
                }
            }
            awaitClose {
                gamerParseLQClient!!.unsubscribe(parseQuery,gamerSubscrHandling)
            }
        }
    }

    @ExperimentalCoroutinesApi
    override suspend fun gameLiveQuery(key: String): Flow<Game> {
        gameParseLQClient = ParseLiveQueryClient.Factory.getClient()
        val parseQuery = ParseQuery<ParseObject>(GameConstants.CLASS_GAME)
        parseQuery.whereContains("objectId", key)
        return callbackFlow {
            gameSubscrHandling = gameParseLQClient!!.subscribe(parseQuery)
            gameSubscrHandling!!.handleSubscribe {
                if (gameSubscrHandling != null) {
                    gameSubscrHandling!!.handleEvent(
                        SubscriptionHandling.Event.CREATE
                    ) { _: ParseQuery<ParseObject?>?, obj: ParseObject? ->
                        obj?.let { trySendBlocking(getParseGame(obj)) }
                    }
                    gameSubscrHandling!!.handleEvent(
                        SubscriptionHandling.Event.DELETE
                    ) { _: ParseQuery<ParseObject?>?, obj: ParseObject? ->
                        obj?.let {
                            val g = getParseGame(obj)
                            g.keyGame = ""
                            trySendBlocking(g)
                        }
                    }
                    gameSubscrHandling!!.handleEvent(
                        SubscriptionHandling.Event.UPDATE
                    ) { _: ParseQuery<ParseObject?>?, obj: ParseObject? ->
                        obj?.let { trySendBlocking(getParseGame(obj)) }
                    }
                }
            }
            awaitClose {
                gameParseLQClient!!.unsubscribe(parseQuery,gameSubscrHandling)
            }
        }
    }

}
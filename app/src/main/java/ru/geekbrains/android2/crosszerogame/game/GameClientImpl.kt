package ru.geekbrains.android2.crosszerogame.game

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.geekbrains.android2.crosszerogame.game.remote.GameConstants
import ru.geekbrains.android2.crosszerogame.game.remote.GameRemote
import ru.geekbrains.android2.crosszerogame.game.remote.Gamer
import ru.geekbrains.android2.crosszerogame.game.remote.RemoteDataBase
import ru.geekbrains.android2.crosszerogame.structure.GameClient
import ru.geekbrains.android2.crosszerogame.structure.data.Game
import ru.geekbrains.android2.crosszerogame.structure.data.MoveResult
import ru.geekbrains.android2.crosszerogame.structure.data.Player

class GameClientImpl(
    private val db: RemoteDataBase
) : GameClient {
    private var currentGame = GameRemote()
    private val listFinish = listOf(
        GameConstants.GameStatus.ABORTED_GAME,
        GameConstants.GameStatus.DRAWN_GAME,
        GameConstants.GameStatus.WIN_GAMER,
        GameConstants.GameStatus.WIN_OPPONENT
    )
    private var currentGamer = Gamer()
    private lateinit var opponent: Player
    private var playerNick: String = ""
    private var playerIsCross: Boolean = false
    private var gameIsFinish: Boolean = true
    private var isWaitOpponent = false
    private var isWaitMove = false

    override fun checkNick(nick: String): Flow<Boolean> = flow {
        for (g in opponentsList()) {
            if (nick == g.nikGamer)
                emit(false)
        }
        emit(true)
    }

    override fun startGame(game: Game, iIsCross: Boolean): Flow<Player?> = flow {
        playerIsCross = iIsCross
        playerNick = if (iIsCross)
            game.playerCross?.nick ?: ""
        else
            game.playerZero?.nick ?: ""

        updateGamer(game)

        if (game.playerCross == null || game.playerZero == null) {
            newGame(game)
            waitOpponent().collect {
                emit(it)
            }
        } else {
            val opponentKey = if (iIsCross)
                game.playerZero?.id ?: ""
            else
                game.playerCross?.id ?: ""
            connectGame(opponentKey).collect {
                emit(it)
            }
        }
    }

    override fun waitMove(): Flow<Player?> = flow {
        isWaitMove = true
        var game: GameRemote? = null
        do {
            delay(GameConstants.REFRESH_INTERVAL_MS_GAME)
            db.getGame(currentGame.keyGame)?.let { g ->
                game = g
                isWaitMove = g.motionXIndex == -1 || g.turnOfGamer == playerIsCross
            }
        } while (isWaitMove)
        game?.let { g ->
            updateOpponent(g)
            checkFinishGame()
            emit(opponent)
            return@flow
        }
        emit(null)
    }

    override suspend fun postState(state: Player.State) {
        val status = when (state) {
            Player.State.CREATED -> GameConstants.GameStatus.NEW_GAME
            Player.State.PLAYING -> GameConstants.GameStatus.GAME_IS_ON
            Player.State.LEFT -> {
                isWaitMove = false
                isWaitOpponent = false
                GameConstants.GameStatus.ABORTED_GAME
            }
        }
        currentGame.gameStatus = status
        db.postGame(currentGame)
    }

    override suspend fun postMove(x: Int, y: Int, result: MoveResult) {
        if (currentGame.turnOfGamer != playerIsCross)
            return
        currentGame.motionXIndex = x
        currentGame.motionYIndex = y
        currentGame.turnOfGamer = playerIsCross
        currentGame.gameStatus = when (result) {
            MoveResult.MOVE_PLAYER -> GameConstants.GameStatus.GAME_IS_ON
            MoveResult.MOVE_OPPONENT -> GameConstants.GameStatus.GAME_IS_ON
            MoveResult.WIN_PLAYER -> GameConstants.GameStatus.WIN_GAMER
            MoveResult.WIN_OPPONENT -> GameConstants.GameStatus.WIN_OPPONENT
            MoveResult.DRAWN -> GameConstants.GameStatus.DRAWN_GAME
            MoveResult.CANCEL -> GameConstants.GameStatus.GAME_IS_ON
        }
        db.postGame(currentGame)
    }

    private suspend fun waitOpponent(): Flow<Player?> = flow {
        flowGetOpponent().collect {
            if (it == null) {
                emit(null)
                return@collect
            }
            //TODO менять на NEW_GAME_ACCEPT, если оппонента отклонить
            currentGame.gameStatus = GameConstants.GameStatus.GAME_IS_ON
            db.postGame(currentGame)

            opponent = convertToPlayer(it)
            emit(opponent)
        }
    }

    private suspend fun connectGame(opponentKey: String): Flow<Player?> = flow {
        val gamer = db.getGamer(opponentKey)
        if (gamer == null) {
            emit(null)
            return@flow
        }
        val game = db.getGame(gamer.keyGame)
        if (game == null) {
            emit(null)
            return@flow
        }

        if (game.gameStatus != GameConstants.GameStatus.NEW_GAME) {
            emit(null)
            return@flow
        }
        gamer.keyOpponent = currentGamer.keyGamer
        db.postGamer(gamer)

        isWaitOpponent = true
        do {
            delay(GameConstants.REFRESH_INTERVAL_MS_GET_OPPONENT)

            db.getGame(game.keyGame)?.let { g ->
                when (g.gameStatus) {
                    GameConstants.GameStatus.GAME_IS_ON -> {
                        //позволено вступить в игру
                        isWaitOpponent = false
                        currentGame = g
                        opponent = convertToPlayer(gamer)
                        currentGamer.keyOpponent = gamer.keyGamer
                        currentGamer.keyGame = g.keyGame
                        db.postGamer(currentGamer)
                        emit(opponent)
                    }
                    GameConstants.GameStatus.NEW_GAME_ACCEPT -> {
                        //вступление в игру отклонено
                        isWaitOpponent = false
                        g.gameStatus = GameConstants.GameStatus.NEW_GAME
                        db.postGame(currentGame)
                        emit(null)
                    }
                    else -> {}
                }
            }
        } while (isWaitOpponent)
    }

    private fun convertToPlayer(gamer: Gamer) = Player(
        id = gamer.keyGamer,
        nick = gamer.nikGamer
    )

    private fun updateOpponent(game: GameRemote) = with(opponent) {
        state = Player.State.PLAYING
        lastTimeActive = System.currentTimeMillis()
        if (moveX != game.motionXIndex || moveY != game.motionYIndex) {
            moveX = game.motionXIndex
            moveY = game.motionYIndex
            game.turnOfGamer = !game.turnOfGamer
        }
        currentGame = game
    }

    private suspend fun checkFinishGame() {
        if (currentGame.gameStatus in listFinish)
            db.deleteGame(currentGame.keyGame)
    }

    override fun loadGamesList(): Flow<List<Game>> = flow {
        val list = mutableListOf<Game>()

        for (g in opponentsList()) {
            if (playerNick == g.nikGamer)
                continue

            val state = if (g.isOnLine) {
                if (g.keyOpponent.isEmpty())
                    Game.State.WAIT_OPPONENT
                else
                    Game.State.CONTINUE
            } else Game.State.FINISH

            if (state != Game.State.WAIT_OPPONENT)
                continue

            val chipsForWin = db.getGame(g.keyGame)?.dotsToWin ?: -1
            val isCross = g.chipImageId == 0
            val player = Player(
                id = g.keyGamer,
                nick = g.nikGamer
            )

            val game = Game(
                id = g.keyGame,
                fieldSize = g.gameFieldSize,
                chipsForWin = chipsForWin,
                level = g.levelGamer,
                moveTime = g.timeForTurn,
                playerCross = if (isCross) player else null,
                playerZero = if (!isCross) player else null
            )
            list.add(game)
        }

        emit(list)
    }

    private suspend fun updateGamer(game: Game) {
        currentGamer = Gamer(
            keyGamer = currentGamer.keyGamer,
            nikGamer = playerNick,
            gameFieldSize = game.fieldSize,
            levelGamer = game.level,
            chipImageId = if (playerIsCross) 0 else 1,
            timeForTurn = game.moveTime,
            keyOpponent = "",
            keyGame = currentGamer.keyGame,
            isOnLine = true
        )
        val key = db.postGamer(currentGamer) ?: ""
        currentGamer.keyGamer = key
    }

    private suspend fun opponentsList() = db.listGamer()?.filter {
        it.keyGamer != currentGamer.keyGamer && it.keyOpponent == "" && it.isOnLine
    } ?: listOf()

    private fun flowGetOpponent(): Flow<Gamer?> = flow {
        isWaitOpponent = true
        do {
            delay(GameConstants.REFRESH_INTERVAL_MS_GET_OPPONENT)
            db.getGamer(currentGamer.keyGamer)?.let { gamer ->
                val key = gamer.keyOpponent
                if (key != currentGamer.keyOpponent) {
                    currentGamer.keyOpponent = key
                    db.getGamer(key)?.let { opponent ->
                        isWaitOpponent = false
                        emit(opponent)
                        return@flow
                    }
                }
            }
        } while (isWaitOpponent)
        emit(null)
    }

    private suspend fun newGame(game: Game) {
        gameIsFinish = false
        currentGame =
            GameRemote(
                keyGame = "",
                gameFieldSize = game.fieldSize,
                motionXIndex = -1,
                motionYIndex = -1,
                gameStatus = GameConstants.GameStatus.NEW_GAME,
                dotsToWin = game.chipsForWin,
                timeForTurn = game.moveTime
            )

        val key = db.postGame(currentGame) ?: ""
        currentGame.keyGame = key
        currentGamer.keyGame = key
        if (key.isNotEmpty())
            db.postGamer(currentGamer)
    }
}
package ru.geekbrains.android2.crosszerogame.xdata

import ru.geekbrains.android2.crosszerogame.xdata.GameConstants.MIN_TIME_FOR_TURN
import ru.geekbrains.android2.crosszerogame.xdata.ai.AI
import ru.geekbrains.android2.crosszerogame.xdata.remote.CrossZeroDB
import ru.geekbrains.android2.crosszerogame.xdata.remote.RemoteGame

class GameRepositoryImpl(val remoteOpponentGame: Boolean = false, db: CrossZeroDB) :
    GameRepository {
    private var currentGame = Game(
        gameStatus = if (remoteOpponentGame) GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT else GameConstants.GameStatus.NEW_GAME_FIRST_GAMER,
        turnOfGamer = true
    )
    private var currentGamer = Gamer()
    private val ai = AI()
    private val rg = RemoteGame(db)
    private var countTurnGamer = 0
    private var countTurnOpponent = 0
    var flowGetOpponentIsOn = false

    override suspend fun gamer(
        gamer: Gamer
    ): Gamer {
        currentGamer = Gamer(
            keyGamer = gamer.keyGamer,
            nikGamer = gamer.nikGamer,
            gameFieldSize = gamer.gameFieldSize,
            levelGamer = gamer.levelGamer,
            chipImageId = gamer.chipImageId,
            timeForTurn = gamer.timeForTurn,
            keyOpponent = gamer.keyOpponent,
            keyGame = gamer.keyGame,
            isOnLine = gamer.isOnLine,
            isFirst = gamer.isFirst
        )
        if (remoteOpponentGame) currentGamer = rg.gamerRemote(currentGamer) ?: currentGamer
        return currentGamer
    }

    suspend fun flowGamer(key: String) = rg.gamerLiveQueryRemote(key)
    suspend fun flowGame(key: String) = rg.gameLiveQueryRemote(key)

    override suspend fun getOpponent(): Gamer? {
        return if (remoteOpponentGame) {
            val opponent = rg.getOpponentRemote(currentGamer)
            currentGamer.keyOpponent = opponent?.keyGamer ?: ""
            opponent
        } else null
    }

    override suspend fun setOpponent(key: String, gameStatus: GameConstants.GameStatus): Game? {
        currentGamer.keyOpponent = key
        var gm: Game? = null
        if (remoteOpponentGame) {
            if (rg.setOpponentRemote(currentGamer)) {
                if (currentGamer.keyOpponent != "") {
                    currentGame.gameStatus = gameStatus
                    gm = game(
                        currentGame
                    )
                }
            }
        }
        return gm
    }

    override suspend fun opponentsList(): List<Gamer> =
        if (remoteOpponentGame) rg.opponentsListRemote(currentGamer) ?: listOf()
        else listOf()

    override suspend fun game(
        toGame: Game
    ): Game =
        if (remoteOpponentGame)
            gameHuman(toGame)
        else gameAi(toGame.motionXIndex, toGame.motionYIndex, toGame.gameStatus)

    private suspend fun gameHuman(
        toGame: Game
    ): Game {
        if (
            toGame.gameStatus == GameConstants.GameStatus.NEW_GAME_FIRST_GAMER ||
            toGame.gameStatus == GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT
        ) {
            if (toGame.turnOfGamer) {
                initGame(
                    gameStatusOld = toGame.gameStatus,
                    gameStatusNew = toGame.gameStatus,
                    gameFieldSizeNew = currentGamer.gameFieldSize
                )
                countTurnGamer = 1
                currentGame.countOfTurn = countTurnGamer
                if (rg.setGameOpponentRemote(
                        currentGamer,
                        currentGame
                    )
                ) currentGame.gameStatus = GameConstants.GameStatus.GAME_IS_ON
                else currentGame.gameStatus = GameConstants.GameStatus.ABORTED_GAME
            } else {
                initGame(
                    gameStatusOld = toGame.gameStatus,
                    gameStatusNew = GameConstants.GameStatus.GAME_IS_ON,
                    gameFieldSizeNew = toGame.gameFieldSize
                )
                countTurnOpponent = toGame.countOfTurn
            }

            return currentGame
        }

        if ((toGame.gameStatus == GameConstants.GameStatus.GAME_IS_ON &&
                    toGame.gameStatus !in arrayOf(
                GameConstants.GameStatus.WIN_GAMER,
                GameConstants.GameStatus.DRAWN_GAME,
                GameConstants.GameStatus.WIN_OPPONENT,
                GameConstants.GameStatus.ABORTED_GAME
            ))
            ||
            toGame.gameStatus == GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT
        ) {
            currentGame.gameStatus = GameConstants.GameStatus.GAME_IS_ON

            if (toGame.turnOfGamer) {
                if (!checkHumanTurn(
                        toGame.motionXIndex,
                        toGame.motionYIndex,
                        true
                    )
                ) return currentGame
                currentGame.countOfTurn = ++countTurnGamer
                if (rg.setGameOpponentRemote(currentGamer, currentGame))
                    when {
                        ai.checkWin(
                            toGame.motionXIndex,
                            toGame.motionYIndex,
                            currentGame.turnOfGamer
                        ) ->
                            currentGame.gameStatus = GameConstants.GameStatus.WIN_GAMER
                        ai.isMapFull() ->
                            currentGame.gameStatus = GameConstants.GameStatus.DRAWN_GAME
                    } else currentGame.gameStatus = GameConstants.GameStatus.ABORTED_GAME
            } else {
                var goodOpponentTurn = false
                if (toGame.countOfTurn > countTurnOpponent) {
                    countTurnOpponent = toGame.countOfTurn
                    if (checkHumanTurn(
                            toGame.motionXIndex,
                            toGame.motionYIndex,
                            false
                        )
                    ) {
                        currentGame.gameStatus = toGame.gameStatus
                        goodOpponentTurn = true
                    }
                }
                if (!goodOpponentTurn) {
                    currentGame.motionXIndex = -1
                    currentGame.motionYIndex = -1
                    return currentGame
                }

                when {
                    ai.checkWin(
                        currentGame.motionXIndex,
                        currentGame.motionYIndex,
                        currentGame.turnOfGamer
                    ) -> currentGame.gameStatus = GameConstants.GameStatus.WIN_OPPONENT
                    ai.isMapFull() -> currentGame.gameStatus = GameConstants.GameStatus.DRAWN_GAME
                }
            }
            currentGame.turnOfGamer = !currentGame.turnOfGamer
            return currentGame
        }
        currentGame.gameStatus = GameConstants.GameStatus.ABORTED_GAME
        rg.setGameOpponentRemote(currentGamer, currentGame)
        return currentGame
    }

    private fun gameAi(
        motionXIndex: Int,
        motionYIndex: Int,
        gameStatus: GameConstants.GameStatus
    ): Game {
        if (
            gameStatus == GameConstants.GameStatus.NEW_GAME_FIRST_GAMER ||
            gameStatus == GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT
        ) {
            initGame(
                gameStatusOld = gameStatus,
                gameStatusNew = GameConstants.GameStatus.GAME_IS_ON,
                gameFieldSizeNew = currentGamer.gameFieldSize
            )
            currentGamer.gameFieldSize = currentGame.gameFieldSize
            if (currentGame.turnOfGamer) return currentGame
        }

        if ((gameStatus == GameConstants.GameStatus.GAME_IS_ON &&
                    currentGame.gameStatus !in arrayOf(
                GameConstants.GameStatus.WIN_GAMER,
                GameConstants.GameStatus.DRAWN_GAME,
                GameConstants.GameStatus.WIN_OPPONENT,
                GameConstants.GameStatus.ABORTED_GAME
            ))
            ||
            gameStatus == GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT
        ) {
            currentGame.gameStatus = GameConstants.GameStatus.GAME_IS_ON

            if (currentGame.turnOfGamer) {
                if (!checkHumanTurn(motionXIndex, motionYIndex, true)) return currentGame
                when {
                    ai.checkWin(motionXIndex, motionYIndex, currentGame.turnOfGamer) ->
                        currentGame.gameStatus = GameConstants.GameStatus.WIN_GAMER
                    ai.isMapFull() ->
                        currentGame.gameStatus = GameConstants.GameStatus.DRAWN_GAME
                }
                currentGame.turnOfGamer = !currentGame.turnOfGamer
            }
            if (!currentGame.turnOfGamer && currentGame.gameStatus !in arrayOf(
                    GameConstants.GameStatus.WIN_GAMER,
                    GameConstants.GameStatus.DRAWN_GAME
                )
            ) {
                aiTurn(currentGamer.levelGamer)
                when {
                    ai.checkWin(
                        currentGame.motionXIndex,
                        currentGame.motionYIndex,
                        currentGame.turnOfGamer
                    ) -> currentGame.gameStatus = GameConstants.GameStatus.WIN_OPPONENT
                    ai.isMapFull() -> currentGame.gameStatus = GameConstants.GameStatus.DRAWN_GAME
                }

            }
            currentGame.turnOfGamer = !currentGame.turnOfGamer
            return currentGame
        }
        currentGame.gameStatus = GameConstants.GameStatus.ABORTED_GAME
        return currentGame
    }

    override suspend fun deleteGamer(): Boolean {
        return if (remoteOpponentGame) {
            rg.delGamer(currentGamer)
        } else false
    }

    override fun dotsToWin(gameFieldSize: Int): Pair<Int, Int> = ai.dotsToWin(gameFieldSize)

    private fun initGame(
        gameStatusOld: GameConstants.GameStatus,
        gameStatusNew: GameConstants.GameStatus,
        gameFieldSizeNew: Int
    ) {

        val pairFieldWin = ai.initGame(gameFieldSizeNew)

        currentGame =
            Game(
                keyGame = "",
                gameFieldSize = pairFieldWin.first,
                motionXIndex = -1,
                motionYIndex = -1,
                gameStatus = gameStatusNew,
                dotsToWin = pairFieldWin.second,
                timeForTurn = MIN_TIME_FOR_TURN
            )

        currentGame.turnOfGamer = when (gameStatusOld) {
            GameConstants.GameStatus.NEW_GAME_FIRST_GAMER -> true
            else -> false
        }
    }

    private fun checkHumanTurn(
        motionXIndex: Int,
        motionYIndex: Int,
        turnOfGamer: Boolean
    ) = if (ai.humanTurn(motionXIndex, motionYIndex, turnOfGamer)) {
        currentGame.motionXIndex = motionXIndex
        currentGame.motionYIndex = motionYIndex
        if (turnOfGamer) {
            currentGame.gameField[currentGame.motionYIndex][currentGame.motionXIndex] =
                GameConstants.CellField.GAMER
        } else {
            currentGame.gameField[currentGame.motionYIndex][currentGame.motionXIndex] =
                GameConstants.CellField.OPPONENT
        }
        true
    } else {
        currentGame.motionXIndex = -1
        currentGame.motionYIndex = -1
        false
    }

    private fun aiTurn(levelGamer: GameConstants.GameLevel) {
        val xy = ai.aiTurn(level = levelGamer.ordinal)
        currentGame.motionXIndex = xy.first
        currentGame.motionYIndex = xy.second
        currentGame.gameField[currentGame.motionYIndex][currentGame.motionXIndex] =
            GameConstants.CellField.OPPONENT
    }

}
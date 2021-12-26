package ru.geekbrains.android2.crosszerogame.data

import ru.geekbrains.android2.crosszerogame.data.GameConstants.MIN_FIELD_SIZE
import ru.geekbrains.android2.crosszerogame.data.GameConstants.MIN_TIME_FOR_TURN
import ru.geekbrains.android2.crosszerogame.data.ai.AI
import ru.geekbrains.android2.crosszerogame.data.remote.CrossZeroDB
import ru.geekbrains.android2.crosszerogame.data.remote.RemoteGame

class GameRepositoryImpl(val remoteOpponentGame: Boolean = false, db: CrossZeroDB):GameRepository {
    private var currentGame = Game(
        gameStatus = if (remoteOpponentGame) GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT else GameConstants.GameStatus.NEW_GAME_FIRST_GAMER,
        turnOfGamer = true
    )
    private var currentGamer = Gamer()
    private val ai = AI(remoteOpponentGame)
    private val rg = RemoteGame(db)
    private var countTurnGamer = 0
    private var countTurnOpponent = 0

    override fun gamer(
        nikGamer: String ,
        gameFieldSize: Int ,
        levelGamer: Int ,
        chipImageId: Int ,
        timeForTurn: Int ,
    ): Gamer {
        currentGamer = Gamer(
            keyGamer = currentGamer.keyGamer,
            nikGamer = nikGamer,
            gameFieldSize = gameFieldSize,
            levelGamer = levelGamer,
            chipImageId = chipImageId,
            timeForTurn = timeForTurn,
            keyOpponent = currentGamer.keyOpponent,
            keyGame = currentGamer.keyGame
        )
        if (remoteOpponentGame) currentGamer = rg.gamerRemote(currentGamer)
        return currentGamer
    }

    override fun getOpponent(): Gamer? {
        return if (remoteOpponentGame) {
            val opponent = rg.getOpponentRemote(currentGamer)
            currentGamer.keyOpponent = opponent?.keyGamer ?: ""
            opponent
        } else
            Gamer(
                keyGamer = "",
                nikGamer = "Art Intelligence",
                gameFieldSize = currentGamer.gameFieldSize
            )
    }


    override fun setOpponent(key: String, gameStatus: GameConstants.GameStatus): Game? {
        currentGamer.keyOpponent = key
        if (remoteOpponentGame) {
            if (rg.setOpponentRemote(currentGamer)) {
                if (currentGamer.keyOpponent != "") {
                    return game(
                        gameStatus = gameStatus
                    )
                } else return null
            } else return null
        } else return null
    }

    override fun opponentsList(): List<Gamer> =
        if (remoteOpponentGame) rg.opponentsListRemote(currentGamer) ?: listOf()
        else
            listOf(
                Gamer(
                    keyGamer = "",
                    nikGamer = "Art Intelligence",
                    gameFieldSize = currentGamer.gameFieldSize
                )
            )

    override fun game(
        motionXIndex: Int ,
        motionYIndex: Int ,
        gameStatus: GameConstants.GameStatus
    ): Game {
        if (gameStatus == GameConstants.GameStatus.NEW_GAME ||
            gameStatus == GameConstants.GameStatus.NEW_GAME_FIRST_GAMER ||
            gameStatus == GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT ||
            gameStatus == GameConstants.GameStatus.NEW_GAME_ACCEPT
        ) {

            if (remoteOpponentGame) {
                if (gameStatus == GameConstants.GameStatus.NEW_GAME_ACCEPT) {
                    val opponentGame = rg.getGameOpponentRemote(currentGamer)
                    if (opponentGame != null) {
                        initGame(
                            gameStatusOld = opponentGame.gameStatus,
                            gameStatusNew = GameConstants.GameStatus.GAME_IS_ON,
                            previousGameStatus = currentGame.gameStatus,
                            gameFieldSizeNew = opponentGame.gameFieldSize
                        )
                        countTurnOpponent = opponentGame.countOfTurn
                    } else currentGame.gameStatus = GameConstants.GameStatus.ABORTED_GAME

                } else {
                    initGame(
                        gameStatusOld = gameStatus,
                        gameStatusNew = gameStatus,
                        previousGameStatus = currentGame.gameStatus,
                        gameFieldSizeNew = currentGamer.gameFieldSize
                    )
                    countTurnGamer = 1
                    currentGame.countOfTurn = countTurnGamer
                    rg.setGameOpponentRemote(
                        currentGamer,
                        currentGame
                    )
                    currentGame.gameStatus = GameConstants.GameStatus.GAME_IS_ON
                }
                return currentGame
            } else {
                initGame(
                    gameStatusOld = gameStatus,
                    gameStatusNew = GameConstants.GameStatus.GAME_IS_ON,
                    previousGameStatus = currentGame.gameStatus,
                    gameFieldSizeNew = currentGamer.gameFieldSize
                )
                currentGamer.gameFieldSize = currentGame.gameFieldSize
                if (currentGame.turnOfGamer) return currentGame
            }

        }

        if ((gameStatus == GameConstants.GameStatus.GAME_IS_ON &&
                    currentGame.gameStatus !in arrayOf(
                GameConstants.GameStatus.WIN_GAMER,
                GameConstants.GameStatus.DRAWN_GAME,
                GameConstants.GameStatus.WIN_OPPONENT,
                GameConstants.GameStatus.ABORTED_GAME
            ))
            ||
            gameStatus == GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT ||
            (gameStatus == GameConstants.GameStatus.NEW_GAME && !currentGame.turnOfGamer)
        ) {
            currentGame.gameStatus = GameConstants.GameStatus.GAME_IS_ON

            if (currentGame.turnOfGamer) {
                if (!checkHumanTurn(motionXIndex, motionYIndex, true)) return currentGame
                if (ai.checkWin(motionXIndex, motionYIndex, currentGame.turnOfGamer)) {
                    currentGame.motionXIndex = motionXIndex
                    currentGame.motionYIndex = motionYIndex
                    if (remoteOpponentGame) {
                        currentGame.countOfTurn = ++countTurnGamer
                        rg.setGameOpponentRemote(currentGamer, currentGame)
                    }
                    currentGame.gameStatus = GameConstants.GameStatus.WIN_GAMER
                    return currentGame
                }
                if (ai.isMapFull()) {
                    currentGame.motionXIndex = motionXIndex
                    currentGame.motionYIndex = motionYIndex
                    if (remoteOpponentGame) {
                        currentGame.countOfTurn = ++countTurnGamer
                        rg.setGameOpponentRemote(currentGamer, currentGame)
                    }
                    currentGame.gameStatus = GameConstants.GameStatus.DRAWN_GAME
                    return currentGame
                }

                if (remoteOpponentGame) {
                    currentGame.motionXIndex = motionXIndex
                    currentGame.motionYIndex = motionYIndex
                    currentGame.countOfTurn = ++countTurnGamer
                    rg.setGameOpponentRemote(currentGamer, currentGame)
                    currentGame.turnOfGamer = !currentGame.turnOfGamer
                    currentGame.motionXIndex = -1
                    currentGame.motionYIndex = -1
                    return currentGame
                }

                currentGame.turnOfGamer = !currentGame.turnOfGamer


            }
            if (!currentGame.turnOfGamer) {
                if (remoteOpponentGame) {
           //         currentGame.revertField()   ///??????????? HZ
                    val opponentGame = rg.getGameOpponentRemote(currentGamer)
                    if (opponentGame != null) {
                        if (opponentGame.countOfTurn > countTurnOpponent) {
                            countTurnOpponent = opponentGame.countOfTurn

                            if (checkHumanTurn(
                                    opponentGame.motionXIndex,
                                    opponentGame.motionYIndex,
                                    false
                                )
                            ) {
                                currentGame.gameStatus = opponentGame.gameStatus
                            } else return currentGame
                        } else return currentGame
                    } else return currentGame

                } else aiTurn()

                if (ai.checkWin(
                        currentGame.motionXIndex,
                        currentGame.motionYIndex,
                        currentGame.turnOfGamer
                    )
                ) {
                    currentGame.gameStatus = GameConstants.GameStatus.WIN_OPPONENT
                    if (remoteOpponentGame) rg.setGameOpponentRemote(currentGamer, currentGame)
                    return currentGame
                }
                if (ai.isMapFull()) {
                    currentGame.gameStatus = GameConstants.GameStatus.DRAWN_GAME
                    if (remoteOpponentGame) rg.setGameOpponentRemote(currentGamer, currentGame)
                    return currentGame
                }
                currentGame.turnOfGamer = !currentGame.turnOfGamer

            }
            return currentGame
        }
        currentGame.gameStatus = GameConstants.GameStatus.ABORTED_GAME
        if (remoteOpponentGame) rg.setGameOpponentRemote(currentGamer, currentGame)
        return currentGame
    }

    private fun initGame(
        gameStatusOld: GameConstants.GameStatus,
        gameStatusNew: GameConstants.GameStatus,
        previousGameStatus: GameConstants.GameStatus,
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
            GameConstants.GameStatus.NEW_GAME -> previousGameStatus == GameConstants.GameStatus.WIN_OPPONENT ||
                    previousGameStatus == GameConstants.GameStatus.NEW_GAME_FIRST_GAMER
            else -> false
        }
    }

    private fun checkHumanTurn(
        motionXIndex: Int,
        motionYIndex: Int,
        turnOfGamer: Boolean
    ): Boolean {
        if (motionXIndex >= 0 && motionYIndex >= 0) {
            if (!ai.humanTurn(motionXIndex, motionYIndex, turnOfGamer)) return false
            currentGame.motionXIndex = motionXIndex
            currentGame.motionYIndex = motionYIndex
            if (turnOfGamer) {
                currentGame.gameField[currentGame.motionYIndex][currentGame.motionXIndex] =
                    GameConstants.CellField.GAMER
            } else {
                currentGame.gameField[currentGame.motionYIndex][currentGame.motionXIndex] =
                    GameConstants.CellField.OPPONENT
            }
            return true
        } else return false
    }

    private fun aiTurn() {
        val xy = ai.aiTurn()
        currentGame.motionXIndex = xy.first
        currentGame.motionYIndex = xy.second
        currentGame.gameField[currentGame.motionYIndex][currentGame.motionXIndex] =
            GameConstants.CellField.OPPONENT
    }

}
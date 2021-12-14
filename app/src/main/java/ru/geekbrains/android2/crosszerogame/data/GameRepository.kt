package ru.geekbrains.android2.crosszerogame.data

import ru.geekbrains.android2.crosszerogame.data.ai.AI
import ru.geekbrains.android2.crosszerogame.data.remote.RemoteGamerFake

class GameRepository(val remoteOpponentGame: Boolean = false) {
    private var currentGame = Game(gameStatus = GameStatus.NEW_GAME_FIRST_GAMER, turnOfGamer = true)
    private var currentGamer = Gamer()
    private val ai = AI(remoteOpponentGame)
    private val rg = RemoteGamerFake()

    fun gamer(
        keyGamer: Int = 0,
        nikGamer: String = "gamer",
        gameFieldSize: Int = MIN_FIELD_SIZE,
        levelGamer: Int = 1,
        chipImageId: Int = 0,
        timeForTurn: Int = MIN_TIME_FOR_TURN
    ): Gamer {
         if (remoteOpponentGame) {
            currentGamer=rg.gamerRemote( keyGamer = keyGamer,
                nikGamer = nikGamer,
                gameFieldSize = gameFieldSize,
                levelGamer = levelGamer,
                chipImageId = chipImageId,
                timeForTurn = timeForTurn)
        } else {
             currentGamer = Gamer(gameFieldSize = gameFieldSize)
        }
        return currentGamer
    }

    fun opponentsList(keyGamer: Int) =
        listOf(Gamer(keyGamer = 0, nikGamer = "Art Intelligence", gameFieldSize = MIN_FIELD_SIZE))

    fun game(
        keyGame: Int = 0,
        motionXIndex: Int = -1,
        motionYIndex: Int = -1,
        gameStatus: GameStatus = GameStatus.NEW_GAME
    ): Game {
        if (gameStatus == GameStatus.NEW_GAME ||
            gameStatus == GameStatus.NEW_GAME_FIRST_GAMER ||
            gameStatus == GameStatus.NEW_GAME_FIRST_OPPONENT
        ) {
            initGame(gameStatus)
            if (currentGame.turnOfGamer) return currentGame
        }

        if ((gameStatus == GameStatus.GAME_IS_ON &&
                    currentGame.gameStatus !in arrayOf(
                GameStatus.WIN_GAMER,
                GameStatus.DRAWN_GAME,
                GameStatus.WIN_OPPONENT,
                GameStatus.ABORTED_GAME
            ))
            ||
            gameStatus == GameStatus.NEW_GAME_FIRST_OPPONENT ||
            (gameStatus == GameStatus.NEW_GAME && !currentGame.turnOfGamer)
        ) {
            currentGame.gameStatus = GameStatus.GAME_IS_ON

            if (currentGame.turnOfGamer) {
                if (!gamerTurn(motionXIndex, motionYIndex)) return currentGame
                if (ai.checkWin(motionXIndex, motionYIndex, currentGame.turnOfGamer)) {
                    currentGame.motionXIndex = -1
                    currentGame.motionYIndex = -1
                    currentGame.gameStatus = GameStatus.WIN_GAMER
                    return currentGame
                }
                if (ai.isMapFull()) {
                    currentGame.motionXIndex = -1
                    currentGame.motionYIndex = -1
                    currentGame.gameStatus = GameStatus.DRAWN_GAME
                    return currentGame
                }
                currentGame.turnOfGamer = !currentGame.turnOfGamer
            }
            if (!currentGame.turnOfGamer) {
                if (!opponentTurn(motionXIndex, motionYIndex)) return currentGame

                if (ai.checkWin(currentGame.motionXIndex, currentGame.motionYIndex, currentGame.turnOfGamer)) {
                    currentGame.gameStatus = GameStatus.WIN_OPPONENT
                    return currentGame
                }
                if (ai.isMapFull()) {

                    currentGame.gameStatus = GameStatus.DRAWN_GAME
                    return currentGame
                }
                currentGame.turnOfGamer = !currentGame.turnOfGamer
            }

            return currentGame
        }
        currentGame.gameStatus = GameStatus.ABORTED_GAME
        return currentGame
    }

    private fun initGame(gameStatus: GameStatus) {

        val dotsToWin = ai.initGame(currentGamer)

        val previousGameStatus = currentGame.gameStatus
        currentGame =
            Game(
                gameFieldSize = currentGamer.gameFieldSize,
                gameStatus = GameStatus.GAME_IS_ON,
                dotsToWin = dotsToWin
            )

        currentGame.turnOfGamer = when (gameStatus) {
            GameStatus.NEW_GAME_FIRST_GAMER -> true
            GameStatus.NEW_GAME -> previousGameStatus == GameStatus.WIN_OPPONENT || previousGameStatus == GameStatus.NEW_GAME_FIRST_GAMER
            else -> false
        }
    }

    private fun gamerTurn(motionXIndex: Int, motionYIndex: Int): Boolean {
        if (!ai.humanTurn(motionXIndex, motionYIndex, currentGame.turnOfGamer)) return false
        currentGame.motionXIndex = motionXIndex
        currentGame.motionYIndex = motionYIndex
        currentGame.gameField[currentGame.motionYIndex][currentGame.motionXIndex] = CellField.GAMER
        return true
    }

    private fun opponentTurn(motionXIndex: Int, motionYIndex: Int): Boolean {
        if (remoteOpponentGame) {
            if (!ai.humanTurn(motionXIndex, motionYIndex, currentGame.turnOfGamer)) return false
            currentGame.motionXIndex = motionXIndex
            currentGame.motionYIndex = motionYIndex
            currentGame.gameField[currentGame.motionYIndex][currentGame.motionXIndex] =
                CellField.OPPONENT
        } else {
            val xy = ai.aiTurn()
            currentGame.motionXIndex = xy.first
            currentGame.motionYIndex = xy.second
            currentGame.gameField[currentGame.motionYIndex][currentGame.motionXIndex] =
                CellField.OPPONENT
        }
        return true
    }

    companion object {
        const val MAX_FIELD_SIZE = 30
        const val MIN_FIELD_SIZE = 3
        const val DOTS_TO_WIN1_SIZE1 = 3
        const val DOTS_TO_WIN1_SIZE2 = 4
        const val DOTS_TO_WIN2_SIZE1 = 5
        const val DOTS_TO_WIN2_SIZE2 = 6
        const val DOTS_TO_WIN3_SIZE1 = 7
        const val DOTS_TO_WIN3_SIZE2 = 30
        const val DOTS_TO_WIN1 = 3
        const val DOTS_TO_WIN2 = 4
        const val DOTS_TO_WIN3 = 5
        const val MIN_TIME_FOR_TURN = 10
        const val MAX_TIME_FOR_TURN = 120
    }
}
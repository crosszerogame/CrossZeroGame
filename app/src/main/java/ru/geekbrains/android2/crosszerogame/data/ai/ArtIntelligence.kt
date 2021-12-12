package ru.geekbrains.android2.crosszerogame.data.ai

import ru.geekbrains.android2.crosszerogame.data.CellField
import ru.geekbrains.android2.crosszerogame.data.Game
import ru.geekbrains.android2.crosszerogame.data.GameStatus
import ru.geekbrains.android2.crosszerogame.data.Gamer

class ArtIntelligence {

    private var SIZE = MIN_FIELD_SIZE
    private var DOTS_TO_WIN = DOTS_TO_WIN1
    private var lastXDOT_X = 0
    private var lastYDOT_X = 0
    private var lastXDOT_O = 0
    private var lastYDOT_O = 0
    private var freedom = 0
    private var freedomWithFr = 0
    private var currentGame = Game(gameStatus = GameStatus.NEW_GAME_FIRST_GAMER, turnOfGamer = true)
    private var currentGamer = Gamer()

    private var map = Array(SIZE) { Array(SIZE) { DOT_EMPTY } }

    fun newGamer(gameFieldSize: Int): Gamer {
        currentGamer = Gamer(gameFieldSize = gameFieldSize)
        return currentGamer
    }

    fun getGamer(keyGamer: Int = 1) = currentGamer

    fun gamersList() =
        listOf(Gamer(keyGamer = 0, nikGamer = "Art Intelligence", gameFieldSize = SIZE))

    fun game(
        keyOpponent: Int = 0,
        keyGamer: Int = 1,
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
                if (!humanTurn(motionXIndex, motionYIndex)) return currentGame
                if (checkWin(DOT_X, lastXDOT_X, lastYDOT_X)) {
                    currentGame.motionXIndex = -1
                    currentGame.motionYIndex = -1
                    currentGame.gameStatus = GameStatus.WIN_GAMER
                    return currentGame
                }
                if (isMapFull()) {
                    currentGame.motionXIndex = -1
                    currentGame.motionYIndex = -1
                    currentGame.gameStatus = GameStatus.DRAWN_GAME
                    return currentGame
                }
                currentGame.turnOfGamer = !currentGame.turnOfGamer
            }
            if (!currentGame.turnOfGamer) {
                aiTurn()
                if (checkWin(DOT_O, lastXDOT_O, lastYDOT_O)) {
                    currentGame.gameStatus = GameStatus.WIN_OPPONENT
                    return currentGame
                }
                if (isMapFull()) {

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
        SIZE =
            if (currentGamer.gameFieldSize > MAX_FIELD_SIZE || currentGamer.gameFieldSize < MIN_FIELD_SIZE) MIN_FIELD_SIZE
            else currentGamer.gameFieldSize
        currentGamer.gameFieldSize = SIZE

        when (SIZE) {
            in DOTS_TO_WIN1_SIZE1..DOTS_TO_WIN1_SIZE2 -> DOTS_TO_WIN = DOTS_TO_WIN1
            in DOTS_TO_WIN2_SIZE1..DOTS_TO_WIN2_SIZE2 -> DOTS_TO_WIN = DOTS_TO_WIN2
            in DOTS_TO_WIN3_SIZE1..DOTS_TO_WIN3_SIZE2 -> DOTS_TO_WIN = DOTS_TO_WIN3
        }

        map = Array(SIZE) { Array(SIZE) { DOT_EMPTY } }

        val previousGameStatus = currentGame.gameStatus
        currentGame =
            Game(gameFieldSize = SIZE, gameStatus = GameStatus.GAME_IS_ON, dotsToWin = DOTS_TO_WIN)

        currentGame.turnOfGamer = when (gameStatus) {
            GameStatus.NEW_GAME_FIRST_GAMER -> true
            GameStatus.NEW_GAME -> previousGameStatus == GameStatus.WIN_OPPONENT || previousGameStatus == GameStatus.NEW_GAME_FIRST_GAMER
            else -> false
        }
    }

    private fun humanTurn(motionXIndex: Int, motionYIndex: Int): Boolean {
        if (!isCellValid(motionXIndex, motionYIndex)) return false
        map[motionYIndex][motionXIndex] = DOT_X
        lastXDOT_X = motionXIndex
        lastYDOT_X = motionYIndex
        currentGame.motionXIndex = lastXDOT_X
        currentGame.motionYIndex = lastYDOT_X
        currentGame.gameField[currentGame.motionYIndex][currentGame.motionXIndex] = CellField.GAMER
        return true
    }

    private fun isCellValid(x: Int, y: Int): Boolean {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) return false
        if (map[y][x] == DOT_EMPTY) return true
        return false
    }

    private fun checkWin(symb: Char, x: Int, y: Int): Boolean {
        return calcStep(symb, x, y) >= DOTS_TO_WIN
    }

    //для элемента массива с индексами x, y, для символа symb рассчитывается максимальное количество шагов до победы DOTS_TO_WIN - step ,
    // количество победных комбинаций - freedom, количество уже проставленных фищек, входящих в победные комбинации - freedomWithFr
    private fun calcStep(symb: Char, x: Int, y: Int): Int {
        var step = 0
        var lowX = 0
        var highX = 0
        var lowY = 0
        var highY = 0
        freedom = 0
        freedomWithFr = 0
        if (map[y][x] == DOT_EMPTY || map[y][x] == symb) {
            //вертикаль
            highY = y
            lowY = highY
            while (highY - y < DOTS_TO_WIN && highY < SIZE && (map[highY][x] == DOT_EMPTY || map[highY][x] == symb)) highY++
            highY--
            while (y - lowY < DOTS_TO_WIN && lowY >= 0 && (map[lowY][x] == DOT_EMPTY || map[lowY][x] == symb)) lowY--
            lowY++
            for (i in lowY..highY - DOTS_TO_WIN + 1) {
                var numberSymb = 0
                var numberEmpty = 0
                for (j in i until i + DOTS_TO_WIN) {
                    if (map[j][x] == symb) {
                        numberSymb++
                        freedomWithFr++
                    }
                    if (map[j][x] == DOT_EMPTY) numberEmpty++
                }
                if (numberSymb > 0 || numberEmpty > 0) freedom++
                if (numberSymb > step) step = numberSymb
            }
            //горизонталь
            highX = x
            lowX = highX
            while (highX - x < DOTS_TO_WIN && highX < SIZE && (map[y][highX] == DOT_EMPTY || map[y][highX] == symb)) highX++
            highX--
            while (x - lowX < DOTS_TO_WIN && lowX >= 0 && (map[y][lowX] == DOT_EMPTY || map[y][lowX] == symb)) lowX--
            lowX++
            for (i in lowX..highX - DOTS_TO_WIN + 1) {
                var numberSymb = 0
                var numberEmpty = 0
                for (j in i until i + DOTS_TO_WIN) {
                    if (map[y][j] == symb) {
                        numberSymb++
                        freedomWithFr++
                    }
                    if (map[y][j] == DOT_EMPTY) numberEmpty++
                }
                if (numberSymb > 0 || numberEmpty > 0) freedom++
                if (numberSymb > step) step = numberSymb
            }

            //1-я диагональ
            highX = x
            lowX = highX
            highY = y
            lowY = highY
            while (highX - x < DOTS_TO_WIN && highX < SIZE && highY - y < DOTS_TO_WIN && highY < SIZE && (map[highY][highX] == DOT_EMPTY || map[highY][highX] == symb)) {
                highX++
                highY++
            }
            highX--
            highY--
            while (x - lowX < DOTS_TO_WIN && lowX >= 0 && y - lowY < DOTS_TO_WIN && lowY >= 0 && (map[lowY][lowX] == DOT_EMPTY || map[lowY][lowX] == symb)) {
                lowX--
                lowY--
            }
            lowX++
            lowY++
            run {
                var i = lowX
                var k = lowY
                while (i <= highX - DOTS_TO_WIN + 1 && k <= highY - DOTS_TO_WIN + 1) {
                    var numberSymb = 0
                    var numberEmpty = 0
                    var j = i
                    var l = k
                    while (j < i + DOTS_TO_WIN && l < k + DOTS_TO_WIN) {
                        if (map[l][j] == symb) {
                            numberSymb++
                            freedomWithFr++
                        }
                        if (map[l][j] == DOT_EMPTY) numberEmpty++
                        j++
                        l++
                    }
                    if (numberSymb > 0 || numberEmpty > 0) freedom++
                    if (numberSymb > step) step = numberSymb
                    i++
                    k++
                }
            }

            //2-я диагональ
            highX = x
            lowX = highX
            highY = y
            lowY = highY
            while (highX - x < DOTS_TO_WIN && highX < SIZE && y - highY < DOTS_TO_WIN && highY >= 0 && (map[highY][highX] == DOT_EMPTY || map[highY][highX] == symb)) {
                highX++
                highY--
            }
            highX--
            highY++
            while (x - lowX < DOTS_TO_WIN && lowX >= 0 && lowY - y < DOTS_TO_WIN && lowY < SIZE && (map[lowY][lowX] == DOT_EMPTY || map[lowY][lowX] == symb)) {
                lowX--
                lowY++
            }
            lowX++
            lowY--
            var i = lowX
            var k = lowY
            while (i <= highX - DOTS_TO_WIN + 1 && k >= highY + DOTS_TO_WIN - 1) {
                var numberSymb = 0
                var numberEmpty = 0
                var j = i
                var l = k
                while (j < i + DOTS_TO_WIN && l > k - DOTS_TO_WIN) {
                    if (map[l][j] == symb) {
                        numberSymb++
                        freedomWithFr++
                    }
                    if (map[l][j] == DOT_EMPTY) numberEmpty++
                    j++
                    l--
                }
                if (numberSymb > 0 || numberEmpty > 0) freedom++
                if (numberSymb > step) step = numberSymb
                i++
                k--
            }
        }
        return step
    }

    // у противника выбирает элемент с максимальным количеством шагов до победы - stepXmax,
// при равных значениях stepXmax выбирает элемент с максимальным количеством проставленных фищек, входящих в победные комбинации - freedomWithFrXmax,
// при равных значениях freedomWithFrXmax выбирает элемент с максимальным количествоv победных комбинаций - freedomXmax
// у себя выбирает элемент с максимальным количеством шагов до победы - stepOmax,
// при равных значениях stepOmax выбирает элемент с максимальным количеством проставленных фищек, входящих в победные комбинации - freedomWithFrOmax,
// при равных значениях freedomWithFrOmax выбирает элемент с максимальным количествоv победных комбинаций - freedomOmax
// для хода, если  stepOmax >= stepXmax, выбирается лучший элемент для себя, иначе выбирается лучший элемент для противника
    fun aiTurn() {
        val x: Int
        val y: Int
        var stepX: Int
        var stepO: Int
        var stepXmax: Int
        var stepOmax: Int
        var xX = 0
        var yX = 0
        var xO = 0
        var yO = 0
        var freedomXmax: Int
        var freedomOmax: Int
        var freedomWithFrXmax: Int
        var freedomWithFrOmax: Int
        stepXmax = 0
        freedomXmax = 0
        stepOmax = 0
        freedomOmax = 0
        freedomWithFrXmax = 0
        freedomWithFrOmax = 0
        for (i in 0 until SIZE) {
            for (j in 0 until SIZE) {
                if (map[i][j] == DOT_EMPTY) {
                    stepX = calcStep(DOT_X, j, i)
                    if (stepX > stepXmax) {
                        stepXmax = stepX
                        freedomXmax = freedom
                        freedomWithFrXmax = freedomWithFr
                        xX = j
                        yX = i
                    } else if (stepX == stepXmax && freedomWithFr > freedomWithFrXmax) {
                        freedomWithFrXmax = freedomWithFr
                        freedomXmax = freedom
                        xX = j
                        yX = i
                    } else if (stepX == stepXmax && freedomWithFr == freedomWithFrXmax && freedom > freedomXmax) {
                        freedomXmax = freedom
                        xX = j
                        yX = i
                    }
                    stepO = calcStep(DOT_O, j, i)
                    if (stepO > stepOmax) {
                        stepOmax = stepO
                        freedomOmax = freedom
                        xO = j
                        yO = i
                    } else if (stepO == stepOmax && freedomWithFr > freedomWithFrOmax) {
                        freedomWithFrOmax = freedomWithFr
                        freedomOmax = freedom
                        xO = j
                        yO = i
                    } else if (stepO == stepOmax && freedomWithFr == freedomWithFrOmax && freedom > freedomOmax) {
                        freedomOmax = freedom
                        xO = j
                        yO = i
                    }
                }
            }
        }
        if (stepOmax >= stepXmax) {
            x = xO
            y = yO
        } else {
            x = xX
            y = yX
        }
        map[y][x] = DOT_O
        lastXDOT_O = x
        lastYDOT_O = y
        currentGame.motionXIndex = lastXDOT_O
        currentGame.motionYIndex = lastYDOT_O
        currentGame.gameField[currentGame.motionYIndex][currentGame.motionXIndex] =
            CellField.OPPONENT
    }

    private fun isMapFull(): Boolean {
        for (i in 0 until SIZE) {
            for (j in 0 until SIZE) {
                if (map[i][j] == DOT_EMPTY) return false
            }
        }
        return true
    }

    companion object {
        private const val DOT_EMPTY = '•'
        private const val DOT_X = 'X'  //игрок
        private const val DOT_O = 'O'  //ИИ
        const val MAX_FIELD_SIZE = 30
        const val MIN_FIELD_SIZE = 3
        const val DOTS_TO_WIN1_SIZE1 = 3
        const val DOTS_TO_WIN1_SIZE2 = 4
        const val DOTS_TO_WIN2_SIZE1 = 5
        const val DOTS_TO_WIN2_SIZE2 = 9
        const val DOTS_TO_WIN3_SIZE1 = 10
        const val DOTS_TO_WIN3_SIZE2 = 30
        const val DOTS_TO_WIN1 = 3
        const val DOTS_TO_WIN2 = 4
        const val DOTS_TO_WIN3 = 5
    }
}
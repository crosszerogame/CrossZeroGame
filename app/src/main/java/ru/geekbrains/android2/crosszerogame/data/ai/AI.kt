package ru.geekbrains.android2.crosszerogame.data.ai

import ru.geekbrains.android2.crosszerogame.data.*

class AI() {
    private var SIZE = GameConstants.MIN_FIELD_SIZE
    private var DOTS_TO_WIN = GameConstants.DOTS_TO_WIN1
    private var lastXDOT_X = 0
    private var lastYDOT_X = 0
    private var lastXDOT_O = 0
    private var lastYDOT_O = 0
    private var freedom = 0
    private var freedomWithFr = 0

    private var map = Array(SIZE) { Array(SIZE) { DOT_EMPTY } }

    fun initGame(gameFieldSize: Int): Pair<Int, Int> {
        val pair = dotsToWin(gameFieldSize)
        map = Array(pair.first) { Array(pair.first) { DOT_EMPTY } }

        return Pair(SIZE, DOTS_TO_WIN)
    }

    fun dotsToWin(gameFieldSize: Int): Pair<Int, Int> {
        SIZE =
            if (gameFieldSize > GameConstants.MAX_FIELD_SIZE || gameFieldSize < GameConstants.MIN_FIELD_SIZE) GameConstants.MIN_FIELD_SIZE
            else gameFieldSize

        when (SIZE) {
            in GameConstants.DOTS_TO_WIN1_SIZE1..GameConstants.DOTS_TO_WIN1_SIZE2 -> DOTS_TO_WIN =
                GameConstants.DOTS_TO_WIN1
            in GameConstants.DOTS_TO_WIN2_SIZE1..GameConstants.DOTS_TO_WIN2_SIZE2 -> DOTS_TO_WIN =
                GameConstants.DOTS_TO_WIN2
            in GameConstants.DOTS_TO_WIN3_SIZE1..GameConstants.DOTS_TO_WIN3_SIZE2 -> DOTS_TO_WIN =
                GameConstants.DOTS_TO_WIN3
        }
        return Pair(SIZE, DOTS_TO_WIN)
    }

    fun humanTurn(motionXIndex: Int, motionYIndex: Int, turnOfGamer: Boolean): Boolean {
        if (!isCellValid(motionXIndex, motionYIndex)) return false
        if (turnOfGamer) map[motionYIndex][motionXIndex] = DOT_X
        else map[motionYIndex][motionXIndex] = DOT_O
        lastXDOT_X = motionXIndex
        lastYDOT_X = motionYIndex
        return true
    }

    private fun isCellValid(x: Int, y: Int): Boolean {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) return false
        if (map[y][x] == DOT_EMPTY) return true
        return false
    }

    fun checkWin(x: Int, y: Int, turnOfGamer: Boolean): Boolean {
        return calcStep(if (turnOfGamer) DOT_X else DOT_O, x, y) >= DOTS_TO_WIN
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
    fun aiTurn(): Pair<Int, Int> {
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
        return Pair(x, y)
    }

    fun isMapFull(): Boolean {
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

    }
}
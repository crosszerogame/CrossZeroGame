package ru.geekbrains.android2.crosszerogame.game

import ru.geekbrains.android2.crosszerogame.structure.GameRepository
import ru.geekbrains.android2.crosszerogame.structure.data.Cell

class GameRepositoryImpl : GameRepository {
    companion object {
        private const val MIN_FIELD_SIZE = 3
        private const val MAX_FIELD_SIZE = 30
        private const val DEFAULT_FIELD_SIZE = MIN_FIELD_SIZE
        private const val DEFAULT_CHIPS_FOR_WIN = 3

        data class RangeWin(val minSize: Int, val maxSize: Int, val chipsForWin: Int)

        private var rangesWin = listOf<RangeWin>()
            get() {
                if (field.isEmpty())
                    initRangesWin()
                return field
            }

        private fun initRangesWin() {
            rangesWin = listOf(
                RangeWin(MIN_FIELD_SIZE, 4, 3),
                RangeWin(5, 9, 4),
                RangeWin(10, MAX_FIELD_SIZE, 5)
            )
        }
    }

    override var chipsForWin: Int = DEFAULT_CHIPS_FOR_WIN

    override fun getCell(x: Int, y: Int) = field[y][x]

    private var field: Array<Array<Cell>> = Array(DEFAULT_FIELD_SIZE) {
        Array(DEFAULT_FIELD_SIZE) { Cell.EMPTY }
    }

    override suspend fun newGame(fieldSize: Int) {
        field = Array(fieldSize) {
            Array(fieldSize) { Cell.EMPTY }
        }
        chipsForWin = getChipsForWin(fieldSize)
    }

    override fun getChipsForWin(fieldSize: Int): Int {
        for (range in rangesWin) {
            if (fieldSize in range.minSize..range.maxSize)
                return range.chipsForWin
        }
        return DEFAULT_CHIPS_FOR_WIN
    }

    override suspend fun pasteCross(x: Int, y: Int): GameRepository.Result =
        pasteChip(x, y, Cell.CROSS)

    override suspend fun pasteZero(x: Int, y: Int): GameRepository.Result =
        pasteChip(x, y, Cell.ZERO)

    private suspend fun pasteChip(x: Int, y: Int, chip: Cell): GameRepository.Result {
        if (isValidCell(x, y).not())
            return GameRepository.Result.NOT_PASTE
        field[y][x] = chip
        if (isWin(chip, x, y))
            return GameRepository.Result.WIN
        if (isDrawn())
            return GameRepository.Result.DRAWN
        return GameRepository.Result.CONTINUE
    }

    private fun isValidCell(x: Int, y: Int): Boolean =
        x < field.size && y < field.size && x > -1 && y > -1 && field[y][x] == Cell.EMPTY

    private suspend fun isWin(chip: Cell, x: Int, y: Int): Boolean =
        calcStep(chip, x, y).step >= chipsForWin

    private fun isDrawn(): Boolean {
        for (column in field) {
            for (cell in column) {
                if (cell == Cell.EMPTY)
                    return false
            }
        }
        return true
    }

    // Функция Игоря Алексеенко:
// для элемента массива с индексами x, y, для символа symb рассчитывается максимальное количество шагов до победы chipsForWin - step ,
// количество победных комбинаций - freedom, количество уже проставленных фищек, входящих в победные комбинации - freedomWithFr
    override suspend fun calcStep(chip: Cell, x: Int, y: Int): GameRepository.Step {
        val size = field.size
        var step = 0
        var lowX: Int
        var highX: Int
        var lowY: Int
        var highY: Int
        var freedom = 0
        var freedomWithFr = 0
        if (field[y][x] == Cell.EMPTY || field[y][x] == chip) {
            //вертикаль
            highY = y
            lowY = highY
            while (highY - y < chipsForWin && highY < size && (field[highY][x] == Cell.EMPTY || field[highY][x] == chip)) highY++
            highY--
            while (y - lowY < chipsForWin && lowY >= 0 && (field[lowY][x] == Cell.EMPTY || field[lowY][x] == chip)) lowY--
            lowY++
            for (i in lowY..highY - chipsForWin + 1) {
                var numberSymb = 0
                var numberEmpty = 0
                for (j in i until i + chipsForWin) {
                    if (field[j][x] == chip) {
                        numberSymb++
                        freedomWithFr++
                    }
                    if (field[j][x] == Cell.EMPTY) numberEmpty++
                }
                if (numberSymb > 0 || numberEmpty > 0) freedom++
                if (numberSymb > step) step = numberSymb
            }
            //горизонталь
            highX = x
            lowX = highX
            while (highX - x < chipsForWin && highX < size && (field[y][highX] == Cell.EMPTY || field[y][highX] == chip)) highX++
            highX--
            while (x - lowX < chipsForWin && lowX >= 0 && (field[y][lowX] == Cell.EMPTY || field[y][lowX] == chip)) lowX--
            lowX++
            for (i in lowX..highX - chipsForWin + 1) {
                var numberSymb = 0
                var numberEmpty = 0
                for (j in i until i + chipsForWin) {
                    if (field[y][j] == chip) {
                        numberSymb++
                        freedomWithFr++
                    }
                    if (field[y][j] == Cell.EMPTY) numberEmpty++
                }
                if (numberSymb > 0 || numberEmpty > 0) freedom++
                if (numberSymb > step) step = numberSymb
            }

            //1-я диагональ
            highX = x
            lowX = highX
            highY = y
            lowY = highY
            while (highX - x < chipsForWin && highX < size && highY - y < chipsForWin && highY < size && (field[highY][highX] == Cell.EMPTY || field[highY][highX] == chip)) {
                highX++
                highY++
            }
            highX--
            highY--
            while (x - lowX < chipsForWin && lowX >= 0 && y - lowY < chipsForWin && lowY >= 0 && (field[lowY][lowX] == Cell.EMPTY || field[lowY][lowX] == chip)) {
                lowX--
                lowY--
            }
            lowX++
            lowY++
            run {
                var i = lowX
                var k = lowY
                while (i <= highX - chipsForWin + 1 && k <= highY - chipsForWin + 1) {
                    var numberSymb = 0
                    var numberEmpty = 0
                    var j = i
                    var l = k
                    while (j < i + chipsForWin && l < k + chipsForWin) {
                        if (field[l][j] == chip) {
                            numberSymb++
                            freedomWithFr++
                        }
                        if (field[l][j] == Cell.EMPTY) numberEmpty++
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
            while (highX - x < chipsForWin && highX < size && y - highY < chipsForWin && highY >= 0 && (field[highY][highX] == Cell.EMPTY || field[highY][highX] == chip)) {
                highX++
                highY--
            }
            highX--
            highY++
            while (x - lowX < chipsForWin && lowX >= 0 && lowY - y < chipsForWin && lowY < size && (field[lowY][lowX] == Cell.EMPTY || field[lowY][lowX] == chip)) {
                lowX--
                lowY++
            }
            lowX++
            lowY--
            var i = lowX
            var k = lowY
            while (i <= highX - chipsForWin + 1 && k >= highY + chipsForWin - 1) {
                var numberSymb = 0
                var numberEmpty = 0
                var j = i
                var l = k
                while (j < i + chipsForWin && l > k - chipsForWin) {
                    if (field[l][j] == chip) {
                        numberSymb++
                        freedomWithFr++
                    }
                    if (field[l][j] == Cell.EMPTY) numberEmpty++
                    j++
                    l--
                }
                if (numberSymb > 0 || numberEmpty > 0) freedom++
                if (numberSymb > step) step = numberSymb
                i++
                k--
            }
        }
        return GameRepository.Step(step, freedom, freedomWithFr)
    }
}
package ru.geekbrains.android2.crosszerogame.structure

import ru.geekbrains.android2.crosszerogame.structure.data.Cell

interface GameRepository {
    enum class Result {
        NOT_PASTE, CONTINUE, WIN, DRAWN
    }

    data class Step(
        val step: Int,
        val freedom: Int,
        val freedomWithFr: Int,
    )

    val chipsForWin: Int
    fun getCell(x: Int, y: Int): Cell
    suspend fun newGame(fieldSize: Int)
    fun getChipsForWin(fieldSize: Int): Int
    suspend fun pasteCross(x: Int, y: Int): Result
    suspend fun pasteZero(x: Int, y: Int): Result
    suspend fun calcStep(chip: Cell, x: Int, y: Int): Step
}
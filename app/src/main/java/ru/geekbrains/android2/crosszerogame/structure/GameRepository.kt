package ru.geekbrains.android2.crosszerogame.structure

import ru.geekbrains.android2.crosszerogame.structure.state.CellState
import ru.geekbrains.android2.crosszerogame.structure.state.GameState

interface GameRepository {
    val iIsCross: Boolean
    val dotsForWin: Int
    val field: Array<Array<CellState>>
    val status: GameState
    fun newGame(fieldSize: Int, iIsCross: Boolean)
    fun pasteCross(x: Int, y: Int): Boolean // true если ход корректный,
    fun pasteZero(x: Int, y: Int): Boolean  // false если ячейка занята или игра окончена
}
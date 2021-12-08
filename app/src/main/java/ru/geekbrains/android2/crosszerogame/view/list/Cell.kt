package ru.geekbrains.android2.crosszerogame.view.list

import android.graphics.Point

data class Cell(
    val coord: Point,
    var value: CellValue = CellValue.EMPTY
)

enum class CellValue {
    EMPTY, CROSS, ZERO
}
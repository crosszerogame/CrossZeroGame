package ru.geekbrains.android2.crosszerogame.view.list

data class Cell(
    val value: Value,
    val position: Position
    ) {
    enum class Value {
        EMPTY, CROSS, ZERO
    }
    enum class Position {
        CENTER, TOP, BOTTOM, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, LEFT, RIGHT
    }
}
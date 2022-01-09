package ru.geekbrains.android2.crosszerogame.model.repository

import com.google.gson.Gson
import org.json.JSONArray
import ru.geekbrains.android2.crosszerogame.data.CellField
import ru.geekbrains.android2.crosszerogame.data.GameStatus


// var gameStatus: GameStatus = GameStatus.NEW_GAME


fun convertToString(gameStatus: GameStatus): String =
    when (gameStatus) {
        GameStatus.GAME_IS_ON -> "GAME_IS_ON"
        GameStatus.WIN_GAMER -> "WIN_GAMER"
        GameStatus.WIN_OPPONENT -> "WIN_OPPONENT"
        GameStatus.DRAWN_GAME -> "DRAWN_GAME"
        GameStatus.ABORTED_GAME -> "ABORTED_GAME"
        GameStatus.NEW_GAME -> "NEW_GAME"
        GameStatus.NEW_GAME_FIRST_GAMER -> "NEW_GAME_FIRST_GAMER"
        GameStatus.NEW_GAME_FIRST_OPPONENT -> "NEW_GAME_FIRST_OPPONENT"
    }

fun convertToGameStatus(str: String): GameStatus =
    when (str) {
        "GAME_IS_ON" -> GameStatus.GAME_IS_ON
        "WIN_GAMER" -> GameStatus.WIN_GAMER
        "WIN_OPPONENT" -> GameStatus.WIN_OPPONENT
        "DRAWN_GAME" -> GameStatus.DRAWN_GAME
        "ABORTED_GAME" -> GameStatus.ABORTED_GAME
        "NEW_GAME" -> GameStatus.NEW_GAME
        "NEW_GAME_FIRST_GAMER" -> GameStatus.NEW_GAME_FIRST_GAMER
        "NEW_GAME_FIRST_OPPONENT" -> GameStatus.NEW_GAME_FIRST_OPPONENT
        else -> GameStatus.ABORTED_GAME
    }


fun convertToString(gameField: Array<Array<CellField>>): String =
    Gson().toJson(gameField)

fun convertToCellFieldMatrix(str: String): Array<Array<CellField>> {
    var jsonArr = JSONArray(str)
    var gameField: Array<Array<CellField>> =
        Array(jsonArr.length()) { Array(jsonArr.length()) { CellField.EMPTY } }

    for (i in 0 until jsonArr.length()) {
        for (j in 0 until jsonArr.length()) {
            val a = toArray(jsonArr[i].toString())
            gameField[i][j] = convertToCellField(a[j])
        }
    }
    return gameField
}

private fun convertToCellField(str: String): CellField {
    var field = CellField.EMPTY
    when (str) {
        "GAMER" -> field = CellField.GAMER
        "OPPONENT" -> field = CellField.OPPONENT
        "EMPTY" -> field = CellField.EMPTY
    }
    return field
}

private fun toArray(str: String): List<String> {
    val arrayStr = mutableListOf<String>()
    var s = ""
    var bool = false
    val array = str.toCharArray()
    for (i in 0 until array.size) {
        if (array[i] == '"') {
            bool = bool == false
        }
        if (bool) {
            s += array[i]
        } else {
            if (s != "") {
                val re = Regex("[^A-Za-z0-9 ]")
                s = re.replace(s, "")
                arrayStr.add(s)
            }
            s = ""
        }
    }
    return arrayStr
}
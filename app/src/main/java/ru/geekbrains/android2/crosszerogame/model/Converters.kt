package ru.geekbrains.android2.crosszerogame.model

import ru.geekbrains.android2.crosszerogame.data.Game
import ru.geekbrains.android2.crosszerogame.data.Gamer
import ru.geekbrains.android2.crosszerogame.model.localdb.GameEntity
import ru.geekbrains.android2.crosszerogame.model.localdb.GamerEntity

fun convertToGamerEntity(gamer: Gamer) =
    GamerEntity(
        gamer.nikGamer,
        gamer.nikGamer,
        gamer.gameFieldSize,
        gamer.levelGamer,
        gamer.chipImageId,
        2,
        "gamer.keyOpponent",
        "gamer.keyGame",
        true
    )

//fun convertToGamer(gamerEntity: GamerEntity) =
//    Gamer(
//        gamerEntity.keyGamer.toInt(),
//        gamerEntity.nikGamer,
//        gamerEntity.gameFieldSize,
//        gamerEntity.levelGamer,
//        gamerEntity.chipImageId
//    )

fun convertToGameEntity(game: Game) =
    GameEntity(
        "keyGame",
        3,
        game.gameField.toString(),
        game.motionXIndex,
        game.motionYIndex,
        game.gameStatus.toString(),
        game.dotsToWin,
        game.turnOfGamer,
        1
    )

/*
fun convertToGame(gameEntity: GameEntity): Game {
    // TODO
}
*/
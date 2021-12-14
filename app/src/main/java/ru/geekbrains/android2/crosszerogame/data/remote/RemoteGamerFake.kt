package ru.geekbrains.android2.crosszerogame.data.remote

import ru.geekbrains.android2.crosszerogame.data.Game
import ru.geekbrains.android2.crosszerogame.data.GameRepository
import ru.geekbrains.android2.crosszerogame.data.Gamer

class RemoteGamerFake {

    private var listGamers = mutableListOf<Gamer>()
    private var listGames = mutableListOf<Game>()

    fun gamerRemote(
        keyGamer: Int = 0,
        nikGamer: String = "gamer",
        gameFieldSize: Int = GameRepository.MIN_FIELD_SIZE,
        levelGamer: Int = 1,
        chipImageId: Int = 0,
        timeForTurn: Int = GameRepository.MIN_TIME_FOR_TURN
    ): Gamer {
        val gamer = Gamer(
            keyGamer = listGamers.size + 1,
            nikGamer = nikGamer,
            gameFieldSize = gameFieldSize,
            levelGamer = levelGamer,
            chipImageId = chipImageId,
            timeForTurn = timeForTurn
        )
        return if (keyGamer==0) {
            listGamers.add(gamer)
            gamer
        } else {
            listGamers[keyGamer-1]=gamer
            listGamers[keyGamer-1]
        }
    }

}
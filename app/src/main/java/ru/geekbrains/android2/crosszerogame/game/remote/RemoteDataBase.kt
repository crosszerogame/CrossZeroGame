package ru.geekbrains.android2.crosszerogame.game.remote

interface RemoteDataBase {
    suspend fun postGamer(gamer: Gamer): String?
    suspend fun deleteGamer(key: String): Boolean
    suspend fun getGamer(key: String): Gamer?
    suspend fun listGamer(): List<Gamer>?

    suspend fun postGame(game: GameRemote): String?
    suspend fun deleteGame(key: String): Boolean
    suspend fun getGame(key: String): GameRemote?
    suspend fun listGame(): List<GameRemote>?
}
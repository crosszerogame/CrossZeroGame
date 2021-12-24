package ru.geekbrains.android2.crosszerogame.model.di

import dagger.Module
import dagger.Provides
import ru.geekbrains.android2.crosszerogame.model.localdb.GameDao
import ru.geekbrains.android2.crosszerogame.model.localdb.GamerDao
import ru.geekbrains.android2.crosszerogame.model.repository.Repo

@Module
class RepoModule {

    @Provides
    internal fun provideRepo(gameDao: GameDao, gamerDao: GamerDao) =
        Repo(gameDao, gamerDao)

}
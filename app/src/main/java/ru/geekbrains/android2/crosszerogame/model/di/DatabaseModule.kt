package ru.geekbrains.android2.crosszerogame.model.di

import androidx.room.Room
import dagger.Module
import dagger.Provides
import ru.geekbrains.android2.crosszerogame.App
import ru.geekbrains.android2.crosszerogame.model.localdb.Database
import ru.geekbrains.android2.crosszerogame.model.localdb.GameDao
import ru.geekbrains.android2.crosszerogame.model.localdb.GamerDao
import javax.inject.Singleton

@Module
class DatabaseModule {

    @Provides
    @Singleton
    internal fun createDatabase(app: App): Database {
        return Room.databaseBuilder(app, Database::class.java, "_db10")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    internal fun provideGamerDao(app: App): GamerDao = createDatabase(app).gamerDao()

    @Provides
    @Singleton
    internal fun provideGameDao(app: App): GameDao = createDatabase(app).gameDao()

}
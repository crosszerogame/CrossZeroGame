package ru.geekbrains.android2.crosszerogame.model.di

import dagger.Module
import dagger.Provides
import ru.geekbrains.android2.crosszerogame.App

@Module
class AppModule(val app: App) {

    @Provides
    fun app(): App = app

}
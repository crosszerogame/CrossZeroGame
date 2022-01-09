package ru.geekbrains.android2.crosszerogame.model.di

import dagger.Component
import ru.geekbrains.android2.crosszerogame.viewmodel.GameModel
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        DatabaseModule::class,
        RepoModule::class

//        ApiModule::class,
//        RepoModule::class,
//        DatabaseModule::class,
//        SchedulerModule::class
    ]
)
interface AppComponent {

    fun inject(gameModel: GameModel)

}
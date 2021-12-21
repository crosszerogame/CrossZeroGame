package ru.geekbrains.android2.crosszerogame

import android.app.Application
import com.parse.Parse
import ru.geekbrains.android2.crosszerogame.model.di.AppComponent
import ru.geekbrains.android2.crosszerogame.model.di.AppModule
import ru.geekbrains.android2.crosszerogame.model.di.DaggerAppComponent

class App : Application() {

    companion object {
        lateinit var instance: App
    }

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        instance = this

        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()

        Parse.initialize(
            Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.back4app_app_id))
                .clientKey(getString(R.string.back4app_client_key))
                .server(getString(R.string.back4app_server_url))
                .build());
    }
}
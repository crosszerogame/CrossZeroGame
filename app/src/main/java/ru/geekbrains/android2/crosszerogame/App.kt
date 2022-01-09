package ru.geekbrains.android2.crosszerogame

import android.app.Application
import com.parse.Parse
import ru.geekbrains.android2.crosszerogame.data.GameRepositoryImpl
import ru.geekbrains.android2.crosszerogame.data.remote.back4app.CrossZeroDBImpl

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Parse.initialize(
            Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.back4app_app_id))
                .clientKey(getString(R.string.back4app_client_key))
                .server(getString(R.string.back4app_server_url))
                .build()
        )
        db = CrossZeroDBImpl()
        gr = GameRepositoryImpl(true, db)
        grAi = GameRepositoryImpl(false, db)
    }

    companion object {
        lateinit var db: CrossZeroDBImpl
        lateinit var gr: GameRepositoryImpl
        lateinit var grAi: GameRepositoryImpl
    }
}
package ru.geekbrains.android2.crosszerogame.model

import retrofit2.http.GET
import retrofit2.http.Query

interface Api {

    @GET("classes/MyCustomClassName")
    fun get() {

    }


    /*
        @GET("dicservice.json/lookup?")
    fun getWordTranslation(
        @Query("key") key: String,
        @Query("lang") language: String,
        @Query("text") word: String
    ): Single<WordTranslationServerResponse>
     */


}
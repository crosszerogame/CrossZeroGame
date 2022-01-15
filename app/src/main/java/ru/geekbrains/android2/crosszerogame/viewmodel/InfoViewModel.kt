package ru.geekbrains.android2.crosszerogame.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.geekbrains.android2.crosszerogame.utils.Event

class InfoViewModel : ViewModel() {

    var currentSlideMumber: Int = 1

    val eventNextSlide = MutableLiveData<Event<Int>>()

    val eventStartProject = MutableLiveData<Event<Boolean>>()

    fun showNextSlide(numberOfSlides: Int) {
        if (currentSlideMumber < numberOfSlides) {
            eventNextSlide.postValue(Event(currentSlideMumber++))
        } else {
            eventStartProject.postValue(Event(true))
        }
    }

    fun skipInfoSlides() {
        eventStartProject.postValue(Event(true))
    }

}
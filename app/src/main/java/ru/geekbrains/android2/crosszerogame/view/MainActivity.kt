package ru.geekbrains.android2.crosszerogame.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentTransaction
import ru.geekbrains.android2.crosszerogame.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            val fragment = GameFragment()
            fragmentTransaction.replace(R.id.container, fragment).commit()
        }
    }
}
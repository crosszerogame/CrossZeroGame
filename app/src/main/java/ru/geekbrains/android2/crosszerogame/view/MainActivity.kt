package ru.geekbrains.android2.crosszerogame.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomsheet.BottomSheetBehavior
import ru.geekbrains.android2.crosszerogame.R

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG_GAME = "game"
    }
    private lateinit var bottomSheet: BottomSheetBehavior<FrameLayout>
    private val showSettings = {
        bottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottomSheet = BottomSheetBehavior.from(findViewById(R.id.bottom_container))

        if (savedInstanceState == null) {
            val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            val game = GameFragment()
            game.onMessageAction = showSettings
            fragmentTransaction.replace(R.id.container, game, TAG_GAME)
                .replace(R.id.bottom_container, SettingsFragment()).commit()
        } else {
            bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
            supportFragmentManager.findFragmentByTag(TAG_GAME)?.let {
                if (it is GameFragment)
                    it.onMessageAction = showSettings
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val item = menu.add(R.string.settings)
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        item.icon = ContextCompat.getDrawable(this, R.drawable.ic_settings)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (bottomSheet.state == BottomSheetBehavior.STATE_HIDDEN)
            bottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
        else
            bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        supportFragmentManager.findFragmentByTag(TAG_GAME)?.let {
            if (it is BackEvent && it.onBack())
                return
        }
        if (bottomSheet.state == BottomSheetBehavior.STATE_HIDDEN)
            super.onBackPressed()
        else
            bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
    }
}
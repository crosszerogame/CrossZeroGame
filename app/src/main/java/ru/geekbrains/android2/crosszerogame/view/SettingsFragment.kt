package ru.geekbrains.android2.crosszerogame.view

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.layout_remote_connector.*
import ru.geekbrains.android2.crosszerogame.R
import ru.geekbrains.android2.crosszerogame.xdata.Gamer
import ru.geekbrains.android2.crosszerogame.databinding.FragmentSettingsBinding
import ru.geekbrains.android2.crosszerogame.utils.SettingsImpl
import ru.geekbrains.android2.crosszerogame.utils.strings.GameStrings
import ru.geekbrains.android2.crosszerogame.utils.strings.SettingsStrings
import ru.geekbrains.android2.crosszerogame.view.list.OpponentsAdapter
import ru.geekbrains.android2.crosszerogame.viewmodel.SettingsModel
import ru.geekbrains.android2.crosszerogame.viewmodel.SettingsState
import ru.geekbrains.android2.crosszerogame.xdata.GameConstants

class SettingsFragment : Fragment() {
    companion object {
        private const val DEFAULT_SIZE = 0
        private const val SHIFT_SIZE = 3
    }

    private val model: SettingsModel by lazy {
        ViewModelProvider(this).get(SettingsModel::class.java)
    }

    private var binding: FragmentSettingsBinding? = null
    private lateinit var adapter: OpponentsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentSettingsBinding.inflate(inflater, container, false).also {
        binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initModelView()

        if (savedInstanceState == null) {
            binding?.run {
                btnSingleLaunch.isChecked = true
                containerRemoteConnect.root.visibility = View.GONE
            }
        } else
            setTab(model.tab)

        initSections()
        initSingleLaunch()
        initRemoteConnect()
    }

    private fun initModelView() {
        model.state.observe(requireActivity()) {
            parseState(it)
        }
        model.init(
            settings = SettingsImpl(requireContext()),
            strings = SettingsStrings(
                fieldSizeFormat = getString(R.string.field_size),
                gameLevelFormat = getString(R.string.game_level)
            )
        )
    }

    private fun parseState(state: SettingsState) {
        when (state) {
            is SettingsState.Settings -> loadSettings(state)
            SettingsState.AvailableNick -> showNick(true)
            SettingsState.UnavailableNick -> showNick(false)
            is SettingsState.Opponents -> showOpponents(state.opponents)
            is SettingsState.Error -> showError(state.error)
        }
    }

    private fun showOpponents(games: List<Gamer>) {
        binding?.containerRemoteConnect?.run {
            pbLoad.visibility = View.GONE
            vBlock.visibility = View.GONE
        }
        adapter.setItems(games)
    }

    private fun loadSettings(state: SettingsState.Settings) = binding?.run {
        with(containerSingleLaunch) {
            if (state.gamer.isFirst)
                btnFirst.isChecked = true
            else
                btnSecond.isChecked = true
            sbFieldsize.progress = state.gamer.gameFieldSize - SHIFT_SIZE
            sbLevel.progress = state.gamer.levelGamer.ordinal
            etNick.setText(state.gamer.nikGamer)
            etNick.setSelection(state.gamer.nikGamer.length)
            sbLevel.progress = state.gamer.levelGamer.ordinal
            if (state.gamer.isOnLine)
                btnHuman.isChecked = true
            else
                btnAi.isChecked = true

        }
        model.checkNick(state.gamer.nikGamer)
    }

    private fun showNick(isAvailable: Boolean) = binding?.run {
        containerSingleLaunch.btnStart.isEnabled = isAvailable
        if (isAvailable) {
            containerSingleLaunch.tilNick.error = null
            if (containerRemoteConnect.pbLoad.visibility == View.GONE)
                containerRemoteConnect.vBlock.visibility = View.GONE
        } else {
            containerSingleLaunch.tilNick.error = getString(R.string.unavailable_nick)
            containerRemoteConnect.vBlock.visibility = View.VISIBLE
        }
    }

    private fun showError(error: Throwable) = binding?.run {
        Snackbar.make(
            root,
            getString(R.string.error) + error.message,
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun initSections() = binding?.run {
        btnSingleLaunch.setOnClickListener {
            setTab(SettingsModel.Tab.SINGLE)
        }
        btnRemoteConnect.setOnClickListener {
            setTab(SettingsModel.Tab.REMOTE_CONNECT)
        }
    }

    private fun setTab(tab: SettingsModel.Tab) = binding?.run {
        model.tab = tab
        when (tab) {
            SettingsModel.Tab.SINGLE -> {
                containerSingleLaunch.root.visibility = View.VISIBLE
                containerRemoteConnect.root.visibility = View.GONE
            }
            SettingsModel.Tab.REMOTE_CONNECT -> {
                containerSingleLaunch.root.visibility = View.GONE
                containerRemoteConnect.root.visibility = View.VISIBLE
                loadingGames()
            }
        }
    }

    private fun initRemoteConnect() = binding?.run {

        adapter = OpponentsAdapter(
            strings = GameStrings(
                fieldSizeFormat = getString(R.string.field_size),
                waitCrossPlayer = getString(R.string.wait_cross),
                waitZeroPlayer = getString(R.string.wait_zero)
            )
        ) { opponent ->
            model.launchGame(
                opponent
            )
            requireActivity().onBackPressed()
        }
        containerRemoteConnect.rvGames.adapter = adapter
        adapter.notifyDataSetChanged()
        containerRemoteConnect.btnRefresh.setOnClickListener {
            loadingGames()
        }
    }

    private fun loadingGames() {
        binding?.containerRemoteConnect?.run {
            vBlock.visibility = View.VISIBLE
            pbLoad.visibility = View.VISIBLE
        }
        model.loadOpponents()
    }

    private fun initSingleLaunch() = binding?.containerSingleLaunch?.run {
        initFieldSize(sbFieldsize, tvFieldsize)
        initLevel(sbLevel, tvLevel)
        initNickInput(tilNick, etNick)

        btnFirst.setOnClickListener {
            model.setFirst(true)
        }
        btnSecond.setOnClickListener {
            model.setFirst(false)
        }
        btnAi.setOnClickListener {
            model.setOnline(false)
        }
        btnHuman.setOnClickListener {
            model.setOnline(true)
        }
        binding?.run {
            btnStart.setOnClickListener {
                model.launchGame()
                if (btnAi.isChecked) requireActivity().onBackPressed()
                else {
                    btnRemoteConnect.isChecked = true
                    setTab(SettingsModel.Tab.REMOTE_CONNECT)
                }
            }
        }

    }

    private fun initNickInput(til: TextInputLayout, et: EditText) {
        et.doOnTextChanged { text, _, _, _ ->
            til.error = null
            model.checkNick(text.toString())
        }
        et.setOnKeyListener(View.OnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                hideKeyboard()
                return@OnKeyListener true
            }
            false
        })
    }

    private fun hideKeyboard() {
        try {
            val act = requireActivity()
            val inputMethodManager: InputMethodManager =
                act.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                act.currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS
            )
        } catch (e: Exception) {
        }
    }

    private fun initLevel(sb: SeekBar, tv: TextView) {
        sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, v: Int, b: Boolean) {
                tv.text = model.getGameLevelString(v)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        sb.progress = DEFAULT_SIZE
    }

    private fun initFieldSize(sb: SeekBar, tv: TextView) {
        sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, v: Int, b: Boolean) {
                tv.text = model.getFieldSizeString(v + SHIFT_SIZE)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        sb.progress = DEFAULT_SIZE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
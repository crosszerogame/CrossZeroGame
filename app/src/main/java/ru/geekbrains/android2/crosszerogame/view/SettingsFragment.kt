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
import kotlinx.android.synthetic.main.layout_remote_connector.*
import ru.geekbrains.android2.crosszerogame.R
import ru.geekbrains.android2.crosszerogame.data.Gamer
import ru.geekbrains.android2.crosszerogame.databinding.FragmentSettingsBinding
import ru.geekbrains.android2.crosszerogame.utils.SettingsImpl
import ru.geekbrains.android2.crosszerogame.utils.strings.GameStrings
import ru.geekbrains.android2.crosszerogame.utils.strings.SettingsStrings
import ru.geekbrains.android2.crosszerogame.view.list.OpponentsAdapter
import ru.geekbrains.android2.crosszerogame.viewmodel.SettingsModel
import ru.geekbrains.android2.crosszerogame.viewmodel.SettingsState

class SettingsFragment : Fragment() {
    companion object {
        private const val DEFAULT_SIZE = 0
        private const val DEFAULT_LEVEL = 2
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
                containerRemoteLaunch.root.visibility = View.GONE
                containerRemoteConnect.root.visibility = View.GONE
            }
        } else
            setTab(model.tab)

        initSections()
        initSingleLaunch()
        initRemoteLaunch()
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
//            if (tilNick.error == null)
            vBlock.visibility = View.GONE
        }
        adapter.setItems(games)
    }

    private fun loadSettings(state: SettingsState.Settings) = binding?.run {
        with(containerSingleLaunch) {
            if (state.beginAsFirst)
                btnFirst.isChecked = true
            else
                btnSecond.isChecked = true
            sbFieldsize.progress = state.fieldSize
        }
        with(containerRemoteLaunch) {
            etNick.setText(state.nick)
            etNick.setSelection(state.nick.length)
            sbFieldsize.progress = state.fieldSize
            sbLevel.progress = state.gameLevel
        }
        with(containerRemoteConnect) {
            if (state.beginAsFirst)
                btnFirst.isChecked = true
            else
                btnSecond.isChecked = true
        }
        model.checkNick(state.nick)
    }

    private fun showNick(isAvailable: Boolean) = binding?.run {

        containerRemoteLaunch.btnCreate.isEnabled = isAvailable
        if (isAvailable) {
            containerRemoteLaunch.tilNick.error = null
            if (containerRemoteConnect.pbLoad.visibility == View.GONE)
                containerRemoteConnect.vBlock.visibility = View.GONE
        } else {
            containerRemoteLaunch.tilNick.error = getString(R.string.unavailable_nick)
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
        btnRemoteLaunch.setOnClickListener {
            setTab(SettingsModel.Tab.REMOTE_CREATE)
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
                containerRemoteLaunch.root.visibility = View.GONE
                containerRemoteConnect.root.visibility = View.GONE
            }
            SettingsModel.Tab.REMOTE_CREATE -> {
                containerSingleLaunch.root.visibility = View.GONE
                containerRemoteLaunch.root.visibility = View.VISIBLE
                containerRemoteConnect.root.visibility = View.GONE
            }
            SettingsModel.Tab.REMOTE_CONNECT -> {
                containerSingleLaunch.root.visibility = View.GONE
                containerRemoteLaunch.root.visibility = View.GONE
                containerRemoteConnect.root.visibility = View.VISIBLE
                loadingGames()
            }
        }
    }

    private fun initRemoteConnect() = binding?.containerRemoteConnect?.run {
        btnFirst.isChecked = true
        btnFirst.setOnClickListener {
            model.setFirst(true)
        }
        btnSecond.setOnClickListener {
            model.setFirst(false)
        }
        adapter = OpponentsAdapter(
            strings = GameStrings(
                fieldSizeFormat = getString(R.string.field_size),
                waitCrossPlayer = getString(R.string.wait_cross),
                waitZeroPlayer = getString(R.string.wait_zero)
            )
        ) { opponent ->
            model.launchGame(
                keyOpponent = opponent.keyGamer,
                beginAsFirst = btnFirst.isChecked,
                nikOpponent = opponent.nikGamer,
                levelOpponent = opponent.levelGamer
            )
            requireActivity().onBackPressed()
        }
        rvGames.adapter = adapter
        adapter.notifyDataSetChanged()
        btnRefresh.setOnClickListener {
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

    private fun initRemoteLaunch() = binding?.containerRemoteLaunch?.run {
        initFieldSize(sbFieldsize, tvFieldsize)
        initLevel()
        initNickInput(tilNick, etNick)
        btnCreate.setOnClickListener {
            model.launchGame(
                fieldSize = sbFieldsize.progress,
                nick = etNick.text.toString(),
                level = sbLevel.progress
            )
            //    requireActivity().onBackPressed()
            setTab(SettingsModel.Tab.REMOTE_CONNECT)
        }
    }

    private fun initSingleLaunch() = binding?.containerSingleLaunch?.run {
        btnFirst.isChecked = true
        initFieldSize(sbFieldsize, tvFieldsize)
        btnFirst.setOnClickListener {
            model.setFirst(true)
        }
        btnSecond.setOnClickListener {
            model.setFirst(false)
        }
        btnStart.setOnClickListener {
            model.launchGame(
                fieldSize = sbFieldsize.progress,
                beginAsFirst = btnFirst.isChecked
            )
            requireActivity().onBackPressed()
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

    private fun initLevel() = binding?.containerRemoteLaunch?.run {
        sbLevel.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, v: Int, b: Boolean) {
                tvLevel.text = model.getGameLevelString(v)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        sbLevel.progress = DEFAULT_LEVEL
    }

    private fun initFieldSize(sb: SeekBar, tv: TextView) {
        sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, v: Int, b: Boolean) {
                tv.text = model.getFieldSizeString(v)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        sb.progress = DEFAULT_SIZE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        model.save()
        binding = null
    }
}
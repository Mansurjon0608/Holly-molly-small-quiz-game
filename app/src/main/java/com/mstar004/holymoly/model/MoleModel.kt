package com.pstorli.wackymole.model

import android.app.Application
import android.content.Context
import android.graphics.Point
import com.pstorli.wackymole.util.MoleType.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mstar004.holymoly.rnd
import com.pstorli.wackymole.util.Consts.GAME_SPEED
import com.pstorli.wackymole.util.Consts.NEGATIVE
import com.pstorli.wackymole.util.Consts.SQUARE_SIZE
import com.pstorli.wackymole.util.Consts.ZERO
import com.pstorli.wackymole.util.MoleMachine
import com.pstorli.wackymole.util.MolePrefs
import com.pstorli.wackymole.util.MoleType
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * This class is where all our data is kept, classic MVVM.
 * What is not so classic is that this app has no repo's to get data from.
 *
 * Also unique is the MoleMachine, which runs in the background and is
 * responsible for messing with the moles (the board.)
 */
class MoleModel (application: Application)  : AndroidViewModel (application) {

    // *********************************************************************************************
    // Vars
    // *********************************************************************************************

    // A list of clicks to process.
    private val clicks: MutableList<Int> = mutableListOf()

    // How many cols do we have?
    var                     cols            = ZERO

    // What level are we on?
    var                     level           = ZERO

    // What speed is game at?
    var                     gameSpeed       = GAME_SPEED

    // The mole machine moles the moles around in a confusing way, or at least in the most confusing way possible.
    private var             moleMachine     = MoleMachine (this)

    // Are we running? Or just sitting there.
    // We need to be able to update the running state from different scopes.
    // Sometimes I sits and thinks and sometimes I just sit.
    var moleMachineRunning: AtomicBoolean   = AtomicBoolean(false)

    // This is the list of moles (images) we are displaying on the board,
    // on screen will be displayed as row/col. Instead of using the drawable ids,
    // we are using the enumerated type MoleType
    lateinit var            moles:          Array<MoleType?>

    // Secure / Encrypted shared prefs.
    private val             prefs           = MolePrefs (application)

    // How many rows do we have?
    var                     rows            = ZERO

    // What is the screen size?
    private lateinit var    screenSize:     Point

    // What's the score?
    var                     score           = ZERO

    // What is the square size?
    var                     squareSize      = SQUARE_SIZE

    // How much time do we really have?
    var                     time            = AtomicInteger (ZERO)

    // Boink this to notify activity to play the bomb sound.
    val                     playBombSound   = MutableLiveData<Boolean> ()

    // Boink this to notify activity to update the board.
    val                     updateBoard     = MutableLiveData<AtomicBoolean> ()

    /**
     * Do some initialization when model is created.
     */
    init {
        restore ()
    }

    // *********************************************************************************************
    // Functions
    // *********************************************************************************************

    /**
     * Change this square to this mole type
     */
    fun change (pos: Int, what: MoleType): Boolean {
        // This is this positions new state.
        moles [pos] = what

        // If we are changing into a bomb, playBombSound
        if (BOMB == what) {
            // Tell activity to play the bomb sound.
            playBombSound.postValue(true)
        }
        return true
    }

    /**
     * Save the level and score.
     */
    fun reset () {
        // Reset saved level, score and game speed.
        level     = ZERO
        score     = ZERO
        gameSpeed = GAME_SPEED

        // Reset the mole array.
        resetMoles ()

        // Save the score and level shared prefs.
        save ()
    }
    /**
     * Reset the mole array.
     */
    fun resetMoles () {
        // Create the mole array. If null, assume MoleType.GRASS
        moles = Array(rows * cols) { GRASS }

        refreshBoard ()
    }

    /**
     * Update the board by notifying the lve data.
     */
    fun refreshBoard () {
        updateBoard.postValue(AtomicBoolean(true))
    }

    /**
     * Restore the level and score from prefs.
     */
    fun restore () {
        // Restore saved level and score.
        level = prefs.getlevel ()
        score = prefs.getScore ()
    }

    /**
     * Save the level and score.
     */
    fun save () {
        // Restore saved level and score.
        prefs.saveLevel (level)
        prefs.saveScore (score)
    }

    /**
     * Something was clicked. Keep the clicks as a list,
     * as user is allowed to go click crazy.
     * Pass in -1 to clear list, or any value outside size of mole array.
     */
    fun clicked (pos: Int) {
        synchronized(this) {
            if (pos>=0 && pos<moles.size) {
                clicks.add(pos)
            }
            else {
                clicks.clear()
            }
        }
    }

    /**
     * Get your clicks on route 66!
     */
    fun getClicks ():MutableList<Int>  {
        // Copy click list.
        val copiedClicks = clicks.toMutableList()

        // Now clear click list.
        clicked (NEGATIVE)

        // Now return click list copy.
        return copiedClicks
    }

    /**
     * Start the mole machine.
     */
    fun start() {
        viewModelScope.launch {
            // Lauch this bad boy, er mole, on the Default background thread.
            moleMachine.start()
        }
    }

    /**
     * Did we just whack-a-mole?
     * (IE Does this position contain a mole?)
     */
    fun whackedMole (pos: Int): Boolean {
        return MOLE1 == moles[pos] || MOLE2 == moles[pos] || MOLE3 == moles[pos]
    }

    /**
     * Compute the rows / cols
     */
    fun setBoardSize (width: Int, height: Int, margin: Int) {
        // Get the screen size.
        screenSize = Point (width,height)

        // Compute rows / cols
        rows = screenSize.y / (squareSize+margin)
        cols = screenSize.x / (squareSize+margin)

        // Resetthe mole array.
        resetMoles ()
    }

    /**
     * Convert the position into a row / col value.
     */
    fun getRowCol (pos: Int): Point {
        val row = pos / cols
        val col = pos % cols

        return Point (row,col)
    }

    /**
     * Convert row/col into a position.
     */
    fun getPosFromRowCol (row: Int, col: Int): Int {
        return row * rows + col
    }

    /**
     * Return a random board position.
     */
    fun rndPos (): Int {
        return moles.size.rnd()
    }
    /**
     * Get Context?
     */
    fun context (): Context {
        return getApplication<Application>().applicationContext
    }

    /**
     * Going down, all hands abandon ship!
     * Do any cleanup before we all are gone.
     */
    override fun onCleared() {
        super.onCleared()

        // Stop the machine.
        moleMachineRunning.set (false)
    }
}
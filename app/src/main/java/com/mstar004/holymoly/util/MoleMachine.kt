package com.pstorli.wackymole.util

import android.media.MediaPlayer
import com.mstar004.holymoly.doit
import com.pstorli.wackymole.model.MoleModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

import com.pstorli.wackymole.util.MoleType.BOMB
import com.pstorli.wackymole.util.MoleType.GRASS
import com.pstorli.wackymole.util.MoleType.HOLE
import com.pstorli.wackymole.util.MoleType.MOLE1
import com.pstorli.wackymole.util.MoleType.MOLE2
import com.pstorli.wackymole.util.MoleType.MOLE3
import com.pstorli.wackymole.util.Consts.ZERO

/**
 * This class handles popping up, down and sideways the moles.
 * (This class should also be executed on a background coroutine,
 * such as from the view model scope.)
 */
class MoleMachine (var moleModel: MoleModel) {

    // *********************************************************************************************
    // Vars
    // *********************************************************************************************
    private lateinit var bombSound: MediaPlayer

    // Mole Percent Probabilities. If random num less than value below, doit.
    val GRASS_TO_HOLE   = 55     // The probability from 0 to 100 of grass changing to a hole.
    val HOLE_TO_GRASS   = 45     // The probability from 0 to 100 of hole changing back to grass.
    val HOLE_TO_MOLE1   = 45     // The probability from 0 to 100 of hole changing to a mole1.
    val HOLE_TO_MOLE2   = 35     // The probability from 0 to 100 of hole changing to a mole2.
    val HOLE_TO_MOLE3   = 25     // The probability from 0 to 100 of hole changing to a mole3.
    val MOLE_TO_HOLE    = 75     // The probability from 0 to 100 of mole changing back to a hole.

    // Scores for clicking on various items in game.
    val BOMB_SCORE      = -66
    val GRASS_SCORE     = -25
    val HOLE_SCORE      = -10
    val MOLE1_SCORE     = 25
    val MOLE2_SCORE     = 50
    val MOLE3_SCORE     = 100

    /**
     * We want bombs to fizzle out quickly,
     * not onesy twosy like messWithMoles
     */
    fun bombCheck (): Boolean {
        var foundBomb = false

        // Loop through all the squares looking for bombs.
        for (pos in 0..moleModel.rndPos()) {
            // Got Bomb?
            if (BOMB == moleModel.moles [pos]) {
                handleHoleWithBomb (pos)

                foundBomb = true
            }
        }

        return foundBomb
    }

    /**
     * This square has a bomb in it.
     */
    fun handleHoleWithBomb (pos: Int): Boolean {
        // Go back to being a hole.
        return moleModel.change (pos, HOLE)
    }

    /**
     * This square has a mole in it.
     */
    fun handleHoleWithMole (pos: Int): Boolean {
        var changed = false

        // Should we change the hole with a mole back to just a hole?
        if (MOLE_TO_HOLE.doit()) {
            // Go back to being a hole.
            changed = moleModel.change(pos, HOLE)
        }

        return changed
    }

    /**
     * This square has only grass.
     */
    fun handleHoleWithGrass (pos: Int): Boolean {
        var changed = false

        // Should we change the grass to a hole?
        if (GRASS_TO_HOLE.doit()) {
            // Go back to being a hole.
            changed = moleModel.change(pos, HOLE)
        }

        return changed
    }

    /**
     * This square has a hole in it.
     */
    fun handleHoleWithHole (pos: Int): Boolean {
        var changed = false

        // Should we change back to grass?
        if (HOLE_TO_GRASS.doit()) {
            // Go back to being grass.
            changed = moleModel.change(pos, GRASS)
        }

        // What type of mole?

        // Mole1
        else if (HOLE_TO_MOLE1.doit()) {
            // Create mole1
            changed = moleModel.change(pos, MOLE1)
        }

        // Mole2
        else if (HOLE_TO_MOLE2.doit()) {
            // Create mole2
            changed = moleModel.change(pos, MOLE2)
        }

        // Mole3
        else if (HOLE_TO_MOLE3.doit()) {
            // Create mole3
            changed = moleModel.change(pos, MOLE3)
        }

        return changed
    }

    /**
     * Run on the Default thread which is used for CPU intensive tasks.
     */
    suspend fun start () = withContext (Dispatchers.Default) {

        // Started?
        if (!moleModel.moleMachineRunning.get()) {
            // Keep running until we stop.
            moleModel.moleMachineRunning.set(true)

            // Don't stop running, until it gets set to false.
            while (moleModel.moleMachineRunning.get()) {

                // If the time is zero, we are paused.
                if (moleModel.time.get()>ZERO) {

                    // Check for bombs.
                    var update = bombCheck()

                    // Check for clicks.
                    if (checkForClicks()) {
                        update = true
                    }

                    // Now do something.
                    if (messWithMoles()) {
                        update = true
                    }

                    // Update the board
                    if (update) {
                        moleModel.refreshBoard ()
                    }
                }

                // Take a small break
                delay(moleModel.gameSpeed)
            }
        }
    }

    /**
     * Let's mess with the moles.
     */
    fun messWithMoles (): Boolean {
        // Did we modify the model.moles
        var messedWith = false

        // First pick a square to play with.
        val pos = moleModel.rndPos ()

        // What is there now?
        val what = moleModel.moles [pos]

        // Based on what is there, decide what to do.
        when (what) {
            BOMB    -> {
                if (handleHoleWithBomb (pos)) {
                    messedWith = true
                }
            }

            HOLE    -> {
                if (handleHoleWithHole (pos)) {
                    messedWith = true
                }
            }

            MOLE1   -> {
                if (handleHoleWithMole (pos)) {
                    messedWith = true
                }
            }

            MOLE2   -> {
                if (handleHoleWithMole (pos)) {
                    messedWith = true
                }
            }

            MOLE3   -> {
                if (handleHoleWithMole (pos)) {
                    messedWith = true
                }
            }

            GRASS   -> {
                if (handleHoleWithGrass(pos)) {
                    messedWith = true
                }
            }
            else -> {
                messedWith = false
            }
        }

        return messedWith
    }

    /**
     * Check to see if the user clicked something.
     */
    fun checkForClicks (): Boolean {

        // Get any user clicks.
        val clicks = moleModel.getClicks()

        // Return true if we modify something.
        var changed = false

        /**
         * Go thru click list
         */
        for (pos in clicks) {
            // What is there now?
            val what = moleModel.moles [pos]

            // Scores for clicking on various items in game.
            val BOMB_SCORE      = 100
            val GRASS_SCORE     = -25
            val HOLE_SCORE      = -10
            val MOLE1_SCORE     = 25
            val MOLE2_SCORE     = 50
            val MOLE3_SCORE     = 100

            // Based on what is there, decide what to do.
            when (what) {

                GRASS   -> {
                    // Update the score.
                    moleModel.score += GRASS_SCORE
                }

                HOLE    -> {
                    // Update the score.
                    moleModel.score += HOLE_SCORE
                }

                MOLE1   -> {
                    // Update the score.
                    moleModel.score += MOLE1_SCORE

                    // Turn mole into bomb!
                    changed = moleModel.change(pos, BOMB)
                }

                MOLE2   -> {
                    // Update the score.
                    moleModel.score += MOLE2_SCORE

                    // Turn mole into bomb!
                    changed = moleModel.change(pos, BOMB)
                }

                MOLE3   -> {
                    // Update the score.
                    moleModel.score += MOLE3_SCORE

                    // Turn mole into bomb!
                    changed = moleModel.change(pos, BOMB)
                }

                BOMB    -> {
                    // Update the score.
                    moleModel.score += BOMB_SCORE

                    // Go back to being a hole.
                    changed = moleModel.change(pos, HOLE)
                }
                else -> {
                    changed = false
                }
            }
        }

        return changed
    }
}
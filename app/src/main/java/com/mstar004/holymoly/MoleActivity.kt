package com.mstar004.holymoly

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.AdapterView
import android.widget.GridView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.mstar004.holymoly.util.Back.isHome
import com.pstorli.wackymole.model.MoleModel
import com.pstorli.wackymole.util.MoleType
import com.mstar004.holymoly.view.MoleAdapter
import com.pstorli.wackymole.util.Consts
import com.pstorli.wackymole.util.Consts.GAME_SPEED_DEC
import com.pstorli.wackymole.util.Consts.GAME_SPEED_FASTEST
import com.pstorli.wackymole.util.Consts.LEVEL_TIME
import com.pstorli.wackymole.util.Consts.ZERO

class MoleActivity : AppCompatActivity() {

    private lateinit var board:             GridView

    // The level
    private lateinit var level:             TextView

    // The mole view model
    private lateinit var moleModel:         MoleModel

    // Menu Item toggles between play and pause
    private lateinit var playPauseMenuItem: MenuItem

    // The score
    private lateinit var score:             TextView

    // The time
    private lateinit var time:              TextView
    private var timer:                      CountDownTimer? = null

    // The toolbar
    private lateinit var toolbar:           Toolbar

    private lateinit var bombSound:        MediaPlayer
    private lateinit var hitSound:         MediaPlayer


    fun getViewModel ():MoleModel  {
        return ViewModelProvider(this).get(MoleModel::class.java)
    }

    /**
     * Handle configuration changes.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            "ORIENTATION LANDSCAPE".logInfo()
        } else {
            "ORIENTATION PORTRAIT".logInfo()
        }
    }

    /**
     * This is where we start!
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the layout for the activity.
        setContentView(R.layout.mole_main)

        // Set up toolbar and toolbar menu.
        setUpToolbar ()
        isHome = true




        // We want to know the size of the visible area, so use tree observer to do things before drawn to screen.
        val constraintLayout = findViewById<View>(R.id.main_mole) as ConstraintLayout
        constraintLayout.viewTreeObserver.addOnGlobalLayoutListener(object :
            OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // Remove tree observer so we don't get called again.
                constraintLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)

                // Get the avail area.
                val width  = constraintLayout.width
                val height = constraintLayout.height

                // Now finish the onCreate
                delayedCreate (width,height)
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (isHome){
            finishAffinity()
        }
    }

    /**
     * Load GUI things from layout.
     */
    fun findGUIStuff () {

        // Get the board.
        board = findViewById (R.id.board)

        // What level are we at?
        level = findViewById (R.id.level)

        // Have we scored?
        score = findViewById (R.id.score)

        // Does anybody really know what time it is?
        time  = findViewById (R.id.time)
    }

    /**
     * Do this after we get the avail area.
     */
    fun delayedCreate (width: Int, height: Int) {
        // Create media player for sound effects.
        bombSound = MediaPlayer.create(this, R.raw.boom)
        hitSound  = MediaPlayer.create(this, R.raw.smack)

        // Load up some items from the layout.
        findGUIStuff ()

        // Get / Create the view model.
        moleModel = getViewModel ()

        // Set the square size. Height and width are the same. Use grass size as size for all.
        moleModel.squareSize = BitmapFactory.decodeResource (this.resources, R.drawable.grass).width

        // Adjust for the margin
        val margin: Int = this.resources.getDimension(R.dimen.mole_margin).toInt()+this.resources.getDimension(R.dimen.margin_adj).toInt()

        // Reduce size of grid to allow toolbar and score lines to be shown.
        val heightAdj = this.resources.getDimension(R.dimen.height_adj).toInt()

        // Set the screen size.
        moleModel.setBoardSize (width, height-heightAdj, margin)

        // Set the number of columns
        board.numColumns = moleModel.cols

        // Set the adapter for the board (grid view).
        board.adapter = MoleAdapter (moleModel)

        // Listen for them to click the board.
        board.onItemClickListener = AdapterView.OnItemClickListener { parent, v, position, id ->
            // Notyify model's mole nachine that something was clicked.
            moleModel.clicked (position)

            // Play hit sound if we wacked a mole.
            if (moleModel.whackedMole(position)) {
                // Yes, we whacked that bad boy. mole.
                hitSound.start()
            }
        }

        // Restore prev level, score, time and board.
        moleModel.restore ()

        // What level are we at?
        updateLevelText()

        // What's the score?
        updateScoreText()

        // Anyone got the time?
        updateTimeText()

        // Set up listener to play bomb sound.
        moleModel.playBombSound.observe(this) {
            // PLay the bomb sound.
            bombSound.start()
        }

        // Set up observer to update the board (moles)
        // from the live data MoleModel.update
        moleModel.updateBoard.observe(this) {
            // Reload the board from the view model.
            (board.adapter as MoleAdapter).notifyDataSetChanged()

            // What level are we at?
            updateLevelText()

            // What's the score?
            updateScoreText()

            // Anyone got the time?
            updateTimeText()
        }

        // Let them know how the game is played.
        toast (R.string.pressPlay)
    }

    // *********************************************************************************************
    // Toolbar
    // *********************************************************************************************

    /**
     * Create the toolbar
     */
    fun setUpToolbar () {
        // Add the toolbar.
        toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        toolbar.showOverflowMenu()

        // Display application icon in the toolbar
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setLogo(R.drawable.molee)
        supportActionBar?.setDisplayUseLogoEnabled(true)
    }

    /**
     * The option menu has play/pause button, reset and help buttons.
     */
    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu (menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.mole_menu, menu)

        // We want the overflow menu to also display icons as well as text.
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }

        // Save the playPause menu item for later.
        playPauseMenuItem = menu.findItem(R.id.playPause)

        return super.onCreateOptionsMenu(menu)
    }

    /**
     * A menu item was selected.
     */
    //Start game
    override fun onOptionsItemSelected (item: MenuItem): Boolean {
        return when (item.getItemId()) {
            // play or pause
            R.id.playPause -> {
                playPausePressed()
                true
            }

            // reset game
            R.id.reset -> {
                resetPressed ()
                true
            }

            // help menu where are you! R.id.help
            else -> super.onContextItemSelected(item)
        }
    }

    /**
     * The play / pause menu was selected.
     */
    fun playPausePressed () {
        "play / pause menu pressed.".debug()

        // Start the mole machine?
        if (!moleModel.moleMachineRunning.get()) {
            // Fire up the engines!
            moleModel.start()
        }

        // If play, (running and not paused.)
        if (moleModel.time.get()>ZERO) {

            pause()
        }
        // If paused, play
        else {
            play()
        }

        // Anyone got the time?
        updateTimeText()
    }

    /**
     * Start the timer.
     */
    fun startTimer () {
        // Create the countdown timer?
        if (null == timer) {
            // Create the timer.
            timer = object : CountDownTimer(LEVEL_TIME * Consts.SECOND, Consts.SECOND) {
                /**
                 * Ticme is ticking.
                 */
                override fun onTick(millisUntilFinished: Long) {
                    // Get current time and subtract a sec off it.
                    var newTime:Int = moleModel.time.get()
                    newTime--

                    // Update time and time text.
                    moleModel.time.set(newTime)
                    updateTimeText()
                }

                /**
                 * Outta time!
                 */
                override fun onFinish() {
                    levelCompleted ()
                }
            }

            // Start the timer.
            timer?.start()
        }
    }

    /**
     * Level done.
     */
    @SuppressLint("StringFormatMatches")
    fun levelCompleted () {
        // Outta time on this level.
        moleModel.time.set(ZERO)
        updateTimeText()

        // Go to the next level
        moleModel.level++

        // Update the level text.
        updateLevelText()

        // Up the game speed.
        if (moleModel.gameSpeed>GAME_SPEED_FASTEST) {
            moleModel.gameSpeed -= GAME_SPEED_DEC
        }

        // Save the model
        moleModel.save ()

        // Pause game.
        pause()

        // Clear the board.
        moleModel.resetMoles ()

        // Let them know that the level has been completed and what the new game speed is.
        toast (getString (R.string.levelCompleted, moleModel.level, moleModel.gameSpeed))
    }

    /**
     * Start your motor!
      */
    fun play () {
        // Start the timer.
        startTimer ()

        // Change icon to pause.
        playPauseMenuItem.setTitle (getString(R.string.pause))
        playPauseMenuItem.icon = application.get (R.drawable.pause1)

        // Play ball!
        moleModel.time.set(LEVEL_TIME.toInt())

        // Create some initial holes.
        for (pos in 1..moleModel.rndPos ()) {
            // Put some grass somewhere so that the user
            // is not wondering if the game has really started.
            moleModel.change (moleModel.rndPos (), MoleType.HOLE)
        }

        // Refresh the board.
        moleModel.refreshBoard ()
    }

    /**
     * Stop the presses!
     */
    fun pause () {
        // Was / Is the timer running?
        if (null != timer) {
            // Cancel the timer.
            timer?.cancel()

            timer = null
        }

        // Pause Game. Change icon to play.
        playPauseMenuItem.setTitle (getString(R.string.play))
        playPauseMenuItem.icon = application.get (R.drawable.play1)

        // Stop the presses.
        moleModel.time.set (ZERO)
    }

    /**
     * Update the level text.
     */
    fun updateLevelText () {
        // Update the level.
        level.text = moleModel.level.toString()
    }

    /**
     * Update the level text.
     */
    fun updateScoreText () {
        // Update the level.
        score.text = moleModel.score.toString()
    }

    /**
     * Update the time text.
     */
    fun updateTimeText () {
        // Update the time.
        time.text = moleModel.time.get().format()
    }

    /**
     * The reset menu was selected.
     */
    fun resetPressed () {
        "reset menu pressed.".debug()

        // Pause everything.
        pause ()

        // Reset level and score.
        moleModel.reset()

        updateLevelText ()
        updateScoreText ()
    }

    /**
     * The help menu was selected.
     */
    fun helpPressed () {
        "help menu pressed.".debug()
    }

    /**
     * Going down, all hands abandon ship!
     * Do any cleanup before we all are gone.
     */
    override fun onDestroy() {
        super.onDestroy()

        // Save level and score
        moleModel.save ()
    }
}
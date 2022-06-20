package com.mstar004.holymoly

import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.mstar004.holymoly.util.Back.isHome


lateinit var handler: Handler
private lateinit var music: MediaPlayer

class SpalshActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        handler = Handler()
        music = MediaPlayer.create(this,R.raw.sp_music)
        music.start()
        isHome = false
        handler.postDelayed({
            startActivity(Intent(this,MoleActivity::class.java))
            music.stop()
        },3000)
    }
}
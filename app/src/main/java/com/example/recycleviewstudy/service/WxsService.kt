package com.example.recycleviewstudy.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.recycleviewstudy.R

class WxsService : Service() {
    private lateinit var mediaPlayer: MediaPlayer

    inner class MusicBinder : Binder() {
        fun play() {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
            } else {
                mediaPlayer.stop()
                mediaPlayer.prepare()
            }
        }
    }
    private val mBinder = MusicBinder()
    override fun onCreate() {
        super.onCreate()
        mediaPlayer=MediaPlayer.create(this,R.raw.wxs)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

}
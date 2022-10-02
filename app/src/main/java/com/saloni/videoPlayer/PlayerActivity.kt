@file:Suppress("DEPRECATION")

package com.saloni.videoPlayer

import android.app.DownloadManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.animation.AnimationUtils
import androidx.lifecycle.ViewModel
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.saloni.videoPlayer.databinding.ActivityPlayerBinding

@Suppress("DEPRECATION")
class PlayerActivity : AppCompatActivity() {

    private lateinit var  binding:ActivityPlayerBinding
    companion object{
        private lateinit var player:SimpleExoPlayer
        lateinit var playerList:ArrayList<Video>
        var position:Int=-1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeLayout()
        initializeBinding()
        binding.playerView.startAnimation(AnimationUtils.loadAnimation(binding.playerView.context,R.anim.move))

    }
    private fun initializeLayout(){
        when(intent.getStringExtra("class")){
            "AllVideos"->{
                playerList= ArrayList()
                playerList.addAll(MainActivity.videoList)
                createPlayer()
            }
            "FolderActivity"->{
                playerList= ArrayList()
                playerList.addAll(FoldersActivity.currentFolderVideos)
                createPlayer()
            }
        }

    }

    private fun initializeBinding(){
        binding.backBtn.setOnClickListener {
            finish()
        }
        binding.downloadBtn.setOnClickListener {
        }
    }
    private fun createPlayer(){
        binding.videoTitle.text= playerList[position].title
        binding.videoTitle.isSelected=true
        player=SimpleExoPlayer.Builder(this).build()
        binding.playerView.player=player
        val mediaItem=MediaItem.fromUri(playerList[position].artUri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()

    }

}

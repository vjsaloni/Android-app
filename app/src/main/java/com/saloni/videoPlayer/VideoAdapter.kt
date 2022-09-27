package com.saloni.videoPlayer

import android.content.Context
import android.content.Intent
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.saloni.videoPlayer.databinding.VideoViewBinding

class VideoAdapter(private val context: Context, private var videoList: ArrayList<Video>,private val isFolder:Boolean=false):RecyclerView.Adapter<VideoAdapter.MyHolder>() {
    class MyHolder(binding: VideoViewBinding):RecyclerView.ViewHolder(binding.root) {
        val title=binding.videoName
        val folder=binding.folderName
        val duration=binding.duration
        val image=binding.videoImg
        val root=binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(VideoViewBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.title.text=videoList[position].title
        holder.folder.text=videoList[position].folderName
        holder.duration.text=DateUtils.formatElapsedTime(videoList[position].duration/1000)
        Glide.with(context)
            .asBitmap()
            .load(videoList[position].artUri)
            .apply(RequestOptions().placeholder(R.mipmap.ic_video_player).centerCrop())
            .into(holder.image)

        holder.root.setOnClickListener {
            when{
                isFolder->{
                    sendIntent(pos=position,ref="FolderActivity")
                }
                else->{
                    sendIntent(pos=position,ref="AllVideos")
                }
            }
        }
        holder.root.startAnimation(AnimationUtils.loadAnimation(holder.root.context,R.anim.move))
    }

    override fun getItemCount(): Int {
        return videoList.size
    }
    private fun sendIntent(pos: Int, ref: String){
        PlayerActivity.position=pos
        val intent=Intent(context,PlayerActivity::class.java)
        intent.putExtra("class",ref)
        ContextCompat.startActivity(context,intent,null)
    }
}

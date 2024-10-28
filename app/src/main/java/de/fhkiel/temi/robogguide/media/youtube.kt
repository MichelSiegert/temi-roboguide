package de.fhkiel.temi.robogguide.media

import android.util.Log
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener

fun getID(url: String): String{
    val urlFrags  = url.split("/")
    return urlFrags[urlFrags.size - 1].split("?")[0];
}

class YoutubePlayerListener : YouTubePlayerListener {

    var isRunning: Boolean = false;
    override fun onApiChange(youTubePlayer: YouTubePlayer) {
    }

    override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
    }

    override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
        TODO("Not yet implemented")
    }

    override fun onPlaybackQualityChange(
        youTubePlayer: YouTubePlayer,
        playbackQuality: PlayerConstants.PlaybackQuality
    ) {
    }

    override fun onPlaybackRateChange(
        youTubePlayer: YouTubePlayer,
        playbackRate: PlayerConstants.PlaybackRate
    ) {
    }

    override fun onReady(youTubePlayer: YouTubePlayer) {
    }

    override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
        isRunning=  arrayOf("BUFFERING","PLAYING").contains(state.toString())
    }

    override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
    }

    override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
    }

    override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {
    }
}
package com.github.libretube.ui.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.libretube.R
import com.github.libretube.api.obj.StreamItem
import com.github.libretube.databinding.FragmentAudioPlayerBinding
import com.github.libretube.extensions.toID
import com.github.libretube.services.BackgroundMode
import com.github.libretube.ui.base.BaseFragment
import com.github.libretube.util.ImageHelper
import com.github.libretube.util.NavigationHelper
import com.github.libretube.util.PlayingQueue

class AudioPlayerFragment : BaseFragment() {
    private lateinit var binding: FragmentAudioPlayerBinding
    private val onTrackChangeListener: (StreamItem) -> Unit = {
        updateStreamInfo()
    }
    private var isPaused: Boolean = false

    private lateinit var playerService: BackgroundMode
    private var mBound: Boolean = false

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as BackgroundMode.LocalBinder
            playerService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Intent(activity, BackgroundMode::class.java).also { intent ->
            activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAudioPlayerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.prev.setOnClickListener {
            val currentIndex = PlayingQueue.currentIndex()
            PlayingQueue.onQueueItemSelected(currentIndex - 1)
        }

        binding.next.setOnClickListener {
            val currentIndex = PlayingQueue.currentIndex()
            PlayingQueue.onQueueItemSelected(currentIndex + 1)
        }

        PlayingQueue.addOnTrackChangedListener(onTrackChangeListener)

        binding.playPause.setOnClickListener {
            if (mBound == false) return@setOnClickListener
            if (isPaused) playerService.play() else playerService.pause()
            binding.playPause.setIconResource(if (isPaused) R.drawable.ic_pause else R.drawable.ic_play)
            isPaused = !isPaused
        }

        updateStreamInfo()
    }

    private fun updateStreamInfo() {
        val current = PlayingQueue.getCurrent()
        current ?: return

        binding.title.text = current.title
        binding.uploader.text = current.uploaderName
        binding.uploader.setOnClickListener {
            NavigationHelper.navigateChannel(requireContext(), current.uploaderUrl?.toID())
        }

        ImageHelper.loadImage(current.thumbnail, binding.thumbnail)
    }

    override fun onDestroy() {
        super.onDestroy()

        activity?.unbindService(connection)
        // unregister the listener
        PlayingQueue.removeOnTrackChangedListener(onTrackChangeListener)
    }
}

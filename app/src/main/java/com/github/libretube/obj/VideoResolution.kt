package com.github.libretube.obj

data class VideoResolution(
    val name: String,
    val resolution: Int? = null,
    val adaptiveSourceUrl: String? = null
)

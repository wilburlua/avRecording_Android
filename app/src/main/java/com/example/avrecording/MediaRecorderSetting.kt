package com.example.avrecording

/**
 * Created by: Wilbur Lua
 * 3/10/22
 */
data class MediaRecorderSetting(
    var audioSource: Int,
    var audioEncoder: Int,
    var audioOutputFormat: Int,
    var audioSamplingRate: Int,
    var audioEncodingBitRate: Int
)

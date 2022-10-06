package com.example.avrecording

object AudioConstants {

    //Audio Source constants
    const val AUDIO_SOURCE_UNPROCESSED = "Unprocessed"
    const val AUDIO_SOURCE_MIC = "Microphone"
    const val AUDIO_SOURCE_VP = "Voice Performance"
    const val AUDIO_SOURCE_VC = "Voice Communication"

    //Audio Encoder constants
    const val AUDIO_ENCODER_AAC = "AAC"
    const val AUDIO_ENCODER_AMR_NB = "AMR_NB"

    //Audio Output format constants
    const val AUDIO_FORMAT_THREEGPP = "3GPP (.3gpp)"
    const val AUDIO_FORMAT_MPEG_4 = "MPEG_4 (.m4a)"

    //Sampling Rate constant
    const val AUDIO_SAMPLING_LOW = "Low (8 kHz)"
    const val AUDIO_SAMPLING_MED = "Med (22.05 kHz)"
    const val AUDIO_SAMPLING_HIGH = "High (44.1 kHz)"

    //Encoding bit rate (16 or 24 bits)
    const val AUDIO_ENCODING_BITRATE_DEFAULT = "Default"
    const val AUDIO_ENCODING_BITRATE_16 = "16 bits"
    const val AUDIO_ENCODING_BITRATE_24 = "24 bits"

}
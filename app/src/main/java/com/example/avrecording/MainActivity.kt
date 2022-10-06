package com.example.avrecording

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import android.view.Menu
import android.view.MenuItem
import com.example.avrecording.databinding.ActivityMainBinding
import android.media.MediaRecorder
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import java.io.*

private const val LOG_TAG = "AudioRecordTest"
class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener{
    private lateinit var binding: ActivityMainBinding
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var mediaRecorderSetting: MediaRecorderSetting? = null
    private var permissionList: Array<String> = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private var mStartRecording : Boolean = true
    private var mStartPlaying: Boolean = true
    private var filePath: String? = ""
    private var fileName: String? = ""
    private val requestCode = 321

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            this.requestCode -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Log.i(LOG_TAG, "Permission granted")
                    binding.txtMicrophoneStatus.text = "Microphone Status: Allowed"
                    binding.txtRecordingStatus.text = "Ready to Record"

                } else {
                    binding.txtMicrophoneStatus.text = "Microphone Status: Denied"
                    binding.txtRecordingStatus.text = "Please enable permissions to use the application"
                    binding.btnRecord.isEnabled = false
                    binding.btnPlay.isEnabled = false
                    binding.btnShare.isEnabled = false
                    Log.i(LOG_TAG, "Permission not granted")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ActivityCompat.requestPermissions(this, permissionList, this.requestCode)
        filePath = getExternalFilesDir(null)?.absolutePath
        setUpSpinners()
        Log.i(LOG_TAG, filePath.toString())
        val recordOnClick = OnClickListener {
            onRecord(this.mStartRecording)
            binding.btnRecord.text = when(mStartRecording) {
                true -> "Tap to Stop"
                false -> "Record"
            }
            mStartRecording = !mStartRecording
            Log.i("avRecording", "record button pressed")
        }

        val playBackOnClick = OnClickListener {
            onPlaying(this.mStartPlaying)
            binding.btnPlay.text = when(mStartPlaying) {
                true -> "Stop"
                false -> "Play"
            }
            mStartPlaying = !mStartPlaying
        }
        val saveOnClick = OnClickListener {
            onSave()
        }
        binding.btnRecord.setOnClickListener(recordOnClick)
        binding.btnPlay.setOnClickListener(playBackOnClick)
        binding.btnShare.setOnClickListener(saveOnClick)
    }

    private fun saveFileDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Insert a file name")

        var input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("Save") { dialog, which ->
            this.fileName = input.text.toString()
            try {
                var format = ""
                if (filePath != null) {
                    format = filePath.toString().takeLastWhile {
                        it.isLetterOrDigit()
                    }
                    Log.i("onSave", format.toString())
                } else {
                    Log.w(LOG_TAG, "filePath is null")
                }
                println( filePath.toString() )
                println(this.fileName)
                val sourceFile = File(filePath.toString())
                val destFile : File = File((getExternalFilesDir(null)?.absolutePath + "/" + this.fileName + ".${format}" ))
                sourceFile.renameTo(destFile)

                println(destFile.absolutePath)
                println(filePath.toString())

                var uri : Uri
                val sendIntent: Intent = Intent().apply {
                    uri = FileProvider.getUriForFile(applicationContext, "com.example.avrecording.provider", destFile)
                    this.action = Intent.ACTION_SEND
                    this.putExtra(Intent.EXTRA_STREAM, uri)

                    this.type = "audio/*"
                    this.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                }
                val chooser = Intent.createChooser(sendIntent, "Share Audio")
                val queries = this.packageManager.queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)
                for (query in queries) {
                    var packageName = query.activityInfo.packageName
                    this.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(chooser)

            } catch (e: IOException) {
                Log.e(LOG_TAG, e.toString())
            }
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            Toast.makeText(applicationContext,
                "Requires file name to share", Toast.LENGTH_SHORT).show()
        }
        builder.show()
    }
        //            val fileData: File = File(filePath.toString())
//            val inputStream = FileInputStream(fileData)
//            val data: ByteArray = inputStream.readBytes()
//            val outputStream : FileOutputStream = openFileOutput("audioTest", Context.MODE_PRIVATE)
//            var total: Long = 0
//            var count = 0
//            while (inputStream.read(data) != -1) {
//                outputStream.write(data, 0, count)
//                count = inputStream.read(data)
//                total += count
//            }
//            outputStream.flush()
//            outputStream.close()

    private fun onSave() {
        saveFileDialog()
    }

    /**
     * onRecord takes in a Boolean value, if true -> startRecord, else -> stopRecord
     */
    private fun onRecord(start: Boolean) = if (start) {
        Log.i("avRecording", "avRecording -> Started to record")
        startRecording()
    } else {
        stopRecording()
    }
    private fun onPlaying(hasStartedPlaying: Boolean) = if (hasStartedPlaying) {
        startPlaying()
    } else {
        stopPlaying()
    }

    private fun getRecorderSettings() : MediaRecorderSetting{
        var audioSourceSelection: Int = 0
        var audioEncoder: Int = 0
        var audioOutput: Int = 0
        var audioSamplingRate: Int = 0
        var audioEncodingBitRate: Int = 0

        when (binding.spinnerAudioSource.selectedItem.toString()) {
            AudioConstants.AUDIO_SOURCE_UNPROCESSED-> audioSourceSelection = MediaRecorder.AudioSource.UNPROCESSED
            AudioConstants.AUDIO_SOURCE_MIC-> audioSourceSelection = MediaRecorder.AudioSource.MIC
            AudioConstants.AUDIO_SOURCE_VP -> audioSourceSelection = MediaRecorder.AudioSource.VOICE_PERFORMANCE
            AudioConstants.AUDIO_SOURCE_VC -> audioSourceSelection = MediaRecorder.AudioSource.VOICE_COMMUNICATION
        }

        when (binding.spinnerAudioEncoder.selectedItem.toString()) {
            AudioConstants.AUDIO_ENCODER_AAC-> audioEncoder = MediaRecorder.AudioEncoder.AAC
            AudioConstants.AUDIO_ENCODER_AMR_NB -> audioEncoder = MediaRecorder.AudioEncoder.AMR_NB
        }

        when (binding.spinnerAudioOutputFormat.selectedItem.toString()) {
            AudioConstants.AUDIO_FORMAT_THREEGPP -> {
                audioOutput = MediaRecorder.OutputFormat.THREE_GPP
                setRecordingFormat(AudioConstants.AUDIO_FORMAT_THREEGPP)
            }
            AudioConstants.AUDIO_FORMAT_MPEG_4 -> {
                audioOutput = MediaRecorder.OutputFormat.MPEG_4
                setRecordingFormat(AudioConstants.AUDIO_FORMAT_MPEG_4)
            }
        }
        when (binding.spinnerSamplingRate.selectedItem.toString()) {
            AudioConstants.AUDIO_SAMPLING_LOW -> audioSamplingRate = 8000
            AudioConstants.AUDIO_SAMPLING_MED -> audioSamplingRate = 22050
            AudioConstants.AUDIO_SAMPLING_HIGH -> audioSamplingRate = 44100
        }

        when (binding.spinnerAudioEncodingBitRate.selectedItem.toString()) {
            AudioConstants.AUDIO_ENCODING_BITRATE_DEFAULT -> audioEncodingBitRate = 1
            AudioConstants.AUDIO_ENCODING_BITRATE_16 -> audioEncodingBitRate = 16
            AudioConstants.AUDIO_ENCODING_BITRATE_24 -> audioEncodingBitRate = 24
        }
        mediaRecorderSetting = MediaRecorderSetting(audioSourceSelection,
            audioEncoder, audioOutput, audioSamplingRate, audioEncodingBitRate)

        return mediaRecorderSetting as MediaRecorderSetting
    }
    private fun setRecordingFormat(outputFormat: String) : String?{
        filePath = null
        filePath = getExternalFilesDir(null)?.absolutePath
        when (outputFormat) {
            AudioConstants.AUDIO_FORMAT_THREEGPP -> {
                filePath += "/audioTest.3gpp"
                return filePath
            }
            AudioConstants.AUDIO_FORMAT_MPEG_4 -> {
                filePath += "/audioTest.m4a"
                return filePath
            }
        }
        return ""
    }
    private fun startRecording() {
        Log.v("Tag", "Start recording...")
        if (getRecorderSettings() != null) {
            this.mediaRecorderSetting = getRecorderSettings()

            //Debug
            Log.i("Audio File path", filePath.toString())
            Log.i("Audio Source", this.mediaRecorderSetting!!.audioSource.toString())
            Log.i("Audio Output Format", this.mediaRecorderSetting!!.audioOutputFormat.toString())
            Log.i("Audio Encoder", this.mediaRecorderSetting!!.audioEncoder.toString())
            Log.i("Audio Sampling Rate", this.mediaRecorderSetting!!.audioSamplingRate.toString())
        } else {
            Log.v(LOG_TAG, "Recorder settings is null")
            return
        }
        Log.i(LOG_TAG, "Checking user for mic access")
        val hasMicrophone = this.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
        if (hasMicrophone) {
            Log.i(LOG_TAG, "Mic Access found")
            recorder = MediaRecorder()
            recorder?.apply {
                //Testing settings
//                this.setAudioSource(MediaRecorder.AudioSource.MIC)
//                this.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//                this.setOutputFile(filePath)
//                Log.i("Audio File path", filePath.toString())
//                this.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
//                this.setAudioEncodingBitRate(16 * 44100);
//                this.setAudioSamplingRate(44100)
//                this.setAudioChannels(1)

                //Real settings
                this.setAudioSource(mediaRecorderSetting!!.audioSource)
                this.setOutputFormat(mediaRecorderSetting!!.audioOutputFormat)
                this.setOutputFile(filePath)
                this.setAudioEncoder(mediaRecorderSetting!!.audioEncoder)
                this.setAudioEncodingBitRate((mediaRecorderSetting!!.audioEncodingBitRate * mediaRecorderSetting!!.audioSamplingRate))
                this.setAudioSamplingRate(mediaRecorderSetting!!.audioSamplingRate)
                this.setAudioChannels(1)
            }
            try {
                recorder?.prepare()
                recorder?.start()
                binding.txtRecordingStatus.text = "Recording started .."
            } catch(e: IOException) {
                Log.e(LOG_TAG, e.toString())
            }
        } else {
            Toast.makeText(this.applicationContext, "Device mic is unavailable/not found", Toast.LENGTH_SHORT)
        }
    }

    private fun stopRecording() {
        Log.i("avRecording", "avRecording -> Stop recording pressed")

        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        binding.txtRecordingStatus.text = "Ready to Record"
    }

    private fun startPlaying() {
        println(filePath)
        player = MediaPlayer().apply {
            this.setDataSource(filePath)
            try {
                this.prepare()
            } catch (e: java.lang.IllegalStateException) {
                Log.e(LOG_TAG, e.toString())
            } catch (e: IOException) {
                Log.e(LOG_TAG, e.toString())
            }
            this.start()
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    private fun setUpSpinners() {
        val audioSourceAdapter = ArrayAdapter.createFromResource(applicationContext, R.array.audioSource, android.R.layout.simple_spinner_item).also {
            adapter -> adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        val audioEncodingAdapter = ArrayAdapter.createFromResource(applicationContext, R.array.audioEncoder, android.R.layout.simple_spinner_item).also {
                adapter -> adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        val audioFormatAdapter = ArrayAdapter.createFromResource(applicationContext, R.array.audioOutputFormat, android.R.layout.simple_spinner_item).also {
            adapter -> adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        val samplingRateAdapter = ArrayAdapter.createFromResource(applicationContext, R.array.audioSamplingRate, android.R.layout.simple_spinner_item).also {
            adapter -> adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        val encodingBitRateAdapter = ArrayAdapter.createFromResource(applicationContext, R.array.audioEncodingBitRate, android.R.layout.simple_spinner_item).also {
            adapter -> adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerAudioSource.adapter = audioSourceAdapter
        binding.spinnerAudioEncoder.adapter = audioEncodingAdapter
        binding.spinnerAudioOutputFormat.adapter = audioFormatAdapter
        binding.spinnerSamplingRate.adapter = samplingRateAdapter
        binding.spinnerAudioEncodingBitRate.adapter = encodingBitRateAdapter

        binding.spinnerAudioSource.onItemSelectedListener = this
        binding.spinnerAudioEncoder.onItemSelectedListener = this
        binding.spinnerAudioOutputFormat.onItemSelectedListener = this
        binding.spinnerSamplingRate.onItemSelectedListener = this
        binding.spinnerAudioEncodingBitRate.onItemSelectedListener = this
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        var item = parent?.getItemAtPosition(position)
        Log.i(LOG_TAG, item.toString())
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }
}
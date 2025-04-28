package com.example.recycleviewstudy.activity.paramode

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.alibaba.dashscope.audio.asr.recognition.Recognition
import com.alibaba.dashscope.audio.asr.recognition.RecognitionParam
import com.alibaba.dashscope.exception.NoApiKeyException
import com.example.recycleviewstudy.R
import com.example.recycleviewstudy.activity.utils.ApiKeyUtil
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.nio.ByteBuffer

class LongTypeActivity : ComponentActivity() {
    private var resultText: TextView? = null
    private var exitButton: Button? = null
    private var startButton: Button? = null
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var isPermissionDeniedPermanently = false

    companion object {
        private const val SAMPLE_RATE = 16000
        private const val BUFFER_SIZE = 1024
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_long2)

        resultText = findViewById(R.id.result_text)
        exitButton = findViewById(R.id.exit_button)
        startButton = findViewById(R.id.start_button)

        // 请求录音权限
        checkAndRequestPermission()

        // 开始按钮点击监听
        startButton!!.setOnClickListener {
            if (checkPermissionGranted()) {
                startRecording()
            } else {
                showPermissionRationale()
            }
        }

        exitButton!!.setOnClickListener {
            stopRecording()
            finish()
        }
    }

    /**
     * 权限相关
     */

    // 检查权限状态
    private fun checkPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    // 新增权限检查方法
    private fun checkAndRequestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                enableButtons(true)
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.RECORD_AUDIO
            ) -> {
                showPermissionRationale()
            }

            else -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO), 1
                )
            }
        }
    }

    // 处理权限请求结果
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    enableButtons(true)
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.RECORD_AUDIO
                        )
                    ) {
                        isPermissionDeniedPermanently = true
                        showPermanentDenialDialog()
                    } else {
                        showPermissionRationale()
                    }
                    enableButtons(false)
                }
            }
        }
    }

    // 显示权限必要性说明弹窗
    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("需要麦克风权限")
            .setMessage("语音识别功能需要使用麦克风权限，请允许权限申请")
            .setPositiveButton("去设置") { _, _ ->
                checkAndRequestPermission()
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // 永久拒绝时的处理
    private fun showPermanentDenialDialog() {
        AlertDialog.Builder(this)
            .setTitle("权限被永久拒绝")
            .setMessage("您已永久拒绝麦克风权限，请到应用设置中手动开启")
            .setPositiveButton("去设置") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("退出") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    // 打开系统设置
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    // 启用/禁用按钮
    private fun enableButtons(enabled: Boolean) {
        startButton?.isEnabled = enabled
        exitButton?.isEnabled = enabled
    }

    // 新增停止录音方法
    private fun stopRecording() {
        isRecording = false
        audioRecord?.apply {
            stop()
            release()
        }
        audioRecord = null
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun startRecording() {
        if (!checkPermissionGranted()) {
            Log.i("md", "no permission")
        }
        // 初始化 AudioRecord
        val bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )


        Log.d("md", "startRecording")
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        isRecording = true
        runOnUiThread {
            resultText?.text = ""
            startButton?.isEnabled = false
            exitButton?.isEnabled = true
        }
        Thread { this.recordAndRecognize() }.start()
    }

    @SuppressLint("CheckResult")
    private fun recordAndRecognize() {
        audioRecord!!.startRecording()
        val buffer = ByteBuffer.allocateDirect(BUFFER_SIZE)

        try {
            Log.d("md", "recordAndRecognize")
            val recognizer: Recognition = Recognition()
            val param: RecognitionParam = buildRecognitionParam()

            val audioSource: Flowable<ByteBuffer> = Flowable.create({ emitter ->
                while (isRecording) {
                    val read = audioRecord!!.read(buffer, BUFFER_SIZE)
                    if (read > 0) {
                        emitter.onNext(buffer)
                        buffer.clear()
                    }
                }
                emitter.onComplete()
            }, BackpressureStrategy.BUFFER)

            Log.d("md", "streamCall")
            recognizer.streamCall(param, audioSource)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result ->
                    if (result.isSentenceEnd()) {
                        val text: String = result.getSentence().getText()
                        Log.d("mdRes", text)
                        runOnUiThread {
                            resultText?.append("$text\n")
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("Recognition", "Error: ", e)
        }
    }

    @Throws(NoApiKeyException::class)
    private fun buildRecognitionParam(): RecognitionParam {
        return RecognitionParam.builder()
            .model("paraformer-realtime-v1")
            .format("pcm")
            .sampleRate(SAMPLE_RATE)
            .apiKey(ApiKeyUtil.dashScopeApiKey)
//            .parameter("language_hints", arrayOf<String>("ja"))
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRecording = false
        if (audioRecord != null) {
            audioRecord!!.stop()
            audioRecord!!.release()
        }
    }

}
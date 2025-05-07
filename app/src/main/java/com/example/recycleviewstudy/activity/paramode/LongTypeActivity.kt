package com.example.recycleviewstudy.activity.paramode

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.service.voice.VoiceInteractionSession.ActivityId
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckedTextView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.alibaba.dashscope.audio.asr.recognition.Recognition
import com.alibaba.dashscope.audio.asr.recognition.RecognitionParam
import com.alibaba.dashscope.audio.asr.vocabulary.VocabularyService
import com.alibaba.dashscope.exception.NoApiKeyException
import com.example.recycleviewstudy.R
import com.example.recycleviewstudy.activity.S5StackActivity
import com.example.recycleviewstudy.activity.utils.ApiKeyUtil
import com.example.recycleviewstudy.item.Hotword
import com.example.recycleviewstudy.item.addStudy
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

open class LongTypeActivity : ComponentActivity() {
    private var resultText: TextView? = null
    private var exitButton: Button? = null
    private var startButton: Button? = null
    private var audioRecord: AudioRecord? = null
    private var vocButton: Button? = null
    private var vocCheckButton: Button? = null
    private var langButton: Button? = null
    private var isRecording = false
    private var isPermissionDeniedPermanently = false

    protected open var model: String = MODEL_V1
    protected open var language = arrayListOf<String>()
    protected var vocId = ""
    protected var wordList = arrayListOf<Hotword>()
    protected open var vocAble = false
    protected var activityId = "0"

    companion object {
        private const val SAMPLE_RATE = 16000
        private const val BUFFER_SIZE = 1024
        const val MODEL_V1 = "paraformer-realtime-v1"
        const val MODEL_V2 = "paraformer-realtime-v2"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_long2)

        resultText = findViewById(R.id.result_text)
        exitButton = findViewById(R.id.exit_button)
        startButton = findViewById(R.id.start_button)
        vocButton = findViewById(R.id.voc_button)
//        vocCheckButton = findViewById(R.id.voc_check_button)
        langButton = findViewById(R.id.lang_button)

        if (vocAble && model == MODEL_V2) {
            if (wordList.isEmpty()){
                wordList.add(Hotword("哈尔滨工业大学",1,"zh"))
            }
            updateHotword()
            vocButton?.visibility = View.VISIBLE
            vocButton?.setOnClickListener {
                insHotWord()
            }
//            vocCheckButton?.visibility = View.VISIBLE
//            vocCheckButton?.setOnClickListener {
//            }
            langButton?.visibility = View.VISIBLE
            langButton?.setOnClickListener {
                setLang()
            }
        } else {
            langButton?.visibility = View.GONE
            vocButton?.visibility = View.GONE
            vocCheckButton?.visibility = View.GONE
        }

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

    private fun setLang() {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle("选择语言")
        val view = LayoutInflater.from(this).inflate(R.layout.checkbox_lang, null)
        val yue = view.findViewById<CheckedTextView>(R.id.yue)
        val ko = view.findViewById<CheckedTextView>(R.id.ko)
        val zh = view.findViewById<CheckedTextView>(R.id.zh)
        val en = view.findViewById<CheckedTextView>(R.id.en)
        val ja = view.findViewById<CheckedTextView>(R.id.ja)
        builder.setView(view)
        builder.setPositiveButton("确定")
        { _, _ ->
            language.clear()
            if (zh.isChecked){
                language.add("zh")
            }
            if (en.isChecked){
                language.add("en")
            }
            if (ja.isChecked){
                language.add("ja")
            }
            if (yue.isChecked){
                language.add("yue")
            }
            if (ko.isChecked){
                language.add("ko")
            }
        }
        builder.setNegativeButton("取消")
        { _, _ ->
            Toast.makeText(this, "cancel!!!", Toast.LENGTH_SHORT).show()
        }
        builder.show()
    }
    private fun insHotWord() {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle("增加热词")
        val view = LayoutInflater.from(this).inflate(R.layout.edit_hot, null)
        val infEdit1 = view.findViewById<EditText>(R.id.infEdit1)
        val infEdit2 = view.findViewById<EditText>(R.id.infEdit2)
        val infEdit3 = view.findViewById<EditText>(R.id.infEdit3)
        builder.setView(view)
        builder.setPositiveButton("确定")
        { _, _ ->
            val word = Hotword(infEdit1.text.toString(),infEdit2.text.toString().toInt(),infEdit3.text.toString())
            wordList.add(word)
            updateHotword()
        }
        builder.setNegativeButton("取消")
        { _, _ ->
            Toast.makeText(this, "cancel!!!", Toast.LENGTH_SHORT).show()
        }
        builder.show()
    }

    /**
     * 热词更新
     */
    private fun updateHotword() {
        lifecycleScope.launch(Dispatchers.IO) {
            val vocabulary = JsonArray()
            for (word in wordList) {
                val jsonObj = JsonObject()
                jsonObj.addProperty("text", word.text)
                jsonObj.addProperty("weight", word.weight)
                jsonObj.addProperty("lang", word.lang)
                vocabulary.add(jsonObj)
            }
            val service = VocabularyService(ApiKeyUtil.dashScopeApiKey)
            if (vocId.isEmpty()) {
                val myVoc = service.createVocabulary(MODEL_V2, activityId, vocabulary)
                vocId = myVoc.vocabularyId
            } else {
                service.updateVocabulary(vocId, vocabulary)
            }
        }
    }

    /**
     * 权限相关
     */

    // 检查权限状态
    private fun checkPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    // 新增权限检查方法
    private fun checkAndRequestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                enableButtons(true)
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.RECORD_AUDIO
            ) -> {
                showPermissionRationale()
            }

            else -> {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.RECORD_AUDIO), 1
                )
            }
        }
    }

    // 处理权限请求结果
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableButtons(true)
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this, Manifest.permission.RECORD_AUDIO
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
        AlertDialog.Builder(this).setTitle("需要麦克风权限")
            .setMessage("语音识别功能需要使用麦克风权限，请允许权限申请")
            .setPositiveButton("去设置") { _, _ ->
                checkAndRequestPermission()
            }.setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    // 永久拒绝时的处理
    private fun showPermanentDenialDialog() {
        AlertDialog.Builder(this).setTitle("权限被永久拒绝")
            .setMessage("您已永久拒绝麦克风权限，请到应用设置中手动开启")
            .setPositiveButton("去设置") { _, _ ->
                openAppSettings()
            }.setNegativeButton("退出") { _, _ ->
                finish()
            }.setCancelable(false).show()
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
            SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
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
            recognizer.streamCall(param, audioSource).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe { result ->
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
        if (model == MODEL_V1) {
            return RecognitionParam.builder().model(MODEL_V1).format("pcm").sampleRate(SAMPLE_RATE)
                .apiKey(ApiKeyUtil.dashScopeApiKey)
//            .parameter("language_hints", arrayOf<String>("ja"))
                .build()
        } else {
            return RecognitionParam.builder().model(MODEL_V2).format("pcm").sampleRate(SAMPLE_RATE)
                .apiKey(ApiKeyUtil.dashScopeApiKey).parameter("language_hints", language).build()
        }
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
package com.example.recycleviewstudy.activity.paramode

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaExtractor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.alibaba.dashscope.audio.asr.recognition.Recognition
import com.alibaba.dashscope.audio.asr.recognition.RecognitionParam
import com.alibaba.dashscope.exception.NoApiKeyException
import com.example.recycleviewstudy.R
import com.example.recycleviewstudy.activity.utils.ApiKeyUtil
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.File


class FileTypeActivity : ComponentActivity() {
    private var resultText: TextView? = null
    private var selectButton: Button? = null
    private var recognizeButton: Button? = null
    private var exitButton: Button? = null
    private var selectedFile: File? = null
    private var isPermissionDeniedPermanently = false

    companion object {
        private const val FILE_SELECT_CODE = 100
        private val SUPPORTED_AUDIO_FORMATS = listOf("mp3", "wav", "aac", "flac", "m4a")
        private val SUPPORTED_VIDEO_FORMATS = listOf("mp4", "avi", "mkv", "mov")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_to_text2)

        resultText = findViewById(R.id.result_text)
        selectButton = findViewById(R.id.select_button)
        recognizeButton = findViewById(R.id.recognize_button)
        exitButton = findViewById(R.id.exit_button)

        // 初始化按钮状态
        updateButtonStates()

        // 文件选择按钮点击监听
        selectButton?.setOnClickListener {
            if (checkPermissionGranted()) {
                openFileSelector()
            } else {
                showPermissionRationale()
            }
        }

        // 识别按钮点击监听
        recognizeButton?.setOnClickListener {
            if (checkPermissionGranted()) {
                recognizeFile()
            } else {
                showPermissionRationale()
            }
        }

        exitButton?.setOnClickListener {
            finish()
        }
    }

    /**
     * 文件选择相关
     */
    private fun openFileSelector() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(
                Intent.EXTRA_MIME_TYPES, arrayOf(
                    "audio/*",
//                    "video/*"
                )
            )
        }
        startActivityForResult(intent, FILE_SELECT_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                val filePath = getFilePathFromUri(uri)
                filePath?.let {
                    selectedFile = File(it)
                    updateButtonStates()
                    resultText?.text = "已选择文件: ${selectedFile?.name}"
                }
            }
        }
    }

    private fun getFilePathFromUri(uri: Uri): String? {
        var path: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                path = it.getString(columnIndex)
            }
        }
        return path ?: uri.path
    }

    private fun isFileSupported(file: File): Boolean {
        val extension = file.extension.lowercase()
        return SUPPORTED_AUDIO_FORMATS.contains(extension) ||
                SUPPORTED_VIDEO_FORMATS.contains(extension)
    }

    /**
     * 语音识别相关
     */
    private fun recognizeFile() {
        selectedFile?.let { file ->
            if (!isFileSupported(file)) {
                resultText?.text = "不支持的文件格式，请选择音频或视频文件"
                return
            }

            recognizeButton?.isEnabled = false
            resultText?.text = "识别中..."

            // 在后台线程执行识别任务
            Thread {
                try {
                    val recognizer = Recognition()
                    val param = buildRecognitionParam()

                    // 调用同步识别接口
                    val result = recognizer.call(param, file)

                    runOnUiThread {
                        var gson = Gson()
                        val jsonObject: JsonObject = gson.fromJson(result, JsonObject::class.java)
                        //herr
                        var mResultText = ""
                        if (jsonObject.has("sentences")) {
                            for (sent in jsonObject.get("sentences").asJsonArray) {
                                mResultText = sent.asJsonObject.get("text").asString
                                Log.d("md","txt:$resultText")
                            }
                        }
                        resultText?.text = "识别结果:\n$mResultText"
                        recognizeButton?.isEnabled = true
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        resultText?.text = "识别失败: ${e.message}"
                        recognizeButton?.isEnabled = true
                    }
                }
            }.start()
        } ?: run {
            resultText?.text = "请先选择文件"
        }
    }

    @Throws(NoApiKeyException::class)
    private fun buildRecognitionParam(): RecognitionParam {
        return RecognitionParam.builder()
            .model("paraformer-realtime-v1")
            .format("wav") // 根据实际API支持调整
//            .format("mp3")
            .sampleRate(16000)
            .apiKey(ApiKeyUtil.dashScopeApiKey)
            .build()
    }

    /**
     * 权限相关 (保留原有逻辑)
     */
    private fun checkPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkAndRequestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                enableButtons(true)
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                showPermissionRationale()
            }

            else -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1
                )
            }
        }
    }

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
                            Manifest.permission.READ_EXTERNAL_STORAGE
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

    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("需要文件访问权限")
            .setMessage("文件识别功能需要读取存储权限，请允许权限申请")
            .setPositiveButton("去设置") { _, _ ->
                checkAndRequestPermission()
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showPermanentDenialDialog() {
        AlertDialog.Builder(this)
            .setTitle("权限被永久拒绝")
            .setMessage("您已永久拒绝文件访问权限，请到应用设置中手动开启")
            .setPositiveButton("去设置") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("退出") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    /**
     * UI状态更新
     */
    private fun updateButtonStates() {
        recognizeButton?.isEnabled = selectedFile != null
    }

    private fun enableButtons(enabled: Boolean) {
        selectButton?.isEnabled = enabled
        recognizeButton?.isEnabled = enabled && selectedFile != null
        exitButton?.isEnabled = enabled
    }
}

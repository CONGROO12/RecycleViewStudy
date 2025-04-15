package com.example.recycleviewstudy.activity.utils

import com.alibaba.dashscope.exception.NoApiKeyException
import com.alibaba.dashscope.utils.ApiKey

object ApiKeyUtil {
    @get:Throws(NoApiKeyException::class)
     val dashScopeApiKey: String
        // 保持原 getDashScopeApiKey 方法逻辑
        get() {
            var dashScopeApiKey: String? = null
            try {
                val apiKey: ApiKey = ApiKey()
                dashScopeApiKey = ApiKey.getApiKey(null)// Retrieve from environment variable.
            } catch (e: NoApiKeyException) {
                println("No API key found in environment.")
            }
            if (dashScopeApiKey == null) {
                // If you cannot set api_key in your environment variable,
                // you can set it here by code
                dashScopeApiKey = "sk-21e066446f00453187ff30d22406f853"
            }
            return dashScopeApiKey
        }
}
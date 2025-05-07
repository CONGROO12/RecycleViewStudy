package com.example.recycleviewstudy.activity.paramode

import android.os.Bundle
import java.util.UUID

class CustTypeActivity: LongTypeActivity() {
    override var model = MODEL_V2
    override var vocAble = true
    override var language = arrayListOf<String>("zh","en")
    override fun onCreate(savedInstanceState: Bundle?) {
        if (activityId=="0") {
            activityId = UUID.randomUUID().toString().substring(0,8)
        }
        super.onCreate(savedInstanceState)
    }
}
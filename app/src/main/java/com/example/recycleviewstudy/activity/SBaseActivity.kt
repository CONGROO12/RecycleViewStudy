package com.example.recycleviewstudy.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.recycleviewstudy.item.Item
import com.example.recycleviewstudy.R
import com.example.recycleviewstudy.item.initItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.LinkedList

open class SBaseActivity : ComponentActivity() {
    protected var myList=LinkedList<Item>()
    protected var itemXml= R.layout.vertical_recycler_item
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val json = getSharedPreferences("data_item",Context.MODE_PRIVATE).getString("list","")
        if(json=="")
        {
            initItem(myList,3)
        }
        else
        {
            val type = object : TypeToken<LinkedList<Item>>(){}.type
            myList=Gson().fromJson(json, type)
            Log.d("sb",myList.toString())
        }
        setContentView(R.layout.main_layout)
    }

    override fun onDestroy() {
        super.onDestroy()
        val editor = getSharedPreferences("data_item", Context.MODE_PRIVATE).edit()
        val json = Gson().toJson(myList)
        editor.clear()
        editor.putString("list",json)
        editor.apply()
    }
}

package com.example.recycleviewstudy.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recycleviewstudy.R

class S2HorizontalActivity : SBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val recyclerView:RecyclerView = findViewById(R.id.recyclerView)
        itemXml= R.layout.horizontal_recycler_item
        val layoutManager = LinearLayoutManager(this)
        val adapter = MyAdapter(myList,itemXml)
        recyclerView.layoutManager =layoutManager
        recyclerView.adapter =adapter
        layoutManager.orientation=LinearLayoutManager.HORIZONTAL
    }
}

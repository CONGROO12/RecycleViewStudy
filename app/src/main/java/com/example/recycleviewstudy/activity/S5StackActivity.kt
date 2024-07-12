package com.example.recycleviewstudy.activity

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.example.recycleviewstudy.R
import com.example.recycleviewstudy.layout_manager.StackLayoutManager

class S5StackActivity : SBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val recyclerView:RecyclerView = findViewById(R.id.recyclerView)
        itemXml= R.layout.vertical_recycler_item
        val layoutManager = StackLayoutManager(this)
        val adapter = MyAdapter(myList,itemXml)
        recyclerView.layoutManager =layoutManager
        recyclerView.adapter =adapter

    }
}

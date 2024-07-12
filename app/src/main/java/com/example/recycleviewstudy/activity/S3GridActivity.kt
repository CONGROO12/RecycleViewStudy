package com.example.recycleviewstudy.activity

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recycleviewstudy.R

class S3GridActivity : SBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val recyclerView:RecyclerView = findViewById(R.id.recyclerView)
        itemXml= R.layout.grid_recycler_item
        val layoutManager = GridLayoutManager(this,3)
        val adapter = MyAdapter(myList,itemXml)
        recyclerView.layoutManager =layoutManager
        recyclerView.adapter =adapter
    }
}

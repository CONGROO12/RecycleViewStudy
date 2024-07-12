package com.example.recycleviewstudy.activity

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.recycleviewstudy.R

class S4StaggeredActivity : SBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val recyclerView:RecyclerView = findViewById(R.id.recyclerView)
        itemXml= R.layout.grid_recycler_item
        val layoutManager = StaggeredGridLayoutManager(3,StaggeredGridLayoutManager.VERTICAL)
        val adapter = MyAdapter(myList,itemXml)
        recyclerView.layoutManager =layoutManager
        recyclerView.adapter =adapter
    }
}

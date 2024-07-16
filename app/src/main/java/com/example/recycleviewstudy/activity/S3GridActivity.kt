package com.example.recycleviewstudy.activity

import android.graphics.Rect
import android.os.Bundle
import android.view.View
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
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                outRect.bottom=20
                outRect.right=10
                outRect.left=10
            }
        })
    }
}

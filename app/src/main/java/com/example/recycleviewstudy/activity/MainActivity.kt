package com.example.recycleviewstudy.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recycleviewstudy.R
import com.example.recycleviewstudy.item.Study
import com.example.recycleviewstudy.service.WxsService

class MainActivity : ComponentActivity() {
    lateinit var wxsBinder:WxsService.MusicBinder
    private val myList = ArrayList<Study>()
    private val connection=object :ServiceConnection{
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder) {
            wxsBinder=p1 as WxsService.MusicBinder
            wxsBinder.play()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {

        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_layout)
        initItem()
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val layoutManager = GridLayoutManager(this, 3)
        val adapter = MyMainAdapter(myList)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        val intent=Intent(this,WxsService::class.java)
        bindService(intent,connection,Context.BIND_AUTO_CREATE)
    }

    private fun initItem() {

        myList.add(Study("垂直滑动", R.drawable.m1, S1VerticalActivity::class.java))
        myList.add(Study("水平滑动", R.drawable.m2, S2HorizontalActivity::class.java))
        myList.add(Study("网格滑动", R.drawable.m3, S3GridActivity::class.java))
        myList.add(Study("瀑布流滑动", R.drawable.m4, S4StaggeredActivity::class.java))
        myList.add(Study("梯形流动", R.drawable.m5, S5StackActivity::class.java))
    }

    class MyMainAdapter(private val myList: List<Study>) :
        RecyclerView.Adapter<MyMainAdapter.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val itemImage: ImageView = view.findViewById(R.id.image)

            val itemInf: TextView = view.findViewById(R.id.inf)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.study, parent, false)
            val viewHolder = ViewHolder(view)
            viewHolder.itemView.setOnClickListener {
                val position = viewHolder.bindingAdapterPosition
                if (position == itemCount - 1) {
                    (parent.context as MainActivity).wxsBinder.play()
                } else {
                    val item = myList[position]
                    val intent = Intent(parent.context, item.activityClass)
                    parent.context.startActivity(intent)
                }
            }
            return viewHolder
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (position == itemCount - 1) {
                holder.itemImage.setImageResource(R.drawable.wxs)
                holder.itemInf.text = "播放妄想税！"
            } else {
                val item = myList[position]
                holder.itemImage.setImageResource(item.imageId)
//            holder.itemName.text=item.name
                holder.itemInf.text = item.inf
            }
        }

        override fun getItemCount(): Int {
            return myList.size + 1
        }
    }
}

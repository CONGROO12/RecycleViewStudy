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
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recycleviewstudy.R
import com.example.recycleviewstudy.activity.paramode.FileTypeActivity
import com.example.recycleviewstudy.activity.paramode.LongTypeActivity
import com.example.recycleviewstudy.item.Study
import com.example.recycleviewstudy.item.addStudy
import com.example.recycleviewstudy.service.WxsService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : ComponentActivity() {
    lateinit var wxsBinder: WxsService.MusicBinder
    private var myList = ArrayList<Study>()
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder) {
            wxsBinder = p1 as WxsService.MusicBinder
//            wxsBinder.play()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val json = getSharedPreferences("study_item", Context.MODE_PRIVATE).getString("list", "")
        if (json == "") {
            Log.d("md", "getnull")
            initItem()
        } else {
            val type = object : TypeToken<ArrayList<Study>>() {}.type
            myList = Gson().fromJson(json, type)
        }
        setContentView(R.layout.main_layout)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val layoutManager = GridLayoutManager(this, 3)
        val adapter = MyMainAdapter(myList)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        val intent = Intent(this, WxsService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("md", "destroy")
        val editor = getSharedPreferences("study_item", Context.MODE_PRIVATE).edit()
        Log.d("md","jsonsta")
        try {
        val json = Gson().toJson(myList)
        Log.d("md","json:$json")
        editor.clear()
        editor.putString("list", json)
        val rs = editor.commit()
        Log.d("md", "rs:$rs")}catch (e:Exception){
            Log.d("md","errmsg:${e.message}")
        }
    }

    private fun initItem() {
        myList.add(Study("长时实时识别", R.drawable.iconblue, LongTypeActivity::class.java.name))
        myList.add(Study("文件识别", R.drawable.icongreen, FileTypeActivity::class.java.name))
//        myList.add(Study("水平滑动", R.drawable.m2, S2HorizontalActivity::class.java))
//        myList.add(Study("自定义模型", R.drawable.iconpurple, S3GridActivity::class.java))
//        myList.add(Study("瀑布流滑动", R.drawable.m4, S4StaggeredActivity::class.java))
//        myList.add(Study("梯形流动", R.drawable.m5, S5StackActivity::class.java))
    }

    class MyMainAdapter(private val myList: ArrayList<Study>) :
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
//                    (parent.context as MainActivity).wxsBinder.play()
                    insItem(position, parent)
                } else {
                    val item = myList[position]
                    val intent = Intent(parent.context, Class.forName(item.activityClass))
                    parent.context.startActivity(intent)
                }
            }
            return viewHolder
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (position == itemCount - 1) {
                holder.itemImage.setImageResource(R.drawable.add_24px)
                holder.itemInf.text = "添加自定义方案"
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

        private fun insItem(position: Int, parent: ViewGroup) {
            val builder = MaterialAlertDialogBuilder(parent.context)
            builder.setTitle("增加信息")
//            val view = LayoutInflater.from(parent.context).inflate(R.layout.num_edit_item, null)
//            val infEdit = view.findViewById<EditText>(R.id.numEdit)
//            var num = 1
//            infEdit.setText(num.toString())
//            builder.setView(view)
            builder.setPositiveButton("确定")
            { _, _ ->
                addStudy(position, myList)
                this.notifyItemInserted(position)
//                }
                this.notifyItemRangeChanged(position, myList.size)
            }
            builder.setNegativeButton("取消")
            { _, _ ->
                Toast.makeText(parent.context, "cancel!!!", Toast.LENGTH_SHORT).show()
            }
            builder.show()
        }
    }
}

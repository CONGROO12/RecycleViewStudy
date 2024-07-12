package com.example.recycleviewstudy.activity

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.recycleviewstudy.item.Item
import com.example.recycleviewstudy.R
import com.example.recycleviewstudy.item.addItem
import java.util.LinkedList

class MyAdapter(private val myList: LinkedList<Item>, private val itemXml:Int) : RecyclerView.Adapter<MyAdapter.ViewHolder>()
{
    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view)
    {
        val itemImage: ImageView =view.findViewById(R.id.image)
        val itemName: TextView =view.findViewById(R.id.name)
        val itemInf: TextView =view.findViewById(R.id.inf)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(itemXml,parent,false)
        val viewHolder=ViewHolder(view)
        viewHolder.itemView.setOnLongClickListener {
            val position=viewHolder.absoluteAdapterPosition
//            val item=myList[position]
//            Toast.makeText(parent.context,"long!", Toast.LENGTH_SHORT).show()
            val builder=AlertDialog.Builder(parent.context)
            builder.setTitle("进行什么操作?")
            builder.setMessage("增加还是删除?")
            builder.setPositiveButton("增加")
            {_,_->
                insItem(position,parent)
            }
            builder.setNegativeButton("删除")
            {_,_->
                delItem(position,parent)
            }
            builder.setNeutralButton("取消"){_,_->}
            builder.show()
            true
        }
        viewHolder.itemView.setOnClickListener {
            val position=viewHolder.absoluteAdapterPosition
            Toast.makeText(parent.context,"click!",Toast.LENGTH_SHORT).show()
            setInf(position,parent)
        }
        return viewHolder
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item=myList[position]
        holder.itemImage.setImageResource(item.imageId)
        holder.itemName.text= buildString {
            append(item.name)
            append(position+1)
        }
        holder.itemInf.text=item.inf
    }

    override fun getItemCount(): Int {
        return  myList.size
    }

    private fun delItem(position: Int,parent: ViewGroup)
    {
        val builder=AlertDialog.Builder(parent.context)
        builder.setTitle("是否删除?")
        builder.setMessage("是否删除此项?")
        builder.setPositiveButton("是")
        {_,_->
            Toast.makeText(parent.context,"del!!",Toast.LENGTH_SHORT).show()
            myList.removeAt(position)
            this.notifyItemRemoved(position)
            this.notifyItemRangeChanged(position,myList.size)
        }
        builder.setNegativeButton("否")
        {_,_->
            Toast.makeText(parent.context,"cancel!!!",Toast.LENGTH_SHORT).show()
        }
        builder.show()
    }

    private fun setInf(position: Int, parent: ViewGroup) {
        val builder=AlertDialog.Builder(parent.context)
        builder.setTitle("编辑信息")
        val view=LayoutInflater.from(parent.context).inflate(R.layout.edit_item,null)
        val infEdit=view.findViewById<EditText>(R.id.infEdit)
        val item=myList[position]
        infEdit.setText(item.inf)
        builder.setView(view)
        builder.setPositiveButton("保存")
        {_,_->
            Toast.makeText(parent.context,"set!!",Toast.LENGTH_SHORT).show()
            val newInf=infEdit.text.toString()
            item.inf=newInf
            this.notifyItemChanged(position)
        }
        builder.setNegativeButton("取消")
        {_,_->
            Toast.makeText(parent.context,"cancel!!!",Toast.LENGTH_SHORT).show()
        }
        builder.show()
    }
    private fun insItem(position: Int, parent: ViewGroup) {
        val builder=AlertDialog.Builder(parent.context)
        builder.setTitle("增加信息")
        val view=LayoutInflater.from(parent.context).inflate(R.layout.num_edit_item,null)
        val infEdit=view.findViewById<EditText>(R.id.numEdit)
        var num=1
        infEdit.setText(num.toString())
        builder.setView(view)
        builder.setPositiveButton("确定")
        {_,_->
            num=infEdit.text.toString().toInt()
            for (i in 0 until  num)
            {
                addItem(position+i,myList)
                this.notifyItemInserted(position+i)
            }
            this.notifyItemRangeChanged(position,myList.size)
        }
        builder.setNegativeButton("取消")
        {_,_->
            Toast.makeText(parent.context,"cancel!!!",Toast.LENGTH_SHORT).show()
        }
        builder.show()
    }
}
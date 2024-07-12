package com.example.recycleviewstudy.item

import com.example.recycleviewstudy.R
import java.util.LinkedList

val  imageList:ArrayList<Int> = arrayListOf(
    R.drawable.m1,
    R.drawable.m2,
    R.drawable.m3,
    R.drawable.m4,
    R.drawable.m5,
    R.drawable.m6,
    R.drawable.m7,
    R.drawable.m8,
    R.drawable.m9,
    R.drawable.m10
)
fun initItem(myList: LinkedList<Item>, time:Int) {
    for(i in 0 until time*10)
    {
        val p=i+1
        myList.add(Item("item", getRLS("inf$p"), getRLI()))
    }
}
fun addItem(start:Int, myList: LinkedList<Item>)
{
    myList.add(start, Item("item","newInf", getRLI()))
}
private fun getRLS(string: String):String
{
    val n=(1..20).random()
    val builder=StringBuilder()
    repeat(n)
    {
        builder.append(string)
    }
    return builder.toString()
}
private fun getRLI():Int
{
    val n=(0 until  imageList.size).random()
    return imageList[n]
}
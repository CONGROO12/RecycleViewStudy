package com.example.recycleviewstudy.layout_manager

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class StackLayoutManager(val context: Context) : RecyclerView.LayoutManager() {
    private var mOffset = 0
    private var mFirstVisiPos = 0
    private var mLastVisiPos = 0
    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.WRAP_CONTENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onLayoutChildren(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ) {
        if (itemCount == 0) {
            detachAndScrapAttachedViews(recycler)
            return
        }
        detachAndScrapAttachedViews(recycler)
        mFirstVisiPos = 0
        mLastVisiPos = itemCount
        mOffset = 0
        fill(recycler, state, 0)
    }

    override fun canScrollVertically(): Boolean {
        return true
    }

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        var varDy = dy
        if (dy == 0 || childCount == 0) {
            return 0
        }
        if (mOffset + varDy < 0) {
            varDy = -mOffset
        } else if (varDy > 0) {
            val last = getChildAt(childCount - 1)
            if (last?.let { getPosition(it) } == itemCount - 1) {
                val gap = height - paddingBottom - getDecoratedBottom(last)
                if (gap > 0) {
                    varDy = -gap
                } else if (gap == 0) {
                    varDy = 0
                } else {
                    varDy = varDy.coerceAtMost(-gap)
                }
            }
        }
        varDy = fill(recycler, state, varDy)
        mOffset += varDy
        offsetChildrenVertical(-varDy)
        return varDy
    }


    private fun fill(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        dy: Int
    ): Int {
        var topOffset = paddingTop
        val leftOffset = paddingLeft
        if (childCount > 0) {
            for (i in childCount - 1 downTo 0) {
                val child = getChildAt(i)
                if (dy > 0 && getDecoratedBottom(child!!) - dy < topOffset) {
                    removeAndRecycleView(child, recycler)
                    mFirstVisiPos++
                } else if (dy < 0 && getDecoratedTop(child!!) - dy > height - paddingBottom) {
                    removeAndRecycleView(child, recycler)
                    mLastVisiPos--
                }
            }
        }
        if (dy >= 0) {
            var minPos = mFirstVisiPos
            mLastVisiPos = itemCount - 1
            if (childCount > 0) {
                val last = getChildAt(childCount - 1)
                minPos = getPosition(last!!) + 1
                topOffset = getDecoratedBottom(last)
            }
            for (i in minPos..mLastVisiPos) {
                val child = recycler.getViewForPosition(i)
                addView(child)
                measureChildWithMargins(child, 0, 0)
                val w = getDecoratedMeasurementHorizontal(child)
                val h = getDecoratedMeasurementVertical(child)
                layoutDecoratedWithMargins(
                    child,
                    leftOffset,
                    topOffset,
                    leftOffset + w,
                    topOffset + h
                )
                if (topOffset - dy > height - paddingBottom) {
                    removeAndRecycleView(child, recycler)
                    mLastVisiPos = i - 1
                }
                topOffset += h
            }
        } else {
            var maxPos = mLastVisiPos
            mFirstVisiPos = 0
            var bottomOffset = topOffset
            if (childCount > 0) {
                val first = getChildAt(0)
                maxPos = getPosition(first!!) - 1
                bottomOffset = getDecoratedTop(first)
            }
            for (i in maxPos downTo mFirstVisiPos) {
                val child = recycler.getViewForPosition(i)
                addView(child, 0)
                measureChildWithMargins(child, 0, 0)
                val w = getDecoratedMeasurementHorizontal(child)
                val h = getDecoratedMeasurementVertical(child)
                layoutDecoratedWithMargins(
                    child,
                    leftOffset,
                    bottomOffset - h,
                    leftOffset + w,
                    bottomOffset
                )
                if (bottomOffset - dy < paddingTop) {
                    removeAndRecycleView(child, recycler)
                    mFirstVisiPos = i + 1
                }
                bottomOffset -= h
            }
        }
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val minScale = 0.6f
            var curScale = 0f
            val center = height / 2
            val childCenter = (getDecoratedTop(child!!) + getDecoratedBottom(child)) / 2
            val fScale = abs(center - childCenter) / (center * 1.0f)
            curScale = 1.0f - (1.0f - minScale) * fScale
            child.scaleX = curScale
            child.scaleY = curScale
            child.alpha = curScale
        }
        return dy
    }

    private fun getDecoratedMeasurementVertical(view: View): Int {
        val params: RecyclerView.LayoutParams = view.layoutParams as RecyclerView.LayoutParams
        return getDecoratedMeasuredHeight(view) + params.topMargin + params.bottomMargin
    }

    private fun getDecoratedMeasurementHorizontal(view: View): Int {
        val params: RecyclerView.LayoutParams = view.layoutParams as RecyclerView.LayoutParams
        return getDecoratedMeasuredWidth(view) + params.leftMargin + params.rightMargin
    }
}
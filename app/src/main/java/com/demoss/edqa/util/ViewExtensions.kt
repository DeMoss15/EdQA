package com.demoss.edqa.util

import android.widget.SeekBar
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.demoss.edqa.base.BaseRecyclerViewAdapter
import com.demoss.edqa.presentation.adapter.SimpleSwipeItemCallback

fun SeekBar.setOnProgressChangeListener(listener: (Int) -> Unit) {
    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            listener(progress)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }
    })
}

fun RecyclerView.addItemTouchHelperWithCallback(callback: ItemTouchHelper.SimpleCallback) {
    context?.let {
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(this)
    }
}

fun <T> RecyclerView.setupSwipeToDelete(adapter: BaseRecyclerViewAdapter<T, *>, onItemDeleteAction: (T) -> Unit) {
    this.addItemTouchHelperWithCallback(SimpleSwipeItemCallback { itemPosition ->
        adapter.apply {
            onItemDeleteAction(differ.currentList[itemPosition])
            differ.submitList(differ.currentList.toMutableList().apply { removeAt(itemPosition) })
        }
    })
}

fun FragmentManager.transaction(allowingStateLoss: Boolean = false, actions: FragmentTransaction.() -> Unit) {
    with(this.beginTransaction().apply {
        actions()
    }) {
        if (allowingStateLoss) {
            this.commitAllowingStateLoss()
        } else {
            commit()
        }
    }
}
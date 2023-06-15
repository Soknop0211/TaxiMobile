package com.eazy.daiku.utility.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import java.lang.reflect.Constructor

class BasePaginationAdapter<ITEM>(
    val self: Any,
    private val itemLayoutId: Int,
    private val diffCallback: DiffUtil.ItemCallback<ITEM>,
    private val holderClass: Class<out BasePaginationViewHolder<ITEM>>,
) :
    PagedListAdapter<ITEM, BasePaginationAdapter.BasePaginationViewHolder<ITEM>>(diffCallback) {

    private lateinit var paginationAdapterListener: (ITEM) -> Unit

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BasePaginationViewHolder<ITEM> {
        val view: View = LayoutInflater.from(parent.context).inflate(itemLayoutId, parent, false)
        try {
            val constructor: Constructor<out BasePaginationViewHolder<ITEM>> =
                holderClass.getDeclaredConstructor(
                    View::class.java
                )
            constructor.isAccessible = true
            return constructor.newInstance(view)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        throw NullPointerException()
    }

    override fun onBindViewHolder(holder: BasePaginationViewHolder<ITEM>, position: Int) {
        TODO("Not yet implemented")
    }

    //Core holder
    abstract class BasePaginationViewHolder<DATA>(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        abstract fun onBind(data: DATA, listener: (DATA) -> Unit)

    }

    fun setPaginationAdapterListener(paginationAdapterListener: (ITEM) -> Unit) {
        this.paginationAdapterListener = paginationAdapterListener
    }

}


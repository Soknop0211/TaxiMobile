package com.eazy.daiku.utility

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eazy.daiku.R
import com.eazy.daiku.utility.base.BaseListenerAutoFillNumber

import com.google.android.material.card.MaterialCardView

class AutoFillNumberRecyclerView : RecyclerView {
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defstyle: Int) : super(context,
        attrs,
        defstyle) {
        init()
    }

    var spanCount = 4
    private val list: ArrayList<Item> = object : ArrayList<Item>() {
        init {
            add(Item(5, "5"))
            add(Item(10, "10"))
            add(Item(20, "20"))
            add(Item(50, "50"))
            add(Item(100, "100"))
            add(Item(200, "200"))
            add(Item(500, "500"))
            add(Item(1000, "1000"))
        }
    }
    private var baseListener: BaseListenerAutoFillNumber<Item?>? = null

    fun setBaseListener(baseListener: BaseListenerAutoFillNumber<Item?>) {
        this.baseListener = baseListener
    }

    fun init() {
        val adapter: Adapter = object : Adapter(list) {
            override fun getBaseListener(): BaseListenerAutoFillNumber<Item?>? {
                return baseListener
            }
        }
        val gridLayoutManager = GridLayoutManager(context, spanCount)
        layoutManager = gridLayoutManager
        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.spacing_small)
        val decoration = GridSpacingItemDecoration(spanCount, spacingInPixels, false)
        addItemDecoration(decoration)
        clipToPadding = false
        setAdapter(adapter)
    }



    @SuppressLint("NotifyDataSetChanged")
    abstract class Adapter(list: ArrayList<Item>) :
        RecyclerView.Adapter<ViewHolder>() {
        private val list: MutableList<Item> = ArrayList()
        var rowIndex = -1
        abstract fun getBaseListener(): BaseListenerAutoFillNumber<Item?>?
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.auto_fill_number_view, parent, false)
            return ViewHolder(view)
        }

        public fun selectPositionItem(selectPosition: Int) {
            this.rowIndex = selectPosition
            notifyDataSetChanged()
        }


        @SuppressLint("ResourceAsColor")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.onBind(item)
            holder.selectItem(rowIndex == position)
            holder.itemView.setOnClickListener { v: View? ->
                this.rowIndex = position
                if (getBaseListener() != null) {
                    getBaseListener()?.onResult(item)
                }
                notifyDataSetChanged()
            }
        }

        override fun getItemCount(): Int {
            return list.size
        }

        init {
            this.list.clear()
            this.list.addAll(list)
            notifyDataSetChanged()
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val autoFillTextView: TextView
        val autoContainerNumber: MaterialCardView
        fun onBind(item: Item) {
            autoFillTextView.text = String.format("%s", item.numberStr)
        }

        init {
            autoFillTextView = itemView.findViewById(R.id.autoFillTextView)
            autoContainerNumber = itemView.findViewById(R.id.container_number)
        }

        fun selectItem(selectItem: Boolean) {
            if (selectItem) {
                autoContainerNumber.strokeWidth = 6
                autoContainerNumber.setStrokeColor(ContextCompat.getColor(itemView.context,
                    R.color.colorPrimary))
            } else {
                autoContainerNumber.strokeWidth = 2
                autoContainerNumber.setStrokeColor(ContextCompat.getColor(itemView.context,
                    R.color.colorAccent))
            }
        }
    }

    class Item(var number: Int, var numberStr: String)

    class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacing: Int,
        private val includeEdge: Boolean,
    ) :
        ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: State) {
            val position = parent.getChildAdapterPosition(view) // item position
            val column = position % spanCount // item column
            if (includeEdge) {
                outRect.left =
                    spacing - column * spacing / spanCount // spacing - column * ((1f / spanCount) * spacing)
                outRect.right =
                    (column + 1) * spacing / spanCount // (column + 1) * ((1f / spanCount) * spacing)
                if (position < spanCount) { // top edge
                    outRect.top = spacing
                }
                outRect.bottom = spacing // item bottom
            } else {
                outRect.left = column * spacing / spanCount // column * ((1f / spanCount) * spacing)
                outRect.right =
                    spacing - (column + 1) * spacing / spanCount // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing // item top
                }
            }
        }
    }

}
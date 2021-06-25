package com.itgo.book_cloud.ui.components


import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.itgo.book_cloud.R
import com.itgo.book_cloud.common.setImageURI
import com.itgo.book_cloud.data.model.ChainBook
import com.itgo.book_cloud.data.model.OriginBook
import kotlin.math.min

class BookRecAdapter(
    context: Context,
    val data: List<OriginBook>,
    private val onClickBookCard: (view: View, holder: BookRecItemViewHolder, book: OriginBook) -> Unit,
) :
    RecyclerView.Adapter<BookRecAdapter.BookRecItemViewHolder>() {
    private val bgs =
        listOf(R.drawable.bg_1, R.drawable.bg_3, R.drawable.bg_2, R.drawable.bg_5, R.drawable.bg_4)
    private val inflater: LayoutInflater = LayoutInflater.from(context)


    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookRecItemViewHolder {
        val view: View = inflater.inflate(R.layout.item_rec_book, null, false)

        return BookRecItemViewHolder(view, onClickBookCard)
    }

    override fun onBindViewHolder(holder: BookRecItemViewHolder, position: Int) {
        holder.bind(bgs[position], data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class BookRecItemViewHolder(
        itemView: View,
        private val onClickBookCard: (view: View, holder: BookRecItemViewHolder, book: OriginBook) -> Unit,
    ) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val bookRecItemBox: View = itemView.findViewById(R.id.bookRecItemBox)
        private val bookCoverView: ImageView = itemView.findViewById(R.id.bookCover)
        private val bookTitleView: TextView = itemView.findViewById(R.id.bookTitle)
        private val bookRecBgView: ImageView = itemView.findViewById(R.id.bookRecBg)

        private var ob: OriginBook? = null

        init {
            bookRecItemBox.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            if (v != null && ob != null) onClickBookCard(v, this, ob!!)
        }

        fun bind(bg: Int, ob: OriginBook?) {
            this.ob = ob
            if (ob == null) {
                bookRecItemBox.visibility = View.INVISIBLE
            } else {
                bookRecItemBox.visibility = View.VISIBLE
                bookTitleView.text = ob.name
                bookRecBgView.setImageResource(bg)
                if (ob.cover.isNullOrEmpty()) {
                    bookCoverView.setImageResource(R.drawable.book_cover_default)
                } else {
                    bookCoverView.setImageURI(ob.cover, R.drawable.book_cover_default)
                }

            }
        }
    }
}



package com.itgo.book_cloud.ui.components


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
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


class UserRecAdapter(
    context: Context,
    val data: List<OriginBook>,
    private val onClickBookCard: (view: View, holder: UserRecItemViewHolder, book: OriginBook) -> Unit,
) :
    RecyclerView.Adapter<UserRecAdapter.UserRecItemViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)


    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserRecItemViewHolder {
        val view: View = inflater.inflate(R.layout.item_rec_user, null, false)

        return UserRecItemViewHolder(view, onClickBookCard)
    }

    override fun onBindViewHolder(holder: UserRecItemViewHolder, position: Int) {
        holder.bind(position + 1, data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class UserRecItemViewHolder(
        itemView: View,
        private val onClickBookCard: (view: View, holder: UserRecItemViewHolder, book: OriginBook) -> Unit,
    ) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val userRecItemBox: View = itemView.findViewById(R.id.userRecItemBox)
        private val bookCoverView: ImageView = itemView.findViewById(R.id.bookCover)
        private val bookTitleView: TextView = itemView.findViewById(R.id.bookTitle)
        private val numberView: TextView = itemView.findViewById(R.id.number)

        private var ob: OriginBook? = null

        init {
            userRecItemBox.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            if (v != null && ob != null) onClickBookCard(v, this, ob!!)
        }

        fun bind(idx: Int, ob: OriginBook?) {
            this.ob = ob
            if (ob == null) {
                userRecItemBox.visibility = View.INVISIBLE
            } else {
                userRecItemBox.visibility = View.VISIBLE
                bookTitleView.text = ob.name
                numberView.text = idx.toString()
                if (idx <= 3) {
                    numberView.setTextColor(Color.rgb(166, 124, 64))
                }
                if (ob.cover.isNullOrEmpty()) {
                    bookCoverView.setImageResource(R.drawable.book_cover_default)
                } else {
                    bookCoverView.setImageURI(ob.cover, R.drawable.book_cover_default)
                }
            }
        }
    }
}



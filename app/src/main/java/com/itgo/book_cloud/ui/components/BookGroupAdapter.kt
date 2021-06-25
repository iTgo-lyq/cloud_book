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
import com.itgo.book_cloud.data.model.ChainBook


class BookGroupAdapter(
    context: Context,
    val data: List<ChainBook>,
    private val onClickBookCard: (view: View, holder: BookCardLineViewHolder, chainBook: ChainBook) -> Unit,
    private val onSelectBookCard: (view: View, holder: BookCardLineViewHolder, chainBook: ChainBook) -> Unit
) :
    RecyclerView.Adapter<BookGroupAdapter.BookCardLineViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)


    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookCardLineViewHolder {
        val view: View = inflater.inflate(R.layout.item_book_card, null, false)

        return BookCardLineViewHolder(view, onClickBookCard, onSelectBookCard)
    }

    override fun onBindViewHolder(holder: BookCardLineViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class BookCardLineViewHolder(
        itemView: View,
        private val onClickBookCard: (view: View, holder: BookCardLineViewHolder, chainBook: ChainBook) -> Unit,
        private val onSelectBookCard: (view: View, holder: BookCardLineViewHolder, chainBook: ChainBook) -> Unit
    ) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        private val bookCard: View = itemView.findViewById(R.id.bookCard)
        private val coverView: ImageView = itemView.findViewById(R.id.bookCover)
        private val bookMaskView: View = itemView.findViewById(R.id.bookMask)
        private val titleView: TextView = itemView.findViewById(R.id.bookTitle)
        private val subTitleBox: View = itemView.findViewById(R.id.bookSubTitleBox)
        private val subTitleLeftView: TextView = itemView.findViewById(R.id.bookSubTitleLeft)
        private val subTitleRightView: TextView = itemView.findViewById(R.id.bookSubTitleRight)
        private val workProgressView: ProgressBar = itemView.findViewById(R.id.workProgress)

        private var isWorking = false
        private var chainBook: ChainBook? = null

        init {
            bookCard.setOnClickListener(this)
            bookCard.setOnLongClickListener(this)
        }

        override fun onClick(v: View?) {
            if (v == null || chainBook == null) return

            if (isWorking) Toast.makeText(v.context, "不用重复点，没🐛，😊", Toast.LENGTH_SHORT).show()

            onClickBookCard(v, this, chainBook!!)
        }

        override fun onLongClick(v: View?): Boolean {
            if (v == null || chainBook == null) return false

            if (isWorking) Toast.makeText(v.context, "正在下载😊", Toast.LENGTH_SHORT).show()
            else onSelectBookCard(v, this, chainBook!!)

            return true
        }

        fun bind(cb: ChainBook?) {
            chainBook = cb
            subTitleBox.visibility = View.VISIBLE
            workProgressView.visibility = View.GONE
            updateBookInfo()
        }

        fun showMask() {
            bookMaskView.visibility = View.VISIBLE
        }

        fun hideMask() {
            bookMaskView.visibility = View.INVISIBLE
        }

        @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
        private fun updateBookInfo() {
            if (chainBook == null) {
                coverView.apply {
                    setImageDrawable(
                        resources.getDrawable(
                            R.drawable.book_cover_default,
                            null
                        )
                    )
                }
                titleView.text = ""
                subTitleLeftView.text = "阅读进度"
                subTitleRightView.text = ""
            } else {
                if (chainBook!!.cover.isNullOrEmpty()) {
                    coverView.apply {
                        setImageDrawable(
                            resources.getDrawable(
                                R.drawable.book_cover_default,
                                null
                            )
                        )
                    }
                } else {
                    coverView.setImageURI(Uri.parse(chainBook!!.cover))
                }

                titleView.text = chainBook!!.name
                subTitleLeftView.text = "阅读进度"
                subTitleRightView.text = String.format("%.2f", chainBook!!.process * 100) + "%"
            }
        }

        fun startWork(): Boolean =
            if (isWorking) false
            else {
                isWorking = true
                subTitleBox.visibility = View.GONE
                workProgressView.visibility = View.VISIBLE

                true
            }


        fun updateWorkProgress(progress: Int) {
            workProgressView.progress = progress
        }

        fun stopWork() {
            isWorking = false

            workProgressView.visibility = View.GONE

            workProgressView.progress = 0

            updateBookInfo()

            subTitleBox.visibility = View.VISIBLE
        }

        fun stopWork(errMsg: String) {
            isWorking = false

            workProgressView.visibility = View.GONE

            workProgressView.progress = 0

            subTitleLeftView.text = ""
            subTitleRightView.text = errMsg

            subTitleBox.visibility = View.VISIBLE
        }
    }
}



package com.itgo.book_cloud.ui.components

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.itgo.book_cloud.R
import com.itgo.book_cloud.data.entity.Chapter

class ChapterItemAdapter(
    context: Context,
    private val chapters: List<Chapter>,
    private val onSelectChapter:  (view: View, holder: ChapterListViewHolder, chapter: Chapter) -> Unit
) : RecyclerView.Adapter<ChapterItemAdapter.ChapterListViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChapterListViewHolder {
        val view: View = inflater.inflate(R.layout.item_chpater_title, parent, false)

        return ChapterListViewHolder(view, onSelectChapter)
    }

    override fun onBindViewHolder(holder: ChapterListViewHolder, position: Int) {
        holder.bind(chapters[position])
    }

    override fun getItemCount(): Int {
        return chapters.size
    }

    class ChapterListViewHolder(
        itemView: View,
        private val onSelectChapter: (view: View, holder: ChapterListViewHolder, chapter: Chapter) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val chapterTitleView: TextView = itemView.findViewById(R.id.chapterTitle)

        fun bind(chapter: Chapter) {
            chapterTitleView.setTextColor(Color.BLACK)
            chapterTitleView.text = chapter.title
            itemView.setOnClickListener {
                onSelectChapter(itemView, this, chapter)
            }
        }
    }
}
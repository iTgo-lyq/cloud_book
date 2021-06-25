package com.itgo.book_cloud.ui.reader.epub

import com.itgo.book_cloud.data.entity.Chapter
import com.itgo.book_cloud.data.entity.LocalBook
import com.itgo.book_cloud.data.entity.MediaSource

class CompatBookInfo(
    val localBook: LocalBook,
    val chapters: List<Chapter>,
    val mediaSourceMap: HashMap<String, MediaSource>
) {
    var isChaptersReversed = false

    init {
    }

    fun getSortedChapters() =
        if (!isChaptersReversed) chapters
        else chapters.reversed()

    fun getCurrentChapterProgress(): Double {
        val progress = localBook.process
        val totalSize = localBook.size
        var preStepSize = -1L
        var stepSize = 0L
        chapters.forEach {
            stepSize += it.size
            if (progress >= preStepSize / totalSize && progress < stepSize / totalSize) {
                return (progress - preStepSize / totalSize) / (stepSize / totalSize - preStepSize / totalSize)
            }
            preStepSize = stepSize
        }
        return 1.0
    }

    fun getCurrentChapter(): Chapter {
        val progress = localBook.process
        val totalSize = localBook.size
        var preStepSize = -1.0
        var stepSize = 0.0
        chapters.forEachIndexed { idx, it ->
            stepSize += it.size
            if (progress >= preStepSize / totalSize && progress < stepSize / totalSize) {
                return it
            }
            preStepSize = stepSize
        }
        return chapters.last()
    }

    fun reverseChapterList() {
        isChaptersReversed = !isChaptersReversed
    }

    fun setProgress2NextChapter(): Boolean {
        return true
    }

    fun setProgress2PreChapter(): Boolean {
        return true
    }

    fun findPreChapter(chapter: Chapter): Chapter? {
        chapters.forEach {
            if (chapter.cid == it.cid)
                return it
        }
        return null
    }
}
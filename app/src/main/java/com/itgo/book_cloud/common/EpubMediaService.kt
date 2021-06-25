package com.itgo.book_cloud.common

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.itgo.book_cloud.common.Constant.BookMedia_Schema
import com.itgo.book_cloud.data.entity.Chapter
import nl.siegmann.epublib.browsersupport.Navigator
import nl.siegmann.epublib.domain.TOCReference
import nl.siegmann.epublib.epub.EpubReader
import java.io.InputStream
import kotlin.concurrent.thread

class EpubMediaService : Service() {

    private val mBinder = EpubMediaBinder()

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    class EpubMediaBinder : Binder() {

        fun readAllMediaResource(input: InputStream, handle: OnSaveMediaHandle) {
            thread {
                val chapters = ArrayList<Chapter>()
                val resource2ChapterMap = HashMap<String, Int>()

                try {
                    val epubReader = EpubReader()
                    val book = epubReader.readEpub(input)

                    for ((_, it) in book.resources.resourceMap) {
                        var bs = it.data
                        if (it.mediaType.defaultExtension == ".xhtml")
                            bs = transLinkToLocal(bs)
                        handle.onSaveMedia(bs, it.id, it.href, it.mediaType.defaultExtension)
                        it.close()
                    }

                    val mNavigator = Navigator()

                    mNavigator.addNavigationEventListener {
                        if (it.currentSpinePos != -1) {
                            val c = Chapter.create()
                            c.msHref = it.currentResource.href
                            c.size = it.currentResource.data.size.toLong()
                            chapters.add(c)
                            resource2ChapterMap[it.currentResource.id] = chapters.size - 1
                        }
                    }

                    mNavigator.gotoBook(book, this) // book is from your loaded InputStream book
                    while (mNavigator.hasNextSpineSection()) {
                        mNavigator.gotoNextSpineSection(this)
                    }

                    book.tableOfContents.tocReferences.forEach {
                        deepMapChapters(it, 0, chapters, resource2ChapterMap)
                    }

                    chapters.forEachIndexed { idx, chapter ->
                        chapter.order = idx
                    }

                    supplementTitle(chapters)

                    var author = ""
                    book.metadata.authors.forEach { a -> author += a.firstname + a.lastname }

                    handle.onComplete(chapters, author)

                } catch (err: Throwable) {
                    handle.onFailure(err)
                }
            }
        }

        private fun transLinkToLocal(bs: ByteArray): ByteArray {
            return Regex("xlink:href=\"(\\S*)\"|src=\"(\\S*)\"").replace(bs.toString(Charsets.UTF_8)) {
                val gvs = it.groupValues.filter(String::isNotEmpty)
                it.value.replace(gvs[1], "$BookMedia_Schema://${gvs[1]}")
            }.toByteArray(Charsets.UTF_8)
        }

        private fun deepMapChapters(
            tocReference: TOCReference,
            level: Int,
            chapters: ArrayList<Chapter>,
            resource2ChapterMap: HashMap<String, Int>
        ) {
            val chapter = chapters[resource2ChapterMap[tocReference.resourceId]!!]
            chapter.level = level
            chapter.title = tocReference.title

            tocReference.children.forEach {
                deepMapChapters(it, level + 1, chapters, resource2ChapterMap)
            }
        }

        private fun supplementTitle(chapters: ArrayList<Chapter>) {
            var currentTitle = "无题"
            var currentIdx = 0

            chapters.forEach {
                if (it.title == "") {
                    currentIdx += 1
                    it.title = currentTitle + currentIdx
                } else {
                    currentTitle = it.title
                    currentIdx = 1
                }
            }
        }
    }


    interface OnSaveMediaHandle {
        fun onSaveMedia(byteArray: ByteArray, name: String, href: String, ext: String)

        fun onComplete(chapters: ArrayList<Chapter>, author: String)

        fun onFailure(err: Throwable)
    }
}
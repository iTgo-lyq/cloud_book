package com.itgo.book_cloud.ui.reader.epub

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_SETTLING
import com.itgo.book_cloud.R
import com.itgo.book_cloud.common.Constant.Num_Reader_Pages_Idx_Init
import com.itgo.book_cloud.common.Constant.Num_Reader_Pages_Total
import com.itgo.book_cloud.common.DoubleCircularLinkedMap
import com.itgo.book_cloud.common.mto
import com.itgo.book_cloud.data.entity.Chapter

/**
 *  控制渲染视图循环，向对应的视图传入对应的参数
 *  父级需要需要手动同步 viewPager 的 currentItemIdx
 *  父级需要依靠属性 isScrolling 阻止滑动被打断，避免混淆的逻辑
 */
class ViewPagerControl(fragmentActivity: FragmentActivity, private val bookInfo: CompatBookInfo) {
    var isScrolling = false

    @SuppressLint("InflateParams")
    private val rendererList = DoubleCircularLinkedMap.from<EpubRendererFragment, View>(
        mutableListOf(
            EpubRendererFragment(
                null, Num_Reader_Pages_Idx_Init, bookInfo
            ) mto fragmentActivity.layoutInflater.inflate(
                R.layout.fragment_epub_renderer, null, false
            ),
            EpubRendererFragment(
                null, Num_Reader_Pages_Idx_Init, bookInfo
            ) mto fragmentActivity.layoutInflater.inflate(
                R.layout.fragment_epub_renderer, null, false
            ),
            EpubRendererFragment(
                null, Num_Reader_Pages_Idx_Init, bookInfo
            ) mto fragmentActivity.layoutInflater.inflate(
                R.layout.fragment_epub_renderer, null, false
            ),
        )
    )

    val pageAdapter: FragmentStateAdapter
    val pageChangeListener: ViewPager2.OnPageChangeCallback

    var currentItemIdx = Num_Reader_Pages_Idx_Init

    init {
        pageChangeListener = PageChangeListener()
        pageAdapter = ReaderPagerAdapter(fragmentActivity)
    }

    fun gotoPreChapter(): Boolean {
        return if (bookInfo.setProgress2PreChapter()) {
            onRendererPageSelected(currentItemIdx)
            true
        } else {
            false
        }
    }

    fun gotoNextChapter(): Boolean {
        return if (bookInfo.setProgress2NextChapter()) {
            onRendererPageSelected(currentItemIdx)
            true
        } else {
            false
        }
    }

    fun gotoChapter(chapter: Chapter) {
        rendererList.getPreItem().first.apply {
            loadWithChapter(chapter, -1)
        }
        rendererList.getCurrentItem().first.apply {
            loadWithChapter(chapter, 0)
        }
        rendererList.getNextItem().first.apply {
            loadWithChapter(chapter, 1)
        }
    }

    private fun onCreateRendererPage(position: Int): Fragment {
        val renderer = when (position) {
            currentItemIdx -> {
                rendererList.getCurrentItem()
            }
            currentItemIdx - 1 -> {
                rendererList.getPreItem()
            }
            currentItemIdx + 1 -> {
                rendererList.getNextItem()
            }
            currentItemIdx - 2 -> {
                rendererList.getNextItem()
            }
            currentItemIdx + 2 -> {
                rendererList.getPreItem()
            }
            else -> throw Exception("RendererPage 序列错误")
        }
        Log.d("console-control", "(currentItemIdx=$currentItemIdx position=$position)")

        val newRenderer = EpubRendererFragment(renderer, position, bookInfo)

        when (position) {
            currentItemIdx -> {
                rendererList.setCurrentItemFirst(newRenderer)
            }
            currentItemIdx - 1 -> {
                rendererList.setPreItemFirst(newRenderer)
            }
            currentItemIdx + 1 -> {
                rendererList.setNextItemFirst(newRenderer)
            }
            currentItemIdx - 2 -> {
                rendererList.setNextItemFirst(newRenderer)
            }
            currentItemIdx + 2 -> {
                rendererList.setPreItemFirst(newRenderer)
            }
        }
        return newRenderer
    }

    private fun onRendererPageSelected(position: Int) {
        when (position) {
            currentItemIdx -> {
                rendererList.getCurrentItem()
            }
            currentItemIdx - 1 -> {
                rendererList.move2PreItem()
            }
            currentItemIdx + 1 -> {
                rendererList.move2NextItem()
            }
            else -> throw Exception("RendererPage 序列错误")
        }

        currentItemIdx = position
    }

    private inner class ReaderPagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount() = Num_Reader_Pages_Total

        override fun createFragment(position: Int) = onCreateRendererPage(position)
    }

    private inner class PageChangeListener : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            onRendererPageSelected(position)
        }

        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            isScrolling = state == SCROLL_STATE_SETTLING
        }
    }
}
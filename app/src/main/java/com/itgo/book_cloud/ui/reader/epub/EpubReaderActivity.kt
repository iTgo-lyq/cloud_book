package com.itgo.book_cloud.ui.reader.epub

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat.getWindowInsetsController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.itgo.book_cloud.BookCloudApplication
import com.itgo.book_cloud.R
import com.itgo.book_cloud.common.Constant.Application_Schema
import com.itgo.book_cloud.common.alert
import com.itgo.book_cloud.common.createApplicationClipString
import com.itgo.book_cloud.common.notchlib.NotchScreenManager
import com.itgo.book_cloud.data.AppDatabase
import com.itgo.book_cloud.data.entity.Chapter
import com.itgo.book_cloud.ui.components.ChapterItemAdapter
import kotlinx.android.synthetic.main.activity_reader_epub.*
import kotlin.math.abs


class EpubReaderActivity : AppCompatActivity(),
    BottomNavigationView.OnNavigationItemSelectedListener {
    private val notchScreenManager = NotchScreenManager.getInstance()

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val viewPagerControl by lazy { ViewPagerControl(this, bookInfo) }
    private val insetsControllerCompat by lazy { getWindowInsetsController(readerContainer) }
    private val clipboardManager by lazy { getSystemService(CLIPBOARD_SERVICE) as ClipboardManager }

    private var isShowUISetting = false
    private var isDrawerOpened = false
    private var isFullScreen = false
    private var hasNotch = UNKNOWN_NOTCH
    private var centerClickDownPosition = Array(2) { 0f }

    private lateinit var bookInfo: CompatBookInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cbid = intent.extras?.getLong(PARAMS_CBID) ?: return finish()

        val localBookDao = database.localBookDao()
        val chapterDao = database.chapterDap()
        val mediaSourceDao = database.mediaSourceDao()

        val localBook = localBookDao.findOneBookByCBId(cbid)!!
        val chapters = chapterDao.findAllByCBId(cbid)
        val mediaSourceMap = mediaSourceDao.findAllMediaSourceByCBIdToMapWithHref(cbid)

        bookInfo = CompatBookInfo(localBook, chapters, mediaSourceMap)

        setContentView(R.layout.activity_reader_epub)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun onMounted() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        viewPager.adapter = viewPagerControl.pageAdapter
        viewPager.registerOnPageChangeCallback(viewPagerControl.pageChangeListener)
        viewPager.setCurrentItem(viewPagerControl.currentItemIdx, false)

        readerContainer.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {
                isDrawerOpened = true
            }

            override fun onDrawerClosed(drawerView: View) {
                enterFullscreen()
                isDrawerOpened = false
            }

            override fun onDrawerStateChanged(newState: Int) {}
        })

        chapterListView.apply {
            layoutManager = LinearLayoutManager(this@EpubReaderActivity)

            adapter = ChapterItemAdapter(
                this@EpubReaderActivity,
                bookInfo.chapters,
                this@EpubReaderActivity::onSelectChapter
            )
        }

        sortBtn.setOnClickListener {
            bookInfo.reverseChapterList()

            chapterListView.adapter =
                ChapterItemAdapter(this, bookInfo.getSortedChapters(), this::onSelectChapter)
        }

        bottomNavigation.setOnNavigationItemSelectedListener(this)

        leftClickMask.setOnClickListener {
            if (!viewPagerControl.isScrolling && isFullScreen) viewPager.setCurrentItem(
                viewPagerControl.currentItemIdx - 1,
                true
            )
        }

        centerClickMask.setOnTouchListener { _: View, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_UP && !isDrawerOpened ) {
                if (abs(centerClickDownPosition[0] - event.x) < 50 &&
                    abs(centerClickDownPosition[1] - event.y) < 50
                ) {
                    if (isFullScreen) {
                        quitFullscreen()
                    } else {
                        enterFullscreen()
                    }
                }
                centerClickDownPosition[0] = -1000f
                centerClickDownPosition[1] = -1000f
            } else if (event.action == MotionEvent.ACTION_DOWN) {
                centerClickDownPosition[0] = event.x
                centerClickDownPosition[1] = event.y
            }
            return@setOnTouchListener true
        }

        rightClickMask.setOnClickListener {
            if (!viewPagerControl.isScrolling && isFullScreen) viewPager.setCurrentItem(
                viewPagerControl.currentItemIdx + 1,
                true
            )
        }

        enterFullscreen()
    }

    private fun onSelectChapter(
        view: View,
        holder: ChapterItemAdapter.ChapterListViewHolder,
        chapter: Chapter
    ) {
        viewPagerControl.gotoChapter(chapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.nav_share -> {
                val uid =
                    (application as BookCloudApplication).globalStore.userInfo.value?.uid ?: ""
                val uri = Uri.parse("$Application_Schema://share/$uid/${bookInfo.localBook.cbid}")
                val text = createApplicationClipString(uri, "悄悄收下 ${bookInfo.localBook.name} 哦～")
                val clipData = ClipData.newPlainText("分享", text)
                clipboardManager.setPrimaryClip(clipData)
                alert("已复制到剪切板，分享给有缘人吧～")
            }
            R.id.nav_report -> {
                alert("已收到你的反馈！")
            }
        }

        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_chapter -> {
                readerContainer.openDrawer(GravityCompat.START)
            }
            R.id.nav_set_night -> updateTheme()
            R.id.nav_setting -> switchShowUISetting()
        }

        return false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.reader_top_nav, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun setContentView(layoutResID: Int) {
        when (hasNotch) {
            UNKNOWN_NOTCH -> {
                notchScreenManager.getNotchInfo(this) {
                    hasNotch = if (it.hasNotch) HAS_NOTCH else NO_NOTCH
                    setContentView(layoutResID)
                }
                return
            }
            NO_NOTCH -> {
                theme.applyStyle(R.style.noNotch, true)
            }
            HAS_NOTCH -> {
                theme.applyStyle(R.style.hasNotch, true)
            }
        }
        super.setContentView(layoutResID)
        onMounted()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (isFullScreen && !viewPagerControl.isScrolling && !isDrawerOpened) viewPager.dispatchTouchEvent(ev)
        if (isDrawerOpened) drawerBox.dispatchTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    @SuppressLint("WrongConstant")
    private fun enterFullscreen() {
        isFullScreen = true

        notchScreenManager.setDisplayInNotch(this)

        insetsControllerCompat?.systemBarsBehavior =
            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsControllerCompat?.hide(WindowInsets.Type.systemBars())

        val topBarOutAnim = AnimationUtils.loadAnimation(
            this,
            R.anim.translate_y_out_reader_bar_top
        )
        val bottomBarOutAnim = AnimationUtils.loadAnimation(
            this,
            R.anim.translate_y_out_reader_bar_bottom
        )

        toolbarBox.startAnimation(topBarOutAnim)
        navAndSheetBox.startAnimation(bottomBarOutAnim)
    }

    private fun quitFullscreen() {
        insetsControllerCompat?.show(WindowInsets.Type.systemBars())

        val topBarInAnim = AnimationUtils.loadAnimation(this, R.anim.translate_y_in_reader_bar_top)

        val bottomBarInAnim = AnimationUtils.loadAnimation(
            this, R.anim.translate_y_in_reader_bar_bottom
        )

        toolbarBox.startAnimation(topBarInAnim)
        navAndSheetBox.startAnimation(bottomBarInAnim)

        uiSetting.visibility = View.GONE
        isShowUISetting = false

        isFullScreen = false
    }

    private fun updateTheme() {

    }

    private fun switchShowUISetting() {
        if (isShowUISetting) {
            uiSetting.visibility = View.GONE
        } else {
            uiSetting.visibility = View.VISIBLE
        }

        isShowUISetting = !isShowUISetting
    }

    companion object {
        const val UNKNOWN_NOTCH = 0
        const val NO_NOTCH = 1
        const val HAS_NOTCH = 2

        const val PARAMS_CBID = "cbid"
    }
}

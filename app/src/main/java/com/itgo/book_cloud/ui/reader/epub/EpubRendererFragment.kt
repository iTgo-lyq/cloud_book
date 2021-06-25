package com.itgo.book_cloud.ui.reader.epub

import android.annotation.SuppressLint
import android.os.BatteryManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.get
import androidx.fragment.app.Fragment
import com.itgo.book_cloud.R
import com.itgo.book_cloud.common.Constant.BookMedia_Schema
import com.itgo.book_cloud.common.MutablePair
import com.itgo.book_cloud.common.setInterval
import com.itgo.book_cloud.data.entity.Chapter
import com.itgo.book_cloud.ui.components.BatteryView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class EpubRendererFragment(
    preRenderer: MutablePair<EpubRendererFragment, View>?,
    private val position: Int,
    private val bookInfo: CompatBookInfo,
) : Fragment() {

    private val prePosition = preRenderer?.first?.position
    private val mView = preRenderer?.second
    private val loadingView by lazy { view?.findViewById(R.id.loading) as ProgressBar }
    private val webview by lazy { view?.findViewById(R.id.webview) as WebView }
    private val battery by lazy { view?.findViewById(R.id.battery) as BatteryView }
    private val time by lazy { view?.findViewById(R.id.time) as TextView }
    private val chapterTitle by lazy { view?.findViewById(R.id.chapterTitle) as TextView }
    private val chapterProcess by lazy { view?.findViewById(R.id.chapterProcess) as TextView }

    private var status = STATUS_RUNNING
    private lateinit var autoUpdateSystemStatusUITask: Timer

    private val sharedRunningInfo: SharedRunningInfo

    init {
        Log.d("debug", "$position ${preRenderer?.first?.position}")
        preRenderer?.first?.status = STATUS_BLOCK
        sharedRunningInfo =
            preRenderer?.first?.sharedRunningInfo?.bindRenderer(this) ?: SharedRunningInfo(this)
    }

    override fun onCreateView(
        layoutInflater: LayoutInflater,
        group: ViewGroup?,
        _1: Bundle?
    ) =
        (layoutInflater.inflate(
            R.layout.fragment_reader_container, group, false
        ) as FrameLayout).apply {
            (mView?.parent as ViewGroup?)?.removeView(mView)
            addView(mView)
        }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inheritPreRenderer()
    }

    override fun onResume() {
        super.onResume()
        autoUpdateSystemStatusUITask = autoUpdateSystemStatusUI()
    }

    override fun onPause() {
        super.onPause()
        autoUpdateSystemStatusUITask.cancel()
    }

    /**
     * 直接切换章节，设置页数偏移
     */
    fun loadWithChapter(chapter: Chapter, offset: Int) {
        sharedRunningInfo.loading = true

        sharedRunningInfo.chapter = chapter
        sharedRunningInfo.pager = null
        sharedRunningInfo.title = chapter.title

        when (offset) {
            -1 -> {
                val preChapter = bookInfo.findPreChapter(chapter)
                if (preChapter != null) {
                    loadWithChapter(preChapter, 1.0)
                } else {
                    webview.visibility = View.INVISIBLE
                    sharedRunningInfo.title = null
                    sharedRunningInfo.pager = null
                    sharedRunningInfo.chapter = null
                    sharedRunningInfo.chapterProgress = 0.0
                }
            }
            0 -> {
                sharedRunningInfo.title = chapter.title
                webview.loadUrl("$BookMedia_Schema://${chapter.msHref}")
            }

            1 -> {
                webview.loadUrl("$BookMedia_Schema://${chapter.msHref}")
                postTurnAction(1)
            }
        }
    }

    /**
     * 直接切换章节，设置进度偏移
     */
    private fun loadWithChapter(chapter: Chapter, offset: Double) {
        sharedRunningInfo.loading = true

        sharedRunningInfo.chapter = chapter
        sharedRunningInfo.pager = null
        sharedRunningInfo.title = chapter.title
        sharedRunningInfo.chapterProgress = offset

        webview.loadUrl("$BookMedia_Schema://${chapter.msHref}")
    }

    private fun postTurnAction(pageNum: Int) {
        sharedRunningInfo.turnCache += pageNum
        Log.d(
            "console-book $position",
            "[postTurnAction] cachePage: ${sharedRunningInfo.turnCache}"
        )
    }

    private fun clearTurnAction() {
        sharedRunningInfo.turnCache = 0
    }

    private fun clearTurnAction(pageNum: Int) {
        sharedRunningInfo.turnCache -= pageNum
    }

    private fun doTurnAction() {
        if (!sharedRunningInfo.webviewInitialized || sharedRunningInfo.turnCache == 0) return

        sharedRunningInfo.loading = true

        webview.post {
            val nextPage = sharedRunningInfo.pager!!.currentPage + sharedRunningInfo.turnCache
            if (nextPage < sharedRunningInfo.pager!!.maxPageNum && nextPage > -1) {
                Log.d("debug", "$position $nextPage")
                webview.evaluateJavascript("gotoPageByIdx($nextPage)", null)
                clearTurnAction()
            } else {
                if (nextPage < -1) {

                } else {

                }
            }
        }
    }

    fun setRenderOptions(renderOptions: RenderOptions) {

    }

    /**
     * 若第一次初始化，直接根据 总进度 确定章节和页位置
     * 其他时候调用，则是在翻页
     */
    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    private fun inheritPreRenderer() {
        if (prePosition == null) return

        postTurnAction(position - prePosition)

        if (!sharedRunningInfo.webviewInitialized) {
            WebView.setWebContentsDebuggingEnabled(true)
            webview.setDrawingCacheEnabled(true)
            webview.settings.domStorageEnabled = true
            webview.settings.javaScriptEnabled = true
            webview.settings.loadsImagesAutomatically = true
            webview.settings.javaScriptCanOpenWindowsAutomatically = true
            webview.addJavascriptInterface(this, "Epub")
            webview.webChromeClient = EpubWebChromeClient()
            webview.webViewClient = EpubWebViewClient(bookInfo)
            webview.setOnTouchListener { _, _ -> true }
            sharedRunningInfo.webviewInitialized = true

            loadWithChapter(bookInfo.getCurrentChapter(), bookInfo.getCurrentChapterProgress())

            Log.d("console-book $position", "[create]")
        } else {
            webview.removeJavascriptInterface("Epub")
            webview.addJavascriptInterface(this, "Epub")
            Log.d("console-book $position", "[reuse] from $prePosition")
            doTurnAction()
        }
    }

    @JavascriptInterface // 加载章节结束 -> 跳转页面
    fun onLoad() {
        Log.d("console-book ${sharedRunningInfo.renderer.position}", "[onLoad] $sharedRunningInfo")

        webview.post {
            webview.evaluateJavascript(
                "gotoPageByProgress(${sharedRunningInfo.chapterProgress})",
                null
            )
        }
    }

    @JavascriptInterface // 开始跳转页面、设置mask结束
    fun onReadyStatusChange(ok: Boolean) {
        Log.d("console-book ${sharedRunningInfo.renderer.position}", "[onReadyStatusChange] $ok")

        sharedRunningInfo.loading = !ok
    }

    @JavascriptInterface // 跳转页面，导致pager信息变化
    fun onPagerChange(maxPageNum: Int, currentPage: Int) {
        Log.d(
            "console-book ${sharedRunningInfo.renderer.position}",
            "[onPagerChange] maxPageNum=$maxPageNum currentPage=$currentPage cachePage=${sharedRunningInfo.turnCache}"
        )

        sharedRunningInfo.pager = Pager(maxPageNum, currentPage)
    }

    @JavascriptInterface // 已跳转页面，等待设置mask
    fun onPageViewChange(viewportHeight: Int, defaultReservedSpace: Int) {
        webview.post {
            webview.buildDrawingCache()
            val bitmap = webview.getDrawingCache()

            Log.d(
                "console-book $position",
                "[onPageViewChange] (chromium， h=$viewportHeight, space=$defaultReservedSpace) (screenShoot, w=${bitmap.width} h=${bitmap.height})"
            )

            val scale = bitmap.height.toDouble() / viewportHeight
            val reservedSpace = (defaultReservedSpace * scale).roundToInt()
            var top = 0
            var bottom = 0

            loopX@ for (x in 0 until reservedSpace) {
                for (y in 0 until bitmap.width)
                    if (bitmap[y, x] != -1) continue@loopX
                top = x
                break
            }

            loopX@ for (x in bitmap.height - reservedSpace until bitmap.height) {
                for (y in 0 until bitmap.width)
                    if (bitmap[y, x] != -1) continue@loopX
                bottom = bitmap.height - x
                break
            }

            Log.d(
                "console-book $position",
                "[computeMaskPanel] (scale=${scale} hideTop=$top hideBottom=$bottom)"
            )

            webview.evaluateJavascript(
                "setMask($scale, $top, $bottom)",
                null
            )
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun autoUpdateSystemStatusUI() =
        setInterval(this.activity, 1000, 0) {
            time.text = SimpleDateFormat("HH:mm").format(Date())
            val manager =
                this.activity?.getSystemService(AppCompatActivity.BATTERY_SERVICE) as BatteryManager
            val currentLevel = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            battery.power = currentLevel
        }

    companion object {
        var renderOptions = RenderOptions(14, "", 0, 1, "")

        const val STATUS_RUNNING = "running"
        const val STATUS_BLOCK = "block"
    }

    data class RenderOptions(
        val fontSize: Int,
        val fontFamily: String,
        val letterSpacing: Int,
        val lineHeight: Int,
        val fontColor: String
    )

    data class Pager(val maxPageNum: Int, val currentPage: Int)

    class SharedRunningInfo(var renderer: EpubRendererFragment) {
        var title: String? = null
            set(value) {
                field = value
                renderer.activity?.runOnUiThread {
                    renderer.chapterTitle.text = value
                }
            }

        var pager: Pager? = null
            set(value) {
                field = value
                if (value != null) renderer.doTurnAction()
                renderer.activity?.runOnUiThread {
                    renderer.chapterProcess.text =
                        if (value != null) "${value.currentPage + 1}/${value.maxPageNum}" else ""
                }
            }
        var loading = true
            set(value) {
                field = value
                renderer.activity?.runOnUiThread {
                    renderer.webview.visibility = if (value) View.INVISIBLE else View.VISIBLE
                    renderer.loadingView.visibility = if (value) View.VISIBLE else View.INVISIBLE
                }
            }

        var turnCache = 0
        var chapter: Chapter? = null
        var chapterProgress = 0.0
        var webviewInitialized = false

        fun bindRenderer(renderer: EpubRendererFragment): SharedRunningInfo {
            this.renderer = renderer
            return this
        }

        override fun toString(): String {
            return "(title=${title} pager=${pager} chapter=${chapter?.cid} chapterProgress=${chapterProgress} )"
        }
    }
}


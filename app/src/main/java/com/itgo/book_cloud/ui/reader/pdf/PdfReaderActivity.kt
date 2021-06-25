package com.itgo.book_cloud.ui.reader.pdf

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE
import com.itgo.book_cloud.BookCloudApplication
import com.itgo.book_cloud.R
import com.itgo.book_cloud.common.*
import com.itgo.book_cloud.common.notchlib.NotchScreenManager
import com.itgo.book_cloud.data.AppDatabase
import com.itgo.book_cloud.data.entity.LocalBook
import com.itgo.book_cloud.http.ServiceFactory
import com.itgo.book_cloud.http.service.AdResult
import com.itgo.book_cloud.http.service.RecommendService
import kotlinx.android.synthetic.main.activity_reader_pdf.*
import kotlinx.android.synthetic.main.activity_reader_pdf.centerClickMask
import kotlinx.android.synthetic.main.activity_reader_pdf.leftClickMask
import kotlinx.android.synthetic.main.activity_reader_pdf.readerContainer
import kotlinx.android.synthetic.main.activity_reader_pdf.rightClickMask
import kotlinx.android.synthetic.main.activity_reader_pdf.toolbar
import kotlinx.android.synthetic.main.activity_reader_pdf.toolbarBox
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.math.abs

class PdfReaderActivity : AppCompatActivity() {
    private val recommendService by lazy { ServiceFactory(this).create(RecommendService::class.java) }
    private val notchScreenManager = NotchScreenManager.getInstance()
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val viewPagerControl by lazy {
        ViewPagerControl(
            this,
            getExternalFile(bookInfo.localUri)
        )
    }
    private val insetsControllerCompat by lazy {
        ViewCompat.getWindowInsetsController(
            readerContainer
        )
    }
    private val clipboardManager by lazy { getSystemService(CLIPBOARD_SERVICE) as ClipboardManager }

    private var isFullScreen = false
    private var hasNotch = UNKNOWN_NOTCH
    private var centerClickDownPosition = Array(2) { 0f }

    private lateinit var bookInfo: LocalBook

    private var adTimer: Timer? = null
    private var counterTimer: Timer? = null

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cbid = intent.extras?.getLong(PARAMS_CBID) ?: return finish()
        val localBookDao = database.localBookDao()
        bookInfo = localBookDao.findOneBookByCBId(cbid)!!

        setContentView(R.layout.activity_reader_pdf)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun onMounted() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        viewport.adapter = viewPagerControl
        viewport.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                seekBar.progress = position
            }
        })
        viewport.setCurrentItem(computeCurrentPage(bookInfo), false)

        seekBar.max = bookInfo.size.toInt()

        leftClickMask.setOnClickListener {
            if (viewport.scrollState == SCROLL_STATE_IDLE && isFullScreen) viewport.setCurrentItem(
                viewport.currentItem - 1,
                true
            )
        }

        prePageBtn.setOnClickListener {
            if (viewport.scrollState == SCROLL_STATE_IDLE && !isFullScreen) viewport.setCurrentItem(
                viewport.currentItem - 1,
                true
            )
        }

        centerClickMask.setOnTouchListener { _: View, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_UP) {
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
            if (viewport.scrollState == SCROLL_STATE_IDLE && isFullScreen) viewport.setCurrentItem(
                viewport.currentItem + 1,
                true
            )
        }

        nextPageBtn.setOnClickListener {
            if (viewport.scrollState == SCROLL_STATE_IDLE && !isFullScreen) viewport.setCurrentItem(
                viewport.currentItem + 1,
                true
            )
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewport.setCurrentItem(
                        progress,
                        false
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        enterFullscreen()
    }

    override fun onResume() {
        super.onResume()

        adTimer = setTimeout(5000) {
            (application as BookCloudApplication).globalStore.userInfo.value?.uid?.let {
                recommendService.fetchAd().enqueue(object : Callback<AdResult> {
                    @SuppressLint("SetTextI18n")
                    override fun onResponse(call: Call<AdResult>, response: Response<AdResult>) {
                        val ad = response.body()!!
                        runOnUiThread {
                            var counter = 5
                            val adBuilder = AlertDialog.Builder(this@PdfReaderActivity)
                            val view = View.inflate(application, R.layout.dialog_ad, null)
                            adBuilder.setView(view)
                            adBuilder.setCancelable(false)
                            val adDialog = adBuilder.create()
                            val timer = view.findViewById<TextView>(R.id.timer)
                            val adCard = view.findViewById<CardView>(R.id.adCard)
                            val adImage = view.findViewById<ImageView>(R.id.adImage)
                            val button = view.findViewById<Button>(R.id.button)
                            timer.text = "反馈 $counter s"
                            adImage.setImageURI(ad.picture, R.drawable.book_cover_default)
                            button.text = ad.title
                            counterTimer = setInterval(this@PdfReaderActivity, 1000) {
                                if (counter == 0) {
                                    adDialog.hide()
                                    counterTimer?.cancel()
                                } else {
                                    counter--
                                    timer.text = "反馈 $counter s"
                                }
                            }
                            adCard.setOnClickListener {
                                runOnUiThread {
                                    alert("即将打开落地页：${ad.url}")
                                    adDialog.hide()
                                    counterTimer?.cancel()
                                }
                            }
                            adDialog.show()
                        }
                    }

                    override fun onFailure(call: Call<AdResult>, t: Throwable) {
                        Log.d("debug", t.message.toString())
                    }
                })
            }
        }
    }

    override fun onPause() {
        super.onPause()

        adTimer?.cancel()
        counterTimer?.cancel()
    }

    private fun computeCurrentPage(bookInfo: LocalBook): Int {
        return (bookInfo.process * bookInfo.size).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewPagerControl.destroy()
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
        progressBar.startAnimation(bottomBarOutAnim)
    }

    private fun quitFullscreen() {
        insetsControllerCompat?.show(WindowInsets.Type.systemBars())

        val topBarInAnim = AnimationUtils.loadAnimation(this, R.anim.translate_y_in_reader_bar_top)

        val bottomBarInAnim = AnimationUtils.loadAnimation(
            this, R.anim.translate_y_in_reader_bar_bottom
        )

        toolbarBox.startAnimation(topBarInAnim)
        progressBar.startAnimation(bottomBarInAnim)

        isFullScreen = false
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (isFullScreen) viewport.dispatchTouchEvent(ev)

        return super.dispatchTouchEvent(ev)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.reader_top_nav, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.nav_share -> {
                val uid =
                    (application as BookCloudApplication).globalStore.userInfo.value?.uid ?: ""
                val uri = Uri.parse("${Constant.Application_Schema}://share/$uid/${bookInfo.cbid}")
                val text = createApplicationClipString(uri, "悄悄收下 ${bookInfo.name} 哦～")
                val clipData = ClipData.newPlainText("分享", text)
                clipboardManager.setPrimaryClip(clipData)
                alert("已复制到剪切板，分享给有缘人吧～")
            }
            R.id.nav_report -> alert("已收到你的反馈！")
        }

        return true
    }


    companion object {
        const val UNKNOWN_NOTCH = 0
        const val NO_NOTCH = 1
        const val HAS_NOTCH = 2

        const val PARAMS_CBID = "cbid"
    }
}
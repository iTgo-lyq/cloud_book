package com.itgo.book_cloud.ui.reader.pdf

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.BatteryManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.itgo.book_cloud.R
import com.itgo.book_cloud.common.setInterval
import kotlinx.android.synthetic.main.fragment_pdf_renderer.*
import java.text.SimpleDateFormat
import java.util.*

class PdfRendererFragment(
    private val pageBitmap: Bitmap,
    private val position: Int,
    private val total: Int
) : Fragment() {
    private lateinit var autoUpdateSystemStatusUITask: Timer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pdf_renderer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        page.setImageBitmap(pageBitmap)
        totalPageNum.text = total.toString()
        currentPageIdx.text = position.toString()
    }

    override fun onResume() {
        super.onResume()
        autoUpdateSystemStatusUITask = autoUpdateSystemStatusUI()
    }

    override fun onPause() {
        super.onPause()
        autoUpdateSystemStatusUITask.cancel()
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
}
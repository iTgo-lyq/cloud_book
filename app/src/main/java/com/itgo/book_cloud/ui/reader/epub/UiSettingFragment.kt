package com.itgo.book_cloud.ui.reader.epub

import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.itgo.book_cloud.R
import kotlinx.android.synthetic.main.fragment_sheet_reader_epub_ui.*


class UiSettingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sheet_reader_epub_ui, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        variability.progress = getSystemLight(this.requireContext()) / 255
    }

    override fun onResume() {
        super.onResume()
        variability.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    setAppLight(progress * 255f / 100)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
    }

    private fun getSystemLight(context: Context): Int {
        val contentResolver = context.contentResolver
        return Settings.System.getInt(
            contentResolver,
            Settings.System.SCREEN_BRIGHTNESS, 255
        ) //系统亮度为0~255
    }

    private fun setAppLight(lightValue: Float) {
        val params: WindowManager.LayoutParams =  requireActivity().window.attributes
        params.screenBrightness = lightValue / 255
        requireActivity().window.attributes = params
    }
}
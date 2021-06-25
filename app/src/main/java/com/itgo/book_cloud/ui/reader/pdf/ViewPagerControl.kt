package com.itgo.book_cloud.ui.reader.pdf

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.io.File


class ViewPagerControl(fragmentActivity: FragmentActivity, file: File) :
    FragmentStateAdapter(fragmentActivity) {
    private val rect = fragmentActivity.windowManager.currentWindowMetrics.bounds
    private val renderer =
        PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY))

    override fun getItemCount() = renderer.pageCount

    override fun createFragment(position: Int): Fragment {
        val page = renderer.openPage(position)
        val ww = rect.width()
        val bitmap = Bitmap.createBitmap(ww, ww * page.height / page.width, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        return PdfRendererFragment(bitmap, position, itemCount)
    }

    fun destroy() = renderer.close()
}
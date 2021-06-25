package com.itgo.book_cloud.ui.home.subview

import android.content.ComponentName
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.itgo.book_cloud.R
import com.itgo.book_cloud.common.*
import com.itgo.book_cloud.common.Constant.getChainBookCacheFolder
import com.itgo.book_cloud.common.Constant.Origin_Ext_Epub
import com.itgo.book_cloud.common.Constant.Origin_Ext_Pdf
import com.itgo.book_cloud.data.AppDatabase
import com.itgo.book_cloud.data.entity.Chapter
import com.itgo.book_cloud.data.entity.LocalBook
import com.itgo.book_cloud.data.entity.MediaSource
import com.itgo.book_cloud.data.model.BookShelfItem
import com.itgo.book_cloud.data.model.ChainBook
import com.itgo.book_cloud.http.ServiceFactory
import com.itgo.book_cloud.http.service.BookService
import com.itgo.book_cloud.ui.components.BookCardAdapter
import com.itgo.book_cloud.ui.reader.epub.EpubReaderActivity
import com.itgo.book_cloud.ui.reader.pdf.PdfReaderActivity
import kotlinx.android.synthetic.main.fragment_book_shelf.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class BookShelfFragment : Fragment() {
    private val bookService by lazy { ServiceFactory(requireContext()).create(BookService::class.java) }

    private var touchBookAlert: AlertDialog? = null
    private var createBooShelfAlert: AlertDialog? = null

    private var books: MutableList<ChainBook> = mutableListOf()

    private val database by lazy { AppDatabase.getDatabase(requireContext()) }

    lateinit var downloadBinder: DownloadService.DownloadBinder

    lateinit var epubMediaBinder: EpubMediaService.EpubMediaBinder

    private val downloadConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            downloadBinder = service as DownloadService.DownloadBinder
        }

        override fun onServiceDisconnected(name: ComponentName?) {}
    }

    private val epubMediaConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            epubMediaBinder = service as EpubMediaService.EpubMediaBinder
        }

        override fun onServiceDisconnected(name: ComponentName?) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().bindService(
            Intent(this.requireActivity(), DownloadService::class.java),
            downloadConnection,
            BIND_AUTO_CREATE
        )

        requireActivity().bindService(
            Intent(this.requireActivity(), EpubMediaService::class.java),
            epubMediaConnection,
            BIND_AUTO_CREATE
        )

        books = mutableListOf()

        bookService.getAllBookShelf().enqueue(object : Callback<List<BookShelfItem>> {
            override fun onResponse(
                call: Call<List<BookShelfItem>>,
                response: Response<List<BookShelfItem>>
            ) {
                loading.visibility = View.VISIBLE
                var counter = 0
                val bookShelf = response.body()
                bookShelf?.forEach { shelfItem ->
                    counter++
                    bookService.getBookByShelf(shelfItem.bsid)
                        .enqueue(object : Callback<List<ChainBook>> {
                            override fun onResponse(
                                call: Call<List<ChainBook>>,
                                response: Response<List<ChainBook>>
                            ) {
                                response.body()?.let {
                                    setRootBookShelf(it)
                                }
                                counter--
                                if (counter == 0) loading.visibility = View.INVISIBLE
                            }

                            override fun onFailure(call: Call<List<ChainBook>>, t: Throwable) {
                                Log.d("debug", t.message.toString())
                            }
                        })
                }
            }

            override fun onFailure(call: Call<List<BookShelfItem>>, t: Throwable) {
                requireContext().alert("获取书架失败！")
                Log.d("debug", t.message.toString())
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_book_shelf, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRootBookShelf(books, true)
    }


    private fun onClickBookCard(
        view: View,
        holder: BookCardAdapter.BookCardLineViewHolder,
        chainBook: ChainBook
    ) {
        val localBookDao = database.localBookDao()

        if (!localBookDao.exitBook(chainBook.id)) {
            when (chainBook.ext) {
                Origin_Ext_Pdf -> downloadPdf(holder, chainBook)
                Origin_Ext_Epub -> downloadEpub(holder, chainBook)
                else -> requireContext().alert("暂时无法查阅")
            }
        } else {
            when (chainBook.ext) {
                Origin_Ext_Pdf -> {
                    val bundle = Bundle()

                    bundle.putLong(PdfReaderActivity.PARAMS_CBID, chainBook.id)

                    Navigation.findNavController(view).navigate(R.id.pdfReaderActivity, bundle)
                }
                Origin_Ext_Epub -> {
                    val bundle = Bundle()

                    bundle.putLong(EpubReaderActivity.PARAMS_CBID, chainBook.id)

                    Navigation.findNavController(view).navigate(R.id.epubReaderActivity, bundle)
                }
                else -> requireContext().alert("暂时无法查阅")
            }
        }
    }


    private fun onSelectBookCard(
        view: View,
        holder: BookCardAdapter.BookCardLineViewHolder,
        chainBook: ChainBook
    ) {

    }

    private fun setRootBookShelf(list: List<ChainBook>, reset: Boolean = false) {
        if (reset) books = mutableListOf()
        else books.addAll(list)

        rootBookshelfView.let {
            it.layoutManager = GridLayoutManager(context, 3)
            it.adapter = BookCardAdapter(
                this.requireContext(),
                books,
                this::onClickBookCard,
                this::onSelectBookCard
            )
        }
    }

    private fun downloadPdf(holder: BookCardAdapter.BookCardLineViewHolder, chainBook: ChainBook) {
        if (!holder.startWork()) return

        requireContext().alert("开始下载！")

        val saveFolderPath = getChainBookCacheFolder(chainBook.id)

        val saveBookFileName = chainBook.name + chainBook.ext

        val output = requireActivity().openFileOutput(saveFolderPath, saveBookFileName)

        val remoteUrl = chainBook.origin.originUrl

        downloadBinder.startDownload(
            remoteUrl,
            output,
            object : DownloadService.OnDownloadListener {
                override fun onDownloadSuccess() {
                    val localBookDao = database.localBookDao()
                    val localUri = saveFolderPath + saveBookFileName

                    localBookDao.insertOneBook(
                        LocalBook.create(
                            chainBook.origin.id,
                            chainBook.id,
                            chainBook.name,
                            localUri,
                            chainBook.origin.originUrl,
                            chainBook.process,
                            type = LocalBook.TYPE_PDF
                        ).apply {
                            PdfRenderer(
                                ParcelFileDescriptor.open(
                                    requireActivity().getExternalFile(localUri),
                                    ParcelFileDescriptor.MODE_READ_ONLY
                                )
                            ).let {
                                size = it.pageCount.toLong()
                                it.close()
                            }
                        }
                    )

                    activity?.runOnUiThread {
                        holder.stopWork()
                    }

                    requireContext().alert("${chainBook.name}准备就绪！")
                }

                override fun onDownloading(progress: Int) {
                    holder.updateWorkProgress(progress)
                }

                override fun onDownloadFailed(errMsg: String) {
                    requireContext().alert("下载失败了哦~")

                    holder.stopWork("下载失败")
                }
            })
    }


    private fun downloadEpub(holder: BookCardAdapter.BookCardLineViewHolder, chainBook: ChainBook) {
        if (!holder.startWork()) return

        requireContext().alert("开始下载！")

        val saveFolderPath = getChainBookCacheFolder(chainBook.id)

        val saveBookFileName = chainBook.name + chainBook.ext

        val output = requireActivity().openFileOutput(saveFolderPath, saveBookFileName)

        val remoteUrl = chainBook.origin.epubUrl

        downloadBinder.startDownload(
            remoteUrl,
            output,
            object : DownloadService.OnDownloadListener {
                override fun onDownloadSuccess() {
//                    requireContext().alert("开始解析${chainBook.name}！")

                    val input = requireActivity().openFileInput(saveFolderPath, saveBookFileName)

                    epubMediaBinder.readAllMediaResource(
                        input,
                        object : EpubMediaService.OnSaveMediaHandle {
                            override fun onSaveMedia(
                                byteArray: ByteArray,
                                name: String,
                                href: String,
                                ext: String,
                            ) {
                                val localUri = saveFolderPath + name
                                val out = requireActivity().openFileOutput(saveFolderPath, name)

                                saveByteArray2File(byteArray, out)

                                val mediaSourceDao = database.mediaSourceDao()

                                mediaSourceDao.insertMediaSource(
                                    MediaSource(
                                        chainBook.id,
                                        name,
                                        ext,
                                        href,
                                        localUri,
                                        Date().time
                                    )
                                )
                            }

                            override fun onComplete(chapters: ArrayList<Chapter>, author: String) {
                                val localBookDao = database.localBookDao()
                                val chapterDao = database.chapterDap()

                                var totalSize = 0L

                                chapters.forEach {
                                    totalSize += it.size
                                    it.cbid = chainBook.id
                                }

                                chapterDao.insertChapters(chapters)

                                localBookDao.insertOneBook(
                                    LocalBook.create(
                                        chainBook.origin.id,
                                        chainBook.id,
                                        chainBook.name,
                                        saveFolderPath + saveBookFileName,
                                        chainBook.origin.originUrl,
                                        chainBook.process,
                                        author = author,
                                        size = totalSize,
                                        type = LocalBook.TYPE_EPUB
                                    )
                                )

                                activity?.runOnUiThread {
                                    holder.stopWork()
                                }

                                requireContext().alert("${chainBook.name}准备就绪！")
                            }

                            override fun onFailure(err: Throwable) {
                                requireContext().alert("解析失败了奥～")

                                activity?.runOnUiThread {
                                    holder.stopWork("解析失败")
                                }
                            }
                        })
                }

                override fun onDownloading(progress: Int) {
                    holder.updateWorkProgress((progress * .9).toInt())
                }

                override fun onDownloadFailed(errMsg: String) {
                    requireContext().alert("下载失败了哦~")

                    holder.stopWork("下载失败")
                }
            })

    }
}
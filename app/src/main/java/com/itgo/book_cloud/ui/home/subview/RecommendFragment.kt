package com.itgo.book_cloud.ui.home.subview

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.itgo.book_cloud.R
import com.itgo.book_cloud.common.alert
import com.itgo.book_cloud.data.model.BookShelfItem
import com.itgo.book_cloud.data.model.OriginBook
import com.itgo.book_cloud.http.ServiceFactory
import com.itgo.book_cloud.http.service.AddBookToShelfBody
import com.itgo.book_cloud.http.service.BookService
import com.itgo.book_cloud.http.service.RecommendService
import com.itgo.book_cloud.ui.components.BookRecAdapter
import com.itgo.book_cloud.ui.components.UserRecAdapter
import kotlinx.android.synthetic.main.fragment_recommend.*
import kotlinx.android.synthetic.main.fragment_recommend.loading
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RecommendFragment : Fragment() {
    private val bookService by lazy { ServiceFactory(requireContext()).create(BookService::class.java) }
    private val recommendService by lazy { ServiceFactory(requireContext()).create(RecommendService::class.java) }

    private val markSuccessAlertDialog by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage("已添加到书架了哦😯")
            .setNeutralButton("取消") { _: DialogInterface, _: Int -> }
            .setPositiveButton("立即前往查看") { _: DialogInterface, _: Int ->
                Navigation.findNavController(this.requireView())
                    .navigate(R.id.bookShelfFragment, null)
            }.create()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recommend, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loading.visibility = View.VISIBLE
        var counter = 2
        recommendService.getRecsByBook().enqueue(object : Callback<List<OriginBook>> {
            override fun onResponse(
                call: Call<List<OriginBook>>,
                response: Response<List<OriginBook>>
            ) {
                counter--
                if (counter == 0) {
                    loading.visibility = View.INVISIBLE
                }
                bookRecList.apply {
                    layoutManager = GridLayoutManager(context, 2).apply {
                        orientation = GridLayoutManager.HORIZONTAL
                    }
                    response.body()?.let {
                        adapter =
                            BookRecAdapter(
                                this@RecommendFragment.requireContext(),
                                it,
                                this@RecommendFragment::onClickBookCard
                            )
                    }
                }

            }

            override fun onFailure(call: Call<List<OriginBook>>, t: Throwable) {
                Log.d("debug", t.message.toString())
            }

        })

        recommendService.getRecsByUser().enqueue(object : Callback<List<OriginBook>> {
            override fun onResponse(
                call: Call<List<OriginBook>>,
                response: Response<List<OriginBook>>
            ) {
                counter--
                if (counter == 0) {
                    loading.visibility = View.INVISIBLE
                }
                userRecList.apply {
                    layoutManager = GridLayoutManager(context, 4).apply {
                        orientation = GridLayoutManager.HORIZONTAL
                    }
                    response.body()?.let {
                        adapter = UserRecAdapter(
                            this@RecommendFragment.requireContext(),
                            it,
                            this@RecommendFragment::onClickBookCard
                        )
                    }
                }
            }

            override fun onFailure(call: Call<List<OriginBook>>, t: Throwable) {
                Log.d("debug", t.message.toString())
            }
        })
    }

    private fun onClickBookCard(
        view: View,
        holder: BookRecAdapter.BookRecItemViewHolder,
        book: OriginBook
    ) {
        bookService.getAllBookShelf().enqueue(object : Callback<List<BookShelfItem>> {
            override fun onResponse(
                call: Call<List<BookShelfItem>>,
                response: Response<List<BookShelfItem>>
            ) {
                response.body()?.let {
                    bookService.addBookToShelf(it[0].bsid, AddBookToShelfBody(listOf(book.id)))
                        .enqueue(object : Callback<ResponseBody> {
                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {
                                markSuccessAlertDialog.show()
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                requireContext().alert("网络情况较差！请重试！")
                            }
                        })
                }
            }

            override fun onFailure(call: Call<List<BookShelfItem>>, t: Throwable) {
                requireContext().alert("网络情况较差！请重试！")
            }
        })
    }

    private fun onClickBookCard(
        view: View,
        holder: UserRecAdapter.UserRecItemViewHolder,
        book: OriginBook
    ) {
        bookService.getAllBookShelf().enqueue(object : Callback<List<BookShelfItem>> {
            override fun onResponse(
                call: Call<List<BookShelfItem>>,
                response: Response<List<BookShelfItem>>
            ) {
                response.body()?.let {
                    bookService.addBookToShelf(it[0].bsid, AddBookToShelfBody(listOf(book.id)))
                        .enqueue(object : Callback<ResponseBody> {
                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {
                                markSuccessAlertDialog.show()
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                requireContext().alert("网络情况较差！请重试！")
                            }
                        })
                }
            }

            override fun onFailure(call: Call<List<BookShelfItem>>, t: Throwable) {
                requireContext().alert("网络情况较差！请重试！")
            }
        })
    }
}
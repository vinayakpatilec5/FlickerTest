package com.example.dunzotest.ui.main

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dunzotest.R
import com.example.dunzotest.ui.common.LoadingWidget
import com.example.dunzotest.ui.main.adapter.PhotoAdapter
import com.example.dunzotest.util.CustomApplication
import com.example.dunzotest.util.ViewModelFactory
import javax.inject.Inject

class MainActivity : AppCompatActivity(), TextWatcher, LoadingWidget.Callback {
    lateinit var mainViewModel: MainViewModel
    lateinit var adapter: PhotoAdapter
    lateinit var layoutManager: GridLayoutManager
    lateinit var recyclerView: RecyclerView
    lateinit var errorText: TextView
    lateinit var loader: ProgressBar
    lateinit var paginationLoader: LoadingWidget
    lateinit var editText: EditText

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        (application as CustomApplication).component.inject(this)
        mainViewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
        initView()
        setUpRecyclerView()
        setListeners()
        loadData()
    }


    fun initView() {
        recyclerView = findViewById(R.id.recycler_view)
        errorText = findViewById(R.id.error_text)
        loader = findViewById(R.id.loader)
        paginationLoader = findViewById(R.id.bottom_loader)
        editText = findViewById(R.id.edit_text)
        mainViewModel.setSearchTextListener()
    }

    fun setListeners() {
        editText.addTextChangedListener(this)
        paginationLoader.callback = this

        mainViewModel.photoList.observe(this, Observer {
            errorText.visibility = View.GONE
            paginationLoader.hideLoading()
            adapter.setData(it)
        })
        mainViewModel.loading.observe(this, Observer {
            if (it) {
                loader.visibility = View.VISIBLE
                errorText.visibility = View.GONE
                adapter.clearData()
            } else {
                loader.visibility = View.GONE
            }
        })

        mainViewModel.paginationLoading.observe(this, Observer {
            if (it) {
                paginationLoader.showLoading()
                errorText.visibility = View.GONE
            } else {
                paginationLoader.hideLoading()
            }
        })

        mainViewModel.error.observe(this, Observer {
            errorText.visibility = View.VISIBLE
            errorText.text = it
            adapter.clearData()

        })

        mainViewModel.paginationError.observe(this, Observer {
            paginationLoader.setRetryMessage(it)
        })
    }

    //on activity started depend on new instance or rotation get data
    fun loadData() {
        if (mainViewModel.allPhotoList.size > 0) {
            errorText.visibility = View.GONE
            paginationLoader.hideLoading()
            adapter.setData(mainViewModel.allPhotoList)
        } else {
            mainViewModel.setEmptySearchTextData()
        }
    }


    fun setUpRecyclerView() {
        adapter = PhotoAdapter()
        layoutManager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!mainViewModel.paginationDone) {
                    val isLastPosition =
                        layoutManager.findLastCompletelyVisibleItemPosition() == adapter.itemCount - 1
                    if (mainViewModel.loading.value?.not() ?: false && mainViewModel.paginationLoading.value?.not() ?: false && isLastPosition) {
                        mainViewModel.loadNextPage()
                    }
                }
            }
        })
    }

    //on retry tapped at bottom widget
    override fun retryNextPageLoad() {
        if (mainViewModel.loading.value?.not() ?: false && mainViewModel.paginationLoading.value?.not() ?: false) {
            mainViewModel.loadNextPage()
        }
    }

    override fun afterTextChanged(s: Editable?) {
        mainViewModel.searchEditText.onNext(editText.text.toString())
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
}
package com.client.shop.ui.blog

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.client.shop.R
import com.client.shop.ShopApplication
import com.client.shop.ui.blog.adapter.BlogAdapter
import com.client.shop.ui.blog.contract.BlogPresenter
import com.client.shop.ui.blog.contract.BlogView
import com.client.shop.ui.blog.di.BlogModule
import com.domain.entity.Article
import com.ui.base.lce.BaseFragment
import com.ui.base.recycler.OnItemClickListener
import com.ui.const.Constant.DEFAULT_PER_PAGE_COUNT
import com.ui.custom.DividerItemDecorator
import kotlinx.android.synthetic.main.fragment_blog.*
import javax.inject.Inject

class BlogFragment :
    BaseFragment<List<Article>, BlogView, BlogPresenter>(),
    BlogView,
    OnItemClickListener {

    @Inject
    lateinit var blogPresenter: BlogPresenter
    private var articleList = mutableListOf<Article>()
    private lateinit var adapter: BlogAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        seeAll.setOnClickListener {
            context?.let { startActivity(BlogActivity.getStartIntent(it)) }
        }
        changeSeeAllState()
        setupRecycler()
        loadData(true)
    }

    override fun inject() {
        ShopApplication.appComponent.attachBlogComponent(BlogModule()).inject(this)
    }

    //SETUP

    private fun setupRecycler() {
        adapter = BlogAdapter(articleList, this)
        val layoutManager = LinearLayoutManager(context)
        val divider =
            DividerItemDecorator(context, R.drawable.divider_line)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.addItemDecoration(divider)
    }

    private fun changeSeeAllState() {
        if (articleList.size == DEFAULT_PER_PAGE_COUNT) {
            seeAll.visibility = View.VISIBLE
        } else {
            seeAll.visibility = View.GONE
        }
    }

    //INITIAL

    override fun createPresenter() = blogPresenter

    override fun getContentView() = R.layout.fragment_blog

    //LCE

    override fun loadData(pullToRefresh: Boolean) {
        super.loadData(pullToRefresh)
        presenter.loadArticles(DEFAULT_PER_PAGE_COUNT)
    }

    override fun showContent(data: List<Article>) {
        super.showContent(data)
        this.articleList.clear()
        this.articleList.addAll(data)
        adapter.notifyDataSetChanged()
        changeSeeAllState()
    }

    //CALLBACK

    override fun onItemClicked(position: Int) {
        articleList.getOrNull(position)?.let {
            val articleId = it.id
            context?.let {
                startActivity(ArticleActivity.getStartIntent(it, articleId))
            }
        }
    }

}
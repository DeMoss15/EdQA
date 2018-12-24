package com.demoss.idp.presentation.main.tests

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.demoss.idp.R
import com.demoss.idp.base.BaseFragment
import com.demoss.idp.domain.model.TestModel
import com.demoss.idp.presentation.adapter.TestsRecyclerViewAdapter
import com.demoss.idp.presentation.main.main.MainCallback
import com.demoss.idp.util.Constants
import com.demoss.idp.util.pagination.setOnNextPageListener
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_local_data.*
import org.koin.android.ext.android.inject

class TestsFragment : BaseFragment<TestsContract.Presenter>(), TestsContract.View {

    companion object {
        const val TAG = "com.demoss.diploma.tests_fragment"
        fun newInstance(): TestsFragment = TestsFragment()
    }

    override val presenter by inject<TestsContract.Presenter>()
    override val layoutResourceId = R.layout.activity_local_data
    private lateinit var mainCallback: MainCallback
    private lateinit var callback: Callback
    private val rvAdapter = TestsRecyclerViewAdapter { test, action ->
        when (action) {
            TestsRecyclerViewAdapter.Action.SELECT -> callback.startTest(test)
            TestsRecyclerViewAdapter.Action.EDIT -> mainCallback.nextFragment(TAG, test.id)
            TestsRecyclerViewAdapter.Action.SHARE -> TODO("no share action")
        }
    }

    // Lifecycle =======================================================================================================
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mainCallback = activity as MainCallback
        callback = activity as Callback
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainCallback.readyToSetupAppBar()
        rvTests.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = rvAdapter
            setOnNextPageListener(RecyclerView.FOCUS_DOWN) {
                presenter.loadMore()
            }
        }
    }

    // MainFragment ===================================================================================================
    override fun onFabPressed() {
        mainCallback.nextFragment(TAG, Constants.NEW_TEST_ID)
    }

    override fun onMenuItemPressed(itemId: Int) {
        // TODO: add menu handling
    }

    // Paginator =======================================================================================================
    override fun showEmptyProgress(show: Boolean) {
        emptyProgressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showEmptyError(show: Boolean, error: Throwable?) {
        emptyState.text = getText(R.string.rv_error_message)
        emptyState.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showEmptyView(show: Boolean) {
        emptyState.text = getString(R.string.rv_empty_data, resources.getQuantityString(R.plurals.test_plural, Int.MAX_VALUE))
        emptyState.visibility = if (show) View.VISIBLE else View.GONE
        rvTests.visibility =if (show) View.GONE else View.VISIBLE
    }

    override fun showData(show: Boolean, data: List<TestModel>) {
        rvAdapter.dispatchData(data)
    }

    override fun showErrorMessage(error: Throwable) {
        emptyState.text = error.localizedMessage
        emptyState.visibility = View.VISIBLE
    }

    override fun showRefreshProgress(show: Boolean) {
        emptyProgressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showPageProgress(show: Boolean) {
        pageProgress.visibility = if (show) View.VISIBLE else View.GONE
    }

    interface Callback {
        fun startTest(test: TestModel)
    }

    // Private =====================================================================================
    override fun setupAppBar(bottomAppBar: BottomAppBar, fab: FloatingActionButton) {
        fab.setImageResource(R.drawable.ic_add)
        bottomAppBar.apply {
            fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
            replaceMenu(R.menu.bottomappbar_menu_tests)
        }
    }
}

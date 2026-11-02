package com.appmaker.quickbill

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appmaker.quickbill.data.BillRepository
import com.appmaker.quickbill.model.Bill
import com.appmaker.quickbill.model.BillCategory
import com.appmaker.quickbill.ui.AddBillSheet
import com.appmaker.quickbill.ui.BillAdapter
import com.appmaker.quickbill.ui.BillDetailDialog
import com.appmaker.quickbill.ui.BillUiState
import com.appmaker.quickbill.ui.MainViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: BillAdapter

    private lateinit var totalAmount: TextView
    private lateinit var billCount: TextView
    private lateinit var recycler: RecyclerView
    private lateinit var emptyState: View
    private lateinit var loading: CircularProgressIndicator
    private lateinit var filterGroup: ChipGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val repo = BillRepository.get(applicationContext)
        val factory = MainViewModel.Factory(repo)
        val vm: MainViewModel by viewModels { factory }
        viewModel = vm

        totalAmount = findViewById(R.id.totalAmount)
        billCount = findViewById(R.id.billCount)
        recycler = findViewById(R.id.billsRecycler)
        emptyState = findViewById(R.id.emptyState)
        loading = findViewById(R.id.loadingIndicator)
        filterGroup = findViewById(R.id.filterGroup)
        val fab: FloatingActionButton = findViewById(R.id.addFab)

        adapter = BillAdapter { bill -> showDetail(bill) }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        fab.setOnClickListener { showAddSheet() }

        setupFilters()

        supportFragmentManager.setFragmentResultListener(
            AddBillSheet.RESULT_KEY,
            this
        ) { _, bundle ->
            val store = bundle.getString(AddBillSheet.RESULT_STORE).orEmpty()
            val amount = bundle.getDouble(AddBillSheet.RESULT_AMOUNT)
            val category = bundle.getString(AddBillSheet.RESULT_CATEGORY)
                ?: BillCategory.OTHER.key
            val imagePath = bundle.getString(AddBillSheet.RESULT_IMAGE_PATH)
            val id = bundle.getString(AddBillSheet.RESULT_ID) ?: return@setFragmentResultListener
            val createdAt = bundle.getLong(
                AddBillSheet.RESULT_CREATED_AT,
                System.currentTimeMillis()
            )
            viewModel.addBill(
                Bill(
                    id = id,
                    storeName = store,
                    amount = amount,
                    categoryKey = category,
                    imagePath = imagePath,
                    createdAt = createdAt
                )
            )
        }

        supportFragmentManager.setFragmentResultListener(
            BillDetailDialog.RESULT_KEY,
            this
        ) { _, bundle ->
            val id = bundle.getString(BillDetailDialog.RESULT_ID) ?: return@setFragmentResultListener
            viewModel.deleteBill(id)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun setupFilters() {
        filterGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val id = checkedIds.firstOrNull() ?: View.NO_ID
            val category = when (id) {
                R.id.filterFood -> BillCategory.FOOD
                R.id.filterShopping -> BillCategory.SHOPPING
                R.id.filterHome -> BillCategory.HOME
                R.id.filterOther -> BillCategory.OTHER
                else -> null
            }
            viewModel.setFilter(category)
        }
    }

    private fun render(state: BillUiState) {
        when (state) {
            is BillUiState.Loading -> {
                loading.visibility = View.VISIBLE
                recycler.visibility = View.GONE
                emptyState.visibility = View.GONE
            }
            is BillUiState.Empty -> {
                loading.visibility = View.GONE
                recycler.visibility = View.GONE
                emptyState.visibility = View.VISIBLE
                adapter.submitList(emptyList())
                totalAmount.text = getString(
                    R.string.amount_with_currency,
                    String.format(Locale.US, "%.2f", state.totalAll)
                )
                billCount.text = getString(R.string.bill_count, state.countAll)
            }
            is BillUiState.Content -> {
                loading.visibility = View.GONE
                emptyState.visibility = View.GONE
                recycler.visibility = View.VISIBLE
                adapter.submitList(state.bills)
                totalAmount.text = getString(
                    R.string.amount_with_currency,
                    String.format(Locale.US, "%.2f", state.totalAll)
                )
                billCount.text = getString(R.string.bill_count, state.countAll)
            }
        }
    }

    private fun showAddSheet() {
        if (supportFragmentManager.findFragmentByTag(AddBillSheet.TAG_SHEET) != null) return
        AddBillSheet().show(supportFragmentManager, AddBillSheet.TAG_SHEET)
    }

    private fun showDetail(bill: Bill) {
        if (supportFragmentManager.findFragmentByTag(BillDetailDialog.TAG_DIALOG) != null) return
        BillDetailDialog.newInstance(bill)
            .show(supportFragmentManager, BillDetailDialog.TAG_DIALOG)
    }
}
package com.appmaker.quickbill.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.appmaker.quickbill.data.BillRepository
import com.appmaker.quickbill.model.Bill
import com.appmaker.quickbill.model.BillCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val repo: BillRepository) : ViewModel() {

    private val filter = MutableStateFlow<BillCategory?>(null)

    val uiState: StateFlow<BillUiState> =
        combine(repo.bills, filter) { list, category ->
            val totalAll = list.sumOf { it.amount }
            val countAll = list.size
            val filtered = if (category == null) {
                list
            } else {
                list.filter { it.categoryKey == category.key }
            }.sortedByDescending { it.createdAt }

            if (filtered.isEmpty()) {
                BillUiState.Empty(totalAll = totalAll, countAll = countAll)
            } else {
                BillUiState.Content(
                    bills = filtered,
                    totalAll = totalAll,
                    countAll = countAll
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = BillUiState.Loading
        )

    fun setFilter(category: BillCategory?) {
        filter.value = category
    }

    fun addBill(bill: Bill) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.addBill(bill)
        }
    }

    fun deleteBill(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.deleteBill(id)
        }
    }

    class Factory(private val repo: BillRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(repo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
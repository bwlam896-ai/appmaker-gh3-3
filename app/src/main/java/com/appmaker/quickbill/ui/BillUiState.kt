package com.appmaker.quickbill.ui

import com.appmaker.quickbill.model.Bill

sealed class BillUiState {
    object Loading : BillUiState()

    data class Empty(
        val totalAll: Double,
        val countAll: Int
    ) : BillUiState()

    data class Content(
        val bills: List<Bill>,
        val totalAll: Double,
        val countAll: Int
    ) : BillUiState()
}
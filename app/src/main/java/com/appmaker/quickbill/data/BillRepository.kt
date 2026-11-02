package com.appmaker.quickbill.data

import android.content.Context
import com.appmaker.quickbill.model.Bill
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class BillRepository private constructor(context: Context) {

    private val storage = BillStorage(context.applicationContext)
    private val _bills = MutableStateFlow(storage.load())
    val bills: StateFlow<List<Bill>> = _bills.asStateFlow()

    fun addBill(bill: Bill) {
        val next = _bills.value + bill
        storage.save(next)
        _bills.value = next
    }

    fun deleteBill(id: String) {
        val current = _bills.value
        val target = current.firstOrNull { it.id == id } ?: return
        target.imagePath?.let { path ->
            runCatching { File(path).delete() }
        }
        val next = current.filterNot { it.id == id }
        storage.save(next)
        _bills.value = next
    }

    companion object {
        @Volatile
        private var instance: BillRepository? = null

        fun get(context: Context): BillRepository {
            return instance ?: synchronized(this) {
                instance ?: BillRepository(context).also { instance = it }
            }
        }
    }
}
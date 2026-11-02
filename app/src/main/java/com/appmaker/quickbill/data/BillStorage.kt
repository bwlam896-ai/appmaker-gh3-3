package com.appmaker.quickbill.data

import android.content.Context
import android.util.Log
import com.appmaker.quickbill.model.Bill
import com.google.gson.Gson

class BillStorage(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun load(): List<Bill> {
        val json = prefs.getString(KEY_BILLS, null) ?: return emptyList()
        return try {
            val arr = gson.fromJson(json, Array<Bill>::class.java)
            arr?.toList() ?: emptyList()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse stored bills", e)
            emptyList()
        }
    }

    fun save(bills: List<Bill>) {
        try {
            val json = gson.toJson(bills)
            prefs.edit().putString(KEY_BILLS, json).apply()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save bills", e)
        }
    }

    companion object {
        private const val TAG = "BillStorage"
        private const val PREFS = "quickbill_prefs"
        private const val KEY_BILLS = "bills_json"
    }
}
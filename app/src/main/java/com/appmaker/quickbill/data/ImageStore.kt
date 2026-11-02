package com.appmaker.quickbill.data

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object ImageStore {

    private const val TAG = "ImageStore"

    fun billsDir(context: Context): File {
        val dir = File(context.filesDir, "bills")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun newBillFile(context: Context): File {
        val name = "bill_${System.currentTimeMillis()}_${(0..999).random()}.jpg"
        return File(billsDir(context), name)
    }

    fun newCameraTemp(context: Context): File {
        val dir = File(context.cacheDir, "camera")
        if (!dir.exists()) dir.mkdirs()
        val name = "capture_${System.currentTimeMillis()}.jpg"
        return File(dir, name)
    }

    fun copyFromUri(context: Context, uri: Uri): String? {
        return try {
            val target = newBillFile(context)
            val input = context.contentResolver.openInputStream(uri)
                ?: return null
            input.use { source ->
                FileOutputStream(target).use { out ->
                    source.copyTo(out)
                }
            }
            target.absolutePath
        } catch (e: Exception) {
            Log.w(TAG, "copyFromUri failed", e)
            targetFileCleanup(context)
            null
        }
    }

    private fun targetFileCleanup(context: Context) {
        runCatching {
            val dir = billsDir(context)
            dir.listFiles()
                ?.filter { it.length() == 0L }
                ?.forEach { it.delete() }
        }
    }
}
package com.appmaker.quickbill.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import coil.load
import com.appmaker.quickbill.R
import com.appmaker.quickbill.model.Bill
import com.appmaker.quickbill.model.BillCategory
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BillDetailDialog : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.Theme_QuickBill)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.dialog_bill_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = requireArguments()
        val id = args.getString(ARG_ID).orEmpty()
        val store = args.getString(ARG_STORE).orEmpty()
        val amount = args.getDouble(ARG_AMOUNT)
        val categoryKey = args.getString(ARG_CATEGORY).orEmpty()
        val imagePath = args.getString(ARG_IMAGE_PATH)
        val createdAt = args.getLong(ARG_CREATED_AT)

        view.findViewById<TextView>(R.id.detailStore).text = store
        view.findViewById<TextView>(R.id.detailAmount).text =
            getString(R.string.amount_with_currency, String.format(Locale.US, "%.2f", amount))
        view.findViewById<TextView>(R.id.detailDate).text =
            SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(createdAt))

        val category = BillCategory.fromKey(categoryKey)
        val categoryView = view.findViewById<TextView>(R.id.detailCategory)
        categoryView.text = getString(category.labelRes)

        val imageView = view.findViewById<ImageView>(R.id.detailImage)
        val file = imagePath?.let { File(it) }
        if (file != null && file.exists()) {
            imageView.load(file) {
                crossfade(true)
                placeholder(R.drawable.bg_image_placeholder)
                error(R.drawable.ic_receipt)
            }
        } else {
            imageView.setImageResource(R.drawable.ic_receipt)
        }

        view.findViewById<MaterialButton>(R.id.deleteButton).setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete)
                .setMessage(store)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete) { _, _ ->
                    parentFragmentManager.setFragmentResult(
                        RESULT_KEY,
                        Bundle().apply { putString(RESULT_ID, id) }
                    )
                    dismiss()
                }
                .show()
        }

        view.findViewById<MaterialButton>(R.id.closeButton).setOnClickListener { dismiss() }
    }

    companion object {
        const val TAG_DIALOG = "bill_detail_dialog"
        const val RESULT_KEY = "bill_delete_result"
        const val RESULT_ID = "delete_id"

        private const val ARG_ID = "id"
        private const val ARG_STORE = "store"
        private const val ARG_AMOUNT = "amount"
        private const val ARG_CATEGORY = "category"
        private const val ARG_IMAGE_PATH = "image_path"
        private const val ARG_CREATED_AT = "created_at"

        fun newInstance(bill: Bill): BillDetailDialog {
            return BillDetailDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_ID, bill.id)
                    putString(ARG_STORE, bill.storeName)
                    putDouble(ARG_AMOUNT, bill.amount)
                    putString(ARG_CATEGORY, bill.categoryKey)
                    putString(ARG_IMAGE_PATH, bill.imagePath)
                    putLong(ARG_CREATED_AT, bill.createdAt)
                }
            }
        }
    }
}
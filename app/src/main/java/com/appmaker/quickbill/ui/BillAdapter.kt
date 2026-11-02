package com.appmaker.quickbill.ui

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.appmaker.quickbill.R
import com.appmaker.quickbill.model.Bill
import com.appmaker.quickbill.model.BillCategory
import com.google.android.material.card.MaterialCardView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BillAdapter(
    private val onClick: (Bill) -> Unit
) : ListAdapter<Bill, BillAdapter.BillViewHolder>(DIFF) {

    class BillViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.billCard)
        val image: ImageView = view.findViewById(R.id.billImage)
        val store: TextView = view.findViewById(R.id.billStore)
        val amount: TextView = view.findViewById(R.id.billAmount)
        val date: TextView = view.findViewById(R.id.billDate)
        val category: TextView = view.findViewById(R.id.billCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bill, parent, false)
        return BillViewHolder(view)
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        val bill = getItem(position)
        val ctx = holder.itemView.context

        holder.store.text = bill.storeName
        holder.amount.text = ctx.getString(
            R.string.amount_with_currency,
            String.format(Locale.US, "%.2f", bill.amount)
        )
        holder.date.text = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            .format(Date(bill.createdAt))

        val category = BillCategory.fromKey(bill.categoryKey)
        holder.category.text = ctx.getString(category.labelRes)
        val bg = GradientDrawable().apply {
            cornerRadius = 40f
            setColor(ContextCompat.getColor(ctx, category.colorRes))
        }
        holder.category.background = bg
        holder.category.setTextColor(
            ContextCompat.getColor(ctx, R.color.category_text_color)
        )

        val imageFile = bill.imagePath?.let { File(it) }
        if (imageFile != null && imageFile.exists()) {
            holder.image.load(imageFile) {
                crossfade(true)
                placeholder(R.drawable.bg_image_placeholder)
                error(R.drawable.ic_receipt)
            }
        } else {
            holder.image.setImageResource(R.drawable.ic_receipt)
        }
        holder.image.contentDescription = ctx.getString(
            R.string.bill_thumbnail
        )

        holder.card.setOnClickListener { onClick(bill) }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Bill>() {
            override fun areItemsTheSame(oldItem: Bill, newItem: Bill): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Bill, newItem: Bill): Boolean =
                oldItem == newItem
        }
    }
}
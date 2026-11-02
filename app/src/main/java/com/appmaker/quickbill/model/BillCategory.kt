package com.appmaker.quickbill.model

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.appmaker.quickbill.R

enum class BillCategory(
    val key: String,
    @StringRes val labelRes: Int,
    @ColorRes val colorRes: Int
) {
    FOOD("food", R.string.category_food, R.color.category_food),
    SHOPPING("shopping", R.string.category_shopping, R.color.category_shopping),
    HOME("home", R.string.category_home, R.color.category_home),
    OTHER("other", R.string.category_other, R.color.category_other);

    companion object {
        fun fromKey(key: String?): BillCategory =
            values().firstOrNull { it.key == key } ?: OTHER
    }
}
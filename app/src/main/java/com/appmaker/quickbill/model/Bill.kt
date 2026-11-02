package com.appmaker.quickbill.model

data class Bill(
    val id: String,
    val storeName: String,
    val amount: Double,
    val categoryKey: String,
    val imagePath: String?,
    val createdAt: Long
)
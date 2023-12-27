package com.example.sharedshoppinglist.models

import java.util.*

data class ShoppingItem (
    var id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var quantity: Double = 0.0,
    var isMarked: Boolean = false
)

package com.switchsolutions.farmtohome.b2b.roomdb.cart

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_data_table")
data class CartEntityClass(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int,

    @ColumnInfo(name = "imgUrl")
    var imgUrl: String,

    @ColumnInfo(name = "label")
    var label: String,

    @ColumnInfo(name = "site_product_id")
    var site_product_id: Int,

    @ColumnInfo(name = "product_quantity")
    var quantity: String,

    @ColumnInfo(name = "unit")
    var unit: String,

    @ColumnInfo(name = "delivery_date")
    var deliveryDate: String,




)
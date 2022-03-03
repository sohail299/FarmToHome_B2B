package com.switchsolutions.farmtohome.b2b.roomdb.product

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_data_table")
data class ProductEntityClass(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int,

    @ColumnInfo(name = "imgUrl")
    var imgUrl: String,

    @ColumnInfo(name = "label")
    var label: String,

    @ColumnInfo(name = "site_product_id")
    var site_product_id: Int,

    @ColumnInfo(name = "unit")
    var unit: String

)
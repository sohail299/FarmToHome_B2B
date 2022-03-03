package com.switchsolutions.farmtohome.b2b.roomdb.product

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDAO {

    @Insert
    suspend fun insertProduct(productEntityClass: ProductEntityClass) : Long

    @Update
    suspend fun updateProducts(productEntityClass: ProductEntityClass) : Int

    @Delete
    suspend fun deleteProducts(productEntityClass: ProductEntityClass) : Int

    @Query("DELETE FROM product_data_table")
    suspend fun deleteAll() : Int

    @Query("SELECT * FROM product_data_table")
    fun getAllProducts():Flow<List<ProductEntityClass>>
}
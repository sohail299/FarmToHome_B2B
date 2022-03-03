package com.switchsolutions.farmtohome.b2b.roomdb.cart

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDAO {

    @Insert
    suspend fun insertSubscriber(cartEntityClass: CartEntityClass) : Long

    @Update
    suspend fun updateSubscriber(cartEntityClass: CartEntityClass) : Int

    @Delete
    suspend fun deleteSubscriber(cartEntityClass: CartEntityClass) : Int

    @Query("DELETE FROM cart_data_table")
    suspend fun deleteAll() : Int

    @Query("SELECT * FROM cart_data_table")
    fun getAllSubscribers():Flow<List<CartEntityClass>>

    @Query("SELECT * FROM cart_data_table")
    fun getCount(): Int
}
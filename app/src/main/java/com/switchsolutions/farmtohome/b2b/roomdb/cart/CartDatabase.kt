package com.switchsolutions.farmtohome.b2b.roomdb.cart

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CartEntityClass::class], version = 1)
abstract class CartDatabase : RoomDatabase() {
    abstract val cartDAO: CartDAO

    companion object {
        @Volatile
        private var INSTANCE: CartDatabase? = null
        fun getInstance(context: Context): CartDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        CartDatabase::class.java,
                        "cart_data_database"
                    ).build()
                }
                return instance
            }
        }
    }
}


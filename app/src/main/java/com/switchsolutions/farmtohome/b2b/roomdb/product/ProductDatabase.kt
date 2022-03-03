package com.switchsolutions.farmtohome.b2b.roomdb.product


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ProductEntityClass::class], version = 1)
abstract class ProductDatabase : RoomDatabase() {
    abstract val productDao: ProductDAO

    companion object {
        @Volatile
        private var INSTANCE: ProductDatabase? = null
        fun getInstance(context: Context): ProductDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ProductDatabase::class.java,
                        "product_data_database"
                    ).build()
                }
                return instance
            }
        }
    }
}



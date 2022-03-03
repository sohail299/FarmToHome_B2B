package com.switchsolutions.farmtohome.b2b.roomdb.cart



class CartRepository(private val dao: CartDAO) {

    val product = dao.getAllSubscribers()

    suspend fun insert(cartEntityClass: CartEntityClass): Long {
        return dao.insertSubscriber(cartEntityClass)
    }

    suspend fun update(cartEntityClass: CartEntityClass): Int {
        return dao.updateSubscriber(cartEntityClass)
    }

    suspend fun delete(cartEntityClass: CartEntityClass): Int {
        return dao.deleteSubscriber(cartEntityClass)
    }

    suspend fun deleteAll(): Int {
        return dao.deleteAll()
    }

    fun getItemCount(): Int {
        return dao.getCount()
    }
}
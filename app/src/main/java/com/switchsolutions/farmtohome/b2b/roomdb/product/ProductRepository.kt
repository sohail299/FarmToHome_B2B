package com.switchsolutions.farmtohome.b2b.roomdb.product



class ProductRepository(private val daoProduct: ProductDAO) {

    val product = daoProduct.getAllProducts()

    suspend fun insert(productEntityClass: ProductEntityClass): Long {
        return daoProduct.insertProduct(productEntityClass)
    }

    suspend fun deleteAll(): Int {
        return daoProduct.deleteAll()
    }
}
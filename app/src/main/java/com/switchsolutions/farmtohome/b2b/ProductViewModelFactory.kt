package com.switchsolutions.farmtohome.b2b

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.switchsolutions.farmtohome.b2b.roomdb.product.ProductRepository
import java.lang.IllegalArgumentException

class ProductViewModelFactory(
    private val repository: ProductRepository
):ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ProductViewModel::class.java)){
            return ProductViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown View Model class")
    }

}
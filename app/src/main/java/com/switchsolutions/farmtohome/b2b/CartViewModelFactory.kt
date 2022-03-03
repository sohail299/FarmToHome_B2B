package com.switchsolutions.farmtohome.b2b

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.switchsolutions.farmtohome.b2b.roomdb.cart.CartRepository
import java.lang.IllegalArgumentException

class CartViewModelFactory(
        private val repository: CartRepository
        ):ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(CartViewModel::class.java)){
            return CartViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown View Model class")
    }

}
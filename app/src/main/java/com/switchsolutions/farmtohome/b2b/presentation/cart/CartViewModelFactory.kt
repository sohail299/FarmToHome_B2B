package com.switchsolutions.farmtohome.b2b.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.switchsolutions.farmtohome.b2b.presentation.cart.viewmodel.SubmitCartViewModel
import java.lang.IllegalArgumentException

class CartViewModelFactory(private val totalAmount : Int) : ViewModelProvider.Factory{

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubmitCartViewModel::class.java)){
            return SubmitCartViewModel() as T
        }
        throw IllegalArgumentException("Unknown View Model Class")
    }


}
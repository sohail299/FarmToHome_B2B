package com.switchsolutions.farmtohome.b2b.data.viewmodel

import androidx.lifecycle.*
import com.switchsolutions.farmtohome.b2b.callbacks.Event
import com.switchsolutions.farmtohome.b2b.roomdb.product.ProductEntityClass
import com.switchsolutions.farmtohome.b2b.roomdb.product.ProductRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    private val statusMessage = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>>
        get() = statusMessage
    fun insertProduct(product: ProductEntityClass) = viewModelScope.launch {
        val newRowId = repository.insert(product)
        if (newRowId > -1) {
            statusMessage.value = Event("Product Inserted Successfully $newRowId")
        } else {
            statusMessage.value = Event("Error Occurred")
        }
    }


    fun getSavedProducts() = liveData {
        repository.product.collect {
            emit(it)
        }
    }

    fun clearAll() = viewModelScope.launch {
        val noOfRowsDeleted = repository.deleteAll()
        if (noOfRowsDeleted > 0) {
            statusMessage.value = Event("$noOfRowsDeleted products Deleted Successfully")
        } else {
            statusMessage.value = Event("Error Occurred")
        }
    }
}
package com.switchsolutions.farmtohome.b2b

import androidx.lifecycle.*
import com.switchsolutions.farmtohome.b2b.callbacks.Event
import com.switchsolutions.farmtohome.b2b.roomdb.cart.CartEntityClass
import com.switchsolutions.farmtohome.b2b.roomdb.cart.CartRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CartViewModel(private val repository: CartRepository) : ViewModel() {
    private var isUpdateOrDelete = false
    private lateinit var subscriberToUpdateOrDelete: CartEntityClass
    val inputName = MutableLiveData<String>()
    val inputQuantity = MutableLiveData<String>()
    var cartStatus: MutableLiveData<Boolean> = MutableLiveData()


    var total = MutableLiveData<Int>()
    val totalAmount : LiveData<Int>
        get() = total


    private val statusMessage = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>>
        get() = statusMessage


    init {
        inputName.value = ""
        inputQuantity.value = ""
        cartStatus.value = false
        total.value = 0
    }

    fun setTotal(input:Int){
        total.value =(total.value)?.plus(input)
    }

    fun setTotalMinus(input:Int){
        total.value =(total.value)?.minus(input)
    }

    fun initUpdateAndDelete(product: CartEntityClass) {
        inputName.value = product.label
        inputQuantity.value = product.quantity
        isUpdateOrDelete = true
        subscriberToUpdateOrDelete = product
    }

    fun update(entityClass: CartEntityClass){
            updateCartProduct(entityClass)
    }

    fun saveOrUpdate(productName: String,productId: Int,  quantity: String, unit: String, imgUrl: String,  deliveryDate: String, price: String) {
        if (productName.isEmpty()) {
            statusMessage.value = Event("Please enter product's name")
        } else if (quantity.isEmpty()) {
            statusMessage.value = Event("Please enter product's email")
        }
        else {
            cartStatus.value = true
            insertProduct(CartEntityClass(0,imgUrl, productName,productId, quantity,unit, deliveryDate, price))
        }
    }

    private fun insertProduct(product: CartEntityClass) = viewModelScope.launch {
        val newRowId = repository.insert(product)
        if (newRowId > -1) {
            statusMessage.value = Event("Product Inserted Successfully $newRowId")
        } else {
            statusMessage.value = Event("Error Occurred")
        }
    }


    private fun updateCartProduct(product: CartEntityClass) = viewModelScope.launch {
        val noOfRows = repository.update(product)
        if (noOfRows > 0) {
            inputName.value = ""
            inputQuantity.value = ""
            isUpdateOrDelete = false
            statusMessage.value = Event("$noOfRows Row Updated Successfully")
        } else {
            statusMessage.value = Event("Error Occurred")
        }
    }

    fun getSavedProducts() = liveData {
        repository.product.collect {
            emit(it)
        }
    }

    fun clearAllOrDelete() {
        if (isUpdateOrDelete) {
            deleteProduct(subscriberToUpdateOrDelete)
        } else {
            clearAll()
        }
    }

     fun deleteProduct(product: CartEntityClass) = viewModelScope.launch {
        val noOfRowsDeleted = repository.delete(product)
        if (noOfRowsDeleted > 0) {
            statusMessage.value = Event("$noOfRowsDeleted Row Deleted Successfully")
        } else {
            statusMessage.value = Event("Error Occurred")
        }
    }

     fun clearAll() = viewModelScope.launch {
        val noOfRowsDeleted = repository.deleteAll()
        if (noOfRowsDeleted > 0) {
           // statusMessage.value = Event("$noOfRowsDeleted products Deleted Successfully")
        } else {
            cartStatus.value = false
            statusMessage.value = Event("Error Occurred")
        }
    }

    fun getCartItemCount() : Int {
        return repository.getItemCount()


    }
}
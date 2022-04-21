package com.switchsolutions.farmtohome.b2b.presentation.cart.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.switchsolutions.farmtohome.b2b.AppLauncher
import com.switchsolutions.farmtohome.b2b.CartViewModel
import com.switchsolutions.farmtohome.b2b.CartViewModelFactory
import com.switchsolutions.farmtohome.b2b.R
import com.switchsolutions.farmtohome.b2b.databinding.CartItemListAdapterBinding
import com.switchsolutions.farmtohome.b2b.presentation.cart.ui.CartFragment.Companion.cartViewModel
import com.switchsolutions.farmtohome.b2b.presentation.cart.ui.CartFragment.Companion.productName
import com.switchsolutions.farmtohome.b2b.presentation.login.ui.LoginActivity
import com.switchsolutions.farmtohome.b2b.roomdb.cart.CartDatabase
import com.switchsolutions.farmtohome.b2b.roomdb.cart.CartEntityClass
import com.switchsolutions.farmtohome.b2b.roomdb.cart.CartRepository
import com.switchsolutions.farmtohome.b2b.utils.SharedPrefUtils

class MyCartRecyclerViewAdapter( private val clickListener: (CartEntityClass) -> Unit) :
    RecyclerView.Adapter<MyViewHolder>() {
    private val subscribersList = ArrayList<CartEntityClass>()
    private var quantity = 0
    private lateinit var viewContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        viewContext = parent.context
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: CartItemListAdapterBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.cart_item_list_adapter, parent, false)
        return MyViewHolder(binding,  viewContext)
    }

    override fun getItemCount(): Int {
        return subscribersList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(subscribersList[position],position, clickListener)
    }
    fun setList(subscribers: List<CartEntityClass>) {
        subscribersList.clear()
        // productPrice.clear()
        //CartFragment.productQuantity.clear()
        subscribersList.addAll(subscribers)
        productName = subscribers.map {it.copy()} as java.util.ArrayList<CartEntityClass>

    }



}
class MyViewHolder( val binding: CartItemListAdapterBinding, var context: Context) : RecyclerView.ViewHolder(binding.root) {

    @SuppressLint("SetTextI18n")
    fun bind(product: CartEntityClass, position: Int, clickListener: (CartEntityClass) -> Unit) {
        lateinit var itemToUpdate: CartEntityClass
        var requestOptions = RequestOptions()
        requestOptions = requestOptions.transforms(CenterCrop(), RoundedCorners(16))
        binding.productNameCart.text = product.label
        binding.productPriceTvCart2.text = product.price+" / "
        if (product.price.isNotEmpty())
            binding.productPriceTvCart.text = (product.quantity.toInt() * product.price.toInt()).toString()
        if (product.price.isEmpty() || product.quantity.isEmpty())
            productName[position].price = (product.quantity.toInt()*1).toString()
        else
            productName[position].price = (product.quantity.toInt() * product.price.toInt()).toString()
        Glide.with(context)
            .load(product.imgUrl)
            .apply(requestOptions)
            .placeholder(R.drawable.logo_round)
            .into(binding.ivProduct)
        //CartFragment.productQuantity.add(product.quantity)
        binding.totalQtyCart.setText(product.quantity)
        binding.productUnitAndQuantityCart2.text = product.unit

        if (product.price.isEmpty()) {
            cartViewModel.setTotal(product.quantity.toInt()*1)
        }
        else
            cartViewModel.setTotal(product.quantity.toInt()*(product.price.toInt()))

        binding.deleteProductImageCart.setOnClickListener {
            clickListener(product)
        }
//        binding.totalQtyCart.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {
//                if (binding.totalQtyCart.text.toString().isNotEmpty()) {
//                    if (productName.size > position) {
//                        productName[position].quantity = s.toString()
//                        product.quantity = s.toString()
//                        //productName[position].quantity = product.quantity
//                        if (product.price.isNotEmpty()) {
//                            // binding.productPriceTvCart.text = (product.quantity.toInt() * product.price.toInt()).toString()
//                            productName[position].price = (product.quantity.toInt() * product.price.toInt()).toString()
//                            binding.productPriceTvCart.text = productName[position].price
//                            //prod[position] = product.quantity
//                            //productPrice[position] = (product.quantity.toInt() * product.price.toInt()).toString()
//                        }
////                        if (product.price.isNotEmpty()) {
////                            val total = binding.productPriceTvCart.text.toString().toInt()
////                            binding.productPriceTvCart.text = (product.quantity.toInt() * product.price.toInt()).toString()
////                            cartViewModel.setTotalMinus(total)
////                            cartViewModel.setTotal(product.quantity.toInt() * product.price.toInt())
////                        }
//                    }
//                }
//            }
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//            }
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//            }
//        })
        binding.productQtyAddCart.setOnClickListener {
            product.quantity = product.quantity.toIntOrNull()?.plus(1)!!.toString()
            //product.quantity = product.quantity.toInt().plus(1).toString()
            //productName[position].quantity = product.quantity
            if (product.price.isNotEmpty()) {
                // binding.productPriceTvCart.text = (product.quantity.toInt() * product.price.toInt()).toString()
                productName[position].quantity = product.quantity
                productName[position].price = (product.quantity.toInt() * product.price.toInt()).toString()
                binding.productPriceTvCart.text = productName[position].price
                //prod[position] = product.quantity
                //productPrice[position] = (product.quantity.toInt() * product.price.toInt()).toString()
            }
            // productName[position].quantity = product.quantity
            binding.totalQtyCart.setText(product.quantity)
        }

        binding.productQtyMinusCart.setOnClickListener {
            if ((product.quantity.toInt())> 1){
                //quantity = product.quantity.toIntOrNull()?.minus(1)!!
                product.quantity = product.quantity.toInt().minus(1).toString()
                if (product.price.isNotEmpty()) {
                    // binding.productPriceTvCart.text = (product.quantity.toInt() * product.price.toInt()).toString()
                        productName[position].quantity = product.quantity
                    productName[position].price = (product.quantity.toInt() * product.price.toInt()).toString()
                    binding.productPriceTvCart.text = productName[position].price
                    //prod[position] = product.quantity
                    //productPrice[position] = (product.quantity.toInt() * product.price.toInt()).toString()
                }
                // productName[position].quantity = product.quantity
                binding.totalQtyCart.setText(product.quantity)

            }
        }
    }
}
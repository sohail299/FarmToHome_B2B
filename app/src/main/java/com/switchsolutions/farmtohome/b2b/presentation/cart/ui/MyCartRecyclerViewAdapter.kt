package com.switchsolutions.farmtohome.b2b.presentation.cart.ui

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.switchsolutions.farmtohome.b2b.R
import com.switchsolutions.farmtohome.b2b.databinding.CartItemListAdapterBinding
import com.switchsolutions.farmtohome.b2b.roomdb.cart.CartEntityClass

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
        return MyViewHolder(binding, quantity, viewContext)
    }

    override fun getItemCount(): Int {
        return subscribersList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(subscribersList[position],position, clickListener)
    }

    fun setList(subscribers: List<CartEntityClass>) {
        subscribersList.clear()
        CartFragment.productQuantity.clear()
        subscribersList.addAll(subscribers)
    }
}
class MyViewHolder( val binding: CartItemListAdapterBinding, var quantity: Int, var context: Context) : RecyclerView.ViewHolder(binding.root) {

    fun bind(product: CartEntityClass, position: Int, clickListener: (CartEntityClass) -> Unit) {
        var requestOptions = RequestOptions()
        requestOptions = requestOptions.transforms(CenterCrop(), RoundedCorners(16))
        binding.productNameCart.text = product.label
        if (product.price.isNotEmpty())
        binding.productPriceTvCart.text = (product.quantity.toInt()*product.price.toInt()).toString() + " Rs"
        Glide.with(context)
            .load(product.imgUrl)
            .apply(requestOptions)
            .placeholder(R.drawable.logo_round)
            .into(binding.ivProduct)
        CartFragment.productQuantity.add(product.quantity)
        binding.totalQtyCart.setText(product.quantity)
        binding.productUnitAndQuantityCart2.text = product.unit

        binding.deleteProductImageCart.setOnClickListener {
            clickListener(product)
        }
        binding.totalQtyCart.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (binding.totalQtyCart.text.toString().isNotEmpty()) {
                    if (CartFragment.productQuantity.size > position) {
                        CartFragment.productQuantity[position] = s.toString()
                        product.quantity = s.toString()
                        if (product.price.isNotEmpty())
                        binding.productPriceTvCart.text = (product.quantity.toInt()*product.price.toInt()).toString() + " Rs"
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.totalQtyCart.text.toString().isNotEmpty()) {
                    if (CartFragment.productQuantity.size > position) {
                        CartFragment.productQuantity[position] = s.toString()
                        product.quantity = s.toString()
                        if (product.price.isNotEmpty())
                        binding.productPriceTvCart.text = (product.quantity.toInt()*product.price.toInt()).toString() + " Rs"
                    }

                }
            }
        })
        binding.productQtyAddCart.setOnClickListener {
                quantity = product.quantity.toIntOrNull()?.plus(1)!!
                product.quantity = product.quantity.toIntOrNull()?.plus(1).toString()
                CartFragment.productQuantity[position] = quantity.toString()
                binding.totalQtyCart.setText(quantity.toString())



        }
        binding.productQtyMinusCart.setOnClickListener {
            if ((product.quantity.toIntOrNull())!! > 1){
                quantity = product.quantity.toIntOrNull()?.minus(1)!!
                product.quantity = product.quantity.toIntOrNull()?.minus(1).toString()
            CartFragment.productQuantity[position] = quantity.toString()
            binding.totalQtyCart.setText(quantity.toString())

        }
        }
    }
}
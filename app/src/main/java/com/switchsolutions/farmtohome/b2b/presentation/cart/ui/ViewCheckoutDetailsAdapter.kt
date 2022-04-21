package com.switchsolutions.farmtohome.b2b.presentation.cart.ui

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.switchsolutions.farmtohome.b2b.R
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.data.response.singleorder.OrderProductsData
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.ui.DashboardFragment
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.viewmodel.DashboardViewModel
import com.switchsolutions.farmtohome.b2b.roomdb.cart.CartEntityClass
import kotlinx.coroutines.NonDisposableHandle.parent

class ViewCheckoutDetailsAdapter(private var product: ArrayList<CartEntityClass>,
                                 private var productStoredData: ArrayList<CartEntityClass>,
                                 private  var onClickListener: View.OnClickListener):
    RecyclerView.Adapter<ViewCheckoutDetailsAdapter.ViewHolder>() {
    private lateinit var viewContext: Context
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ):  ViewHolder {
        viewContext = parent.context
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem: View = layoutInflater.inflate(R.layout.view_single_item_checkout, parent, false)
        return ViewHolder(listItem, product,  viewContext)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind( product[position],productStoredData[position], position)
        //holder.textViewCustomerName.text = listdata[position].label
    }

    override fun getItemCount(): Int {
        return product.size
    }

    private fun openOrderDetail (){

    }
    private fun editOrder(){

    }

    class ViewHolder(itemView: View, product: ArrayList<CartEntityClass>,
                     var viewContext: Context) : RecyclerView.ViewHolder(itemView) {

        var textViewCustomerName: TextView = itemView.findViewById<View>(R.id.product_name_edit) as TextView
        var textViewProductUnit: TextView = itemView.findViewById<View>(R.id.product_unit_and_quantity_cart2) as TextView
        var Quantity: TextView = itemView.findViewById<View>(R.id.total_qty_cart_edit) as TextView
        var price: TextView = itemView.findViewById<View>(R.id.product_item_list_price) as TextView
        var perUnitPrice: TextView = itemView.findViewById<View>(R.id.product_price_tv_cart2_checkout) as TextView
        var iv: ImageView = itemView.findViewById<View>(R.id.iv_product_single_order) as ImageView

        fun bind( product: CartEntityClass, storedData: CartEntityClass,   position: Int){
            val requestOptions = RequestOptions().transforms(CenterCrop(), RoundedCorners(16))
           textViewCustomerName.text = product.label
            Quantity.text = product.quantity
            perUnitPrice.text = storedData.price+ " / "+product.unit
            textViewProductUnit.text = product.unit
            price.text = product.price
            Glide.with(viewContext)
                .load(product.imgUrl?: "")
                .apply(requestOptions)
                .placeholder(R.drawable.logo_round)
                .into(iv)
            //DashboardFragment.productQuantity.add(item.quantity!!)
        }
    }

    // RecyclerView recyclerView;
}
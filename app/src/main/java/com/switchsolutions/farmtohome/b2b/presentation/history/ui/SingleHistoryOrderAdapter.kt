package com.switchsolutions.farmtohome.b2b.presentation.history.ui

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
import com.switchsolutions.farmtohome.b2b.presentation.history.data.response.Data
import com.switchsolutions.farmtohome.b2b.presentation.history.data.response.Product
import kotlinx.coroutines.NonDisposableHandle.parent


class SingleHistoryOrderAdapter(private var viewModel: DashboardViewModel,
                             private var data: Data,
                             private var listdata: List<Product>,
                             private  var onClickListener: View.OnClickListener
) :
    RecyclerView.Adapter<SingleHistoryOrderAdapter.ViewHolder>() {
    private lateinit var viewContext: Context
    private var quantity = 0
    private var price = false
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        viewContext = parent.context
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem: View = layoutInflater.inflate(R.layout.view_single_order_item_list, parent, false)
        if (data.status == 4)
            price = true
        return ViewHolder(listItem, quantity, viewContext, price)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listdata[position], position)
        //holder.textViewCustomerName.text = listdata[position].label

    }

    override fun getItemCount(): Int {
        return listdata.size
    }

    private fun openOrderDetail (){

    }
    private fun editOrder(){

    }

    class ViewHolder(itemView: View, var quantity: Int, var viewContext: Context, var price: Boolean) : RecyclerView.ViewHolder(itemView) {

        var textViewCustomerName: TextView = itemView.findViewById<View>(R.id.product_name_edit) as TextView
        var textViewProductUnit: TextView = itemView.findViewById<View>(R.id.product_unit_and_quantity_cart2) as TextView
        var Quantity: TextView = itemView.findViewById<View>(R.id.total_qty_cart_edit) as TextView
        var iv: ImageView = itemView.findViewById<View>(R.id.iv_product_single_order) as ImageView
        var priceLayout: LinearLayout = itemView.findViewById<View>(R.id.layout_item_list_price) as LinearLayout
        var tvPrice: TextView = itemView.findViewById<View>(R.id.product_item_list_price) as TextView
        var tvUnit: TextView = itemView.findViewById<View>(R.id.product_price_unit) as TextView
        var productUnit: TextView = itemView.findViewById<View>(R.id.product_unit_and_quantity_cart2) as TextView

        fun bind(item: Product, position: Int){
            val requestOptions = RequestOptions().transforms(CenterCrop(), RoundedCorners(16))
            textViewCustomerName.text = item.label
            Quantity.text = item.quantity.toString()
            textViewProductUnit.text = item.unit
            productUnit.text = item.unit
            if (price){
                priceLayout.visibility = View.VISIBLE
                tvPrice.text = item.selling_price
                tvUnit.text = "Rs/${item.unit}"
            }
            Glide.with(viewContext)
                .load(item.imgUrl?: "")
                .apply(requestOptions)
                .placeholder(R.drawable.logo_round)
                .into(iv)
            //DashboardFragment.productQuantity.add(item.quantity!!)
        }
    }

    // RecyclerView recyclerView;
    init {
        this.listdata = listdata
    }
}
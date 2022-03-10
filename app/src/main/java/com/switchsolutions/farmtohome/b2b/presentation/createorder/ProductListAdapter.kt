package com.switchsolutions.farmtohome.b2b.presentation.createorder


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.switchsolutions.farmtohome.b2b.R
import com.switchsolutions.farmtohome.b2b.presentation.createorder.CreateRequestFragment.Companion.productDetails
import com.switchsolutions.farmtohome.b2b.presentation.createorder.data.response.Data
import java.util.*


class ProductListAdapter(
    private var listdata: ArrayList<Data>,
    private var listdataCopy: ArrayList<Data>,
    private  var onClickListener: View.OnClickListener
) :
    RecyclerView.Adapter<ProductListAdapter.ViewHolder>() {
    private lateinit var viewContext: Context
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        viewContext = parent.context
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem: View = layoutInflater.inflate(R.layout.product_list_items, parent, false)
        return ViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (listdata.isNotEmpty()){
        holder.tvProductName.text = listdata[position].label
            var requestOptions = RequestOptions()
            requestOptions = requestOptions.transforms(CenterCrop(), RoundedCorners(16))
        if (listdata[position].imgUrl!=null && listdata[position].imgUrl.isNotEmpty())
            Glide.with(viewContext)
                .load(listdata[position].imgUrl)
                .apply(requestOptions)
                .placeholder(R.drawable.logo_round)
                .into(holder.ivProductImg)
        holder.relativeLayout.setOnClickListener { view ->

            productDetails.showProductDetail(listdata[position])
//            Toast.makeText(
//                view.context,
//                "View Order " + myListData.description +"  "+position,
//                Toast.LENGTH_LONG
//            ).show()
        }
        //    holder.ivEditOrder.setOnClickListener {
        //   DashboardFragment.newInstance().triggerMainFragmentFunction(listdata[position])
        }
    }

    override fun getItemCount(): Int {
        return listdata.size
    }

    private fun openOrderDetail (){

    }
    private fun editOrder(){

    }
    @SuppressLint("NotifyDataSetChanged")
    fun filter(textString: String) {
        var text = textString
        listdata.clear()
        if (text.isEmpty()) {
            listdata.addAll(listdataCopy)
        } else {
            text = text.lowercase(Locale.getDefault())
            for (item in listdataCopy) {
                if (item.label.lowercase(Locale.getDefault()).contains(text)) {
                    listdata.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var relativeLayout: ConstraintLayout
        var ivProductImg: ImageView
        var tvProductName: TextView
        init {
            relativeLayout = itemView.findViewById<View>(R.id.constraint_layout_delivered) as ConstraintLayout
            ivProductImg = itemView.findViewById(R.id.iv_product) as ImageView
            tvProductName = itemView.findViewById(R.id.tv_product_name) as TextView
        }
    }

    // RecyclerView recyclerView;
    init {
        this.listdata = listdata
    }
}
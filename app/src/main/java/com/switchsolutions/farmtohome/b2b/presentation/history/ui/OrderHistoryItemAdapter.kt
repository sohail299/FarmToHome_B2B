package com.switchsolutions.farmtohome.b2b.presentation.history.ui

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.switchsolutions.farmtohome.b2b.R
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.ui.DashboardFragment.Companion.singleOrder
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.viewmodel.DashboardViewModel
import com.switchsolutions.farmtohome.b2b.presentation.history.data.response.Data
import com.switchsolutions.farmtohome.b2b.presentation.history.viewmodel.OrderHistoryViewModel


class OrderHistoryItemAdapter(private var viewModel: OrderHistoryViewModel,
                              private var listdata: List<Data>,
                              private  var onClickListener: View.OnClickListener
) :
    RecyclerView.Adapter<OrderHistoryItemAdapter.ViewHolder>() {
    private lateinit var viewContext: Context
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        viewContext = parent.context
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem: View = layoutInflater.inflate(R.layout.order_history_item_list_adapter, parent, false)
        return ViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvDeliveryDate.text = listdata[position].delivery_date
        holder.tvRequestId.text = listdata[position].id.toString()
        if (listdata[position].status == 1) {
            holder.orderStatusTv.text = "Ready"
            holder.orderStatusTv.setTextColor(ContextCompat.getColor(viewContext, R.color.ready_to_dispatch))
        }
        else if (listdata[position].status == 2) {
            holder.orderStatusTv.text = "Rejected"
            holder.orderStatusTv.setTextColor(ContextCompat.getColor(viewContext, R.color.rejected))
        }
        else if (listdata[position].status == 3) {
            holder.orderStatusTv.text = "Dispatched"
            holder.orderStatusTv.setTextColor(ContextCompat.getColor(viewContext, R.color.dispatched))
        }
        else if (listdata[position].status == 4) {
            holder.orderStatusTv.text = "Delivered"
            holder.orderStatusTv.setTextColor(ContextCompat.getColor(viewContext, R.color.delivered))
        }
        holder.relativeLayout.setOnClickListener {
            singleOrder.showOrderHistory(listdata[position])
//            Toast.makeText(
//                view.context,
//                "View Order " + myListData.description +"  "+position,
//                Toast.LENGTH_LONG
//            ).show()
        }



        //    holder.ivEditOrder.setOnClickListener {
        //   DashboardFragment.newInstance().triggerMainFragmentFunction(listdata[position])
    }

    override fun getItemCount(): Int {
        return listdata.size
    }

    private fun openOrderDetail (){

    }
    private fun editOrder(){

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var relativeLayout: ConstraintLayout
        var tvDeliveryDate: TextView
        var orderStatusTv: TextView
        var tvRequestId: TextView
        init {
            tvDeliveryDate = itemView.findViewById<View>(R.id.tv_delivery_date_history) as TextView
            tvRequestId = itemView.findViewById<View>(R.id.tv_request_id_history) as TextView
            orderStatusTv = itemView.findViewById<View>(R.id.order_status_tv) as TextView
            relativeLayout = itemView.findViewById<View>(R.id.constraint_layout_history) as ConstraintLayout

        }
    }

    // RecyclerView recyclerView;
    init {
        this.listdata = listdata
    }
}
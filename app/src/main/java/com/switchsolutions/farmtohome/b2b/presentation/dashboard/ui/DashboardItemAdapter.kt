package com.switchsolutions.farmtohome.b2b.presentation.dashboard.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.switchsolutions.farmtohome.b2b.R
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.data.response.Data
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.ui.DashboardFragment.Companion.singleOrder
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.viewmodel.DashboardViewModel


class DashboardItemAdapter(private var viewModel: DashboardViewModel,
                           private var listdata: List<Data>,
                           private  var onClickListener: View.OnClickListener
) :
    RecyclerView.Adapter<DashboardItemAdapter.ViewHolder>() {
    private lateinit var viewContext: Context
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        viewContext = parent.context
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem: View = layoutInflater.inflate(R.layout.dashboard_list_items, parent, false)
        return ViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvDeliveryDate.text = listdata[position].delivery_date
        holder.tvRequestId.text = listdata[position].id.toString()
        holder.relativeLayout.setOnClickListener {
            singleOrder.showOrderDetails(listdata[position].id!!)
            singleOrder.startObserverForSingleOrder()
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
        var tvRequestId: TextView
        init {
            tvDeliveryDate = itemView.findViewById<View>(R.id.tv_delivery_date) as TextView
            tvRequestId = itemView.findViewById<View>(R.id.tv_request_id) as TextView
            relativeLayout = itemView.findViewById<View>(R.id.constraint_layout) as ConstraintLayout

        }
    }

    // RecyclerView recyclerView;
    init {
        this.listdata = listdata
    }
}
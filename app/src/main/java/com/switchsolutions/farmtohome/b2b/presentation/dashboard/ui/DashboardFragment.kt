package com.switchsolutions.farmtohome.b2b.presentation.dashboard.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.viewmodel.DashboardViewModel
import com.switchsolutions.farmtohome.b2b.R
import com.switchsolutions.farmtohome.b2b.callbacks.HttpStatusCodes
import com.switchsolutions.farmtohome.b2b.databinding.DashboardFragmentBinding
import com.switchsolutions.farmtohome.b2b.interfaces.ShowOrderDetail
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.data.response.Data
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.data.response.OrdersResponseModel
import com.switchsolutions.farmtohome.b2b.utils.AnimateLayout
import com.switchsolutions.farmtohome.b2b.utils.NotificationUtil
import com.switchsolutions.farmtohome.b2b.utils.enums.Type

class DashboardFragment : Fragment() {
    companion object {
        const val TAG: String = "dispatchedFragment"
        var USER_STORED_CITY_ID = 1
        val dispatchedOrdersJson = JsonObject()
        var USER_ID = 1
        lateinit var singleOrder : ShowOrderDetail
    }
    private lateinit var viewModel: DashboardViewModel
    lateinit var binding: DashboardFragmentBinding
    private lateinit var progressBar: ProgressBar
    private val MY_PREFS_NAME = "FarmToHomeB2B"
    private lateinit var ordersResponseModel: OrdersResponseModel
    private lateinit var listData: List<Data>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val preferences = requireContext().getSharedPreferences(MY_PREFS_NAME, AppCompatActivity.MODE_PRIVATE)
        USER_STORED_CITY_ID = preferences.getInt("cityId", 1)
        USER_ID = preferences.getInt("User", 1)
        binding = DashboardFragmentBinding.inflate(layoutInflater)
        return binding.root
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        progressBar = binding.progressDashboard
        singleOrder = activity as ShowOrderDetail
        //singleOrder = activity as ShowOrderDetail
        dispatchedOrdersJson.addProperty("city_id", USER_STORED_CITY_ID)
        dispatchedOrdersJson.addProperty("status", 3)
        dispatchedOrdersJson.addProperty("created_by", USER_ID)
        viewModel.statusSingleOrderApi.observe(viewLifecycleOwner) { it ->
            it.getContentIfNotHandled()?.let {
                if (it){
                    progressBar.visibility = View.VISIBLE
                }
                else{
                    progressBar.visibility = View.GONE
                }
            }
        }
        startObservers()
        viewModel.startObserver(USER_STORED_CITY_ID, USER_ID)
        binding.rlDashboardRefresh.setOnRefreshListener {
            binding.rlDashboardRefresh.isRefreshing = false
        }
    }

    fun startObservers() {
        viewModel.callSignInApi.observe(viewLifecycleOwner, Observer {
            if (it!!) {
                progressBar.visibility = View.VISIBLE
            }
        })
        viewModel.apiResponseSuccess.observe(viewLifecycleOwner, Observer {
            if (progressBar.isVisible) progressBar.visibility = View.GONE
            ordersResponseModel = it
            listData = ordersResponseModel.data
//            val adapter = DashboardAdapter(orders, View.OnClickListener(){
//            })
            val adapter = DashboardItemAdapter(viewModel, listData, View.OnClickListener {
                // showEditOrderDialog()
            })
            binding.recyclerView.setHasFixedSize(true)
            binding.recyclerView.layoutManager = AnimateLayout(requireContext())
            binding.recyclerView.adapter = adapter
            //showEditOrderDialog()
        })
        viewModel.apiResponseFailure.observe(viewLifecycleOwner, Observer {
            if (progressBar.isVisible) progressBar.visibility = View.GONE
            if ((it?.statusCode == HttpStatusCodes.SC_UNAUTHORIZED) || (it?.statusCode == HttpStatusCodes.SC_NO_CONTENT)) {
                Toast.makeText(
                    requireContext(), "Unauthorized",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                NotificationUtil.showShortToast(requireContext(), it.message, Type.DANGER)
            }
        })

    }



}
package com.switchsolutions.farmtohome.b2b.presentation.history.ui

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
import com.switchsolutions.farmtohome.b2b.callbacks.HttpStatusCodes
import com.switchsolutions.farmtohome.b2b.databinding.OrderHistoryFragmentLayoutBinding
import com.switchsolutions.farmtohome.b2b.interfaces.ShowOrderDetail
import com.switchsolutions.farmtohome.b2b.presentation.history.data.response.Data
import com.switchsolutions.farmtohome.b2b.presentation.history.data.response.OrderHistoryResponseModel
import com.switchsolutions.farmtohome.b2b.presentation.history.viewmodel.OrderHistoryViewModel
import com.switchsolutions.farmtohome.b2b.utils.AnimateLayout


class OrderHistoryFragment : Fragment() {

    companion object {
        const val TAG: String = "historyFragment"
        var USER_STORED_CITY_ID = 1
        var requisitionId = 0
        val dispatchedOrdersJson = JsonObject()
        var USER_ID = 1
        fun newInstance() = OrderHistoryFragment()
        lateinit var singleOrder : ShowOrderDetail
    }

    private lateinit var viewModel: OrderHistoryViewModel
    lateinit var binding: OrderHistoryFragmentLayoutBinding
    private lateinit var progressBar: ProgressBar
    private val MY_PREFS_NAME = "FarmToHomeB2B"
    private lateinit var ordersResponseModel: OrderHistoryResponseModel
    private lateinit var listData: List<Data>



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val preferences = requireContext().getSharedPreferences(MY_PREFS_NAME, AppCompatActivity.MODE_PRIVATE)
        USER_STORED_CITY_ID = preferences.getInt("cityId", 1)
        USER_ID = preferences.getInt("User", 1)
        binding = OrderHistoryFragmentLayoutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[OrderHistoryViewModel::class.java]
        progressBar = binding.progressDashboardHistory
        singleOrder = activity as ShowOrderDetail
        //singleOrder = activity as ShowOrderDetail
        dispatchedOrdersJson.addProperty("city_id", USER_STORED_CITY_ID)
        dispatchedOrdersJson.addProperty("status", 3)
        dispatchedOrdersJson.addProperty("created_by", USER_ID)
        startObservers()
        viewModel.startObserver(USER_STORED_CITY_ID, USER_ID)
        // TODO: Use the ViewModel

        binding.rlDashboardRefreshHistory.setOnRefreshListener {
            binding.rlDashboardRefreshHistory.isRefreshing = false
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
            val adapter = OrderHistoryItemAdapter(viewModel, listData, View.OnClickListener {
                // showEditOrderDialog()
            })
            binding.recyclerViewHistory.setHasFixedSize(true)
            binding.recyclerViewHistory.layoutManager = AnimateLayout(requireContext())
            binding.recyclerViewHistory.adapter = adapter
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
//                Toast.makeText(
//                    requireContext(), "An Error Occurred",
//                    Toast.LENGTH_LONG
//                ).show()
                //NotificationUtil.showShortToast(requireContext(), requireContext().getString(R.string.error_occurred), Type.DANGER)
            }
        })

    }
}
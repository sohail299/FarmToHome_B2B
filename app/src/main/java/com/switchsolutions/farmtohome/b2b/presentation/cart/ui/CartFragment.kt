package com.switchsolutions.farmtohome.b2b.presentation.cart.ui

import android.animation.ValueAnimator
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.switchsolutions.farmtohome.b2b.CartViewModel
import com.switchsolutions.farmtohome.b2b.CartViewModelFactory
import com.switchsolutions.farmtohome.b2b.R
import com.switchsolutions.farmtohome.b2b.callbacks.HttpStatusCodes
import com.switchsolutions.farmtohome.b2b.databinding.CartFragmentBinding
import com.switchsolutions.farmtohome.b2b.presentation.cart.response.OrderSubmissionResponseModel
import com.switchsolutions.farmtohome.b2b.presentation.cart.viewmodel.SubmitCartViewModel
import com.switchsolutions.farmtohome.b2b.roomdb.cart.CartDatabase
import com.switchsolutions.farmtohome.b2b.roomdb.cart.CartEntityClass
import com.switchsolutions.farmtohome.b2b.roomdb.cart.CartRepository
import com.switchsolutions.farmtohome.b2b.utils.NotificationUtil
import com.switchsolutions.farmtohome.b2b.utils.enums.Type

class CartFragment : Fragment() {
    companion object {
        const val TAG: String = "cartFragment"
        var item: ArrayList<String>? = ArrayList()
        val placeOrderJson = JsonObject()
        fun newInstance() = CartFragment()
        var productQuantity: ArrayList<String> = ArrayList()
    }
    private lateinit var cartViewModel: CartViewModel
    private lateinit var adapter: MyCartRecyclerViewAdapter
    lateinit var binding: CartFragmentBinding
    lateinit var cartDataList: List<CartEntityClass>
    lateinit var cartDataListCheck: List<CartEntityClass>
    lateinit var product: CartEntityClass

    private lateinit var submitViewModel: SubmitCartViewModel
    private lateinit var waitDialog: ProgressDialog
    private lateinit var submissionDataResponse: OrderSubmissionResponseModel
    var cityId: Int = 0
    var badgeCount: Int = 0
    private val MY_PREFS_NAME = "FarmToHomeBDO"
    var deliveryDate: String = ""
    var quantityCheck: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val prefs = requireContext().getSharedPreferences(MY_PREFS_NAME, AppCompatActivity.MODE_PRIVATE)
        cityId = prefs.getInt("cityId", 1)
        badgeCount = prefs.getInt("badgeCount", 0)
        deliveryDate =
            prefs.getString("customerDeliveryDate", "").toString() //0 is the default value.
        binding = CartFragmentBinding.inflate(layoutInflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        submitViewModel = ViewModelProvider(this).get(SubmitCartViewModel::class.java)

        val dao = CartDatabase.getInstance(requireContext()).cartDAO
        val repository = CartRepository(dao)
        val factory = CartViewModelFactory(repository)
        cartViewModel = ViewModelProvider(this, factory).get(CartViewModel::class.java)
        binding.myCartViewModel = cartViewModel
        binding.lifecycleOwner = this
        cartViewModel.message.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
               // Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        })
        binding.btnSubmitCart.setOnClickListener {
            if (productQuantity.isNotEmpty()){
                for ((index) in productQuantity.withIndex()){
                    if (productQuantity[index].toInt() == 0) {
                        Toast.makeText(
                            requireContext(),
                            "Quantity cannot be less than 1",
                            Toast.LENGTH_SHORT
                        ).show()
                        quantityCheck = false
                        break
                    }
                    else
                    {
                        quantityCheck = true
                    }
                }
            }
            if (cartDataListCheck.isNotEmpty()) {
                val productsArray = JsonArray()
//                placeOrderJson.addProperty("customer_id", 9241)
//                placeOrderJson.addProperty("delivery_date", "2022-03-12")
//                placeOrderJson.addProperty("city_id", 1)
//                productObject.addProperty("name", "Potato")
//                productObject.addProperty("value", 40)
//                productObject.addProperty("is_removed", 0)
//                productObject.addProperty("name", "Tomato")
//                productObject.addProperty("value", 50)
//                productObject.addProperty("is_removed", 0)
//                productsArray.add(productObject)
//                placeOrderJson.add("products", productsArray)
//                CreateBdoRequestService(requireContext(), placeOrderJson )
                if (cartDataList.isNotEmpty() && quantityCheck) {
                    placeOrderJson.addProperty("delivery_date", deliveryDate)
                    placeOrderJson.addProperty("city_id", cityId)
                    placeOrderJson.addProperty("comments", binding.etCustomerRemarks.text.toString())
                    for ((index) in cartDataList.withIndex()) {
                        product = cartDataList[index]
                        val productObject = JsonObject()
                        productObject.addProperty("name", cartDataList[index].id)
                        productObject.addProperty("value", productQuantity[index])
                        Log.i("ProductQuantity", product.quantity)
                        productObject.addProperty("is_removed", 0)
                        productsArray.add(productObject)
                    }
                    placeOrderJson.add("products", productsArray)
                    //CreateBdoRequestService(cartViewModel, this, requireContext(), placeOrderJson).callApi()
                    startObservers()
                    submitViewModel.startObserver()
                }
            }
            else {
                Toast.makeText(requireContext(), "Please add items to Cart before submitting order.", Toast.LENGTH_LONG).show()
            }
        }
        initRecyclerView()
    }
    fun startObservers() {
        submitViewModel.callCartSubmitApi.observe(viewLifecycleOwner, Observer {
            if (it!!) {
                waitDialog = ProgressDialog.show(requireContext(), "", "Submitting")
                waitDialog.setCancelable(true)
            }
        })
        submitViewModel.apiResponseSuccess.observe(viewLifecycleOwner, Observer {
            if (waitDialog.isShowing) waitDialog.dismiss()
            submissionDataResponse = it
//            val adapter = DashboardAdapter(orders, View.OnClickListener(){
//            })
            //showEditOrderDialog()
            productQuantity.clear()
            cartViewModel.clearAll()
            clearCartAfterSubmission()
            //showSubmissionSuccessMessage()
            initRecyclerView()
        })
        submitViewModel.apiResponseFailure.observe(viewLifecycleOwner, Observer {
            if (waitDialog.isShowing) waitDialog.dismiss()
            if ((it?.statusCode == HttpStatusCodes.SC_UNAUTHORIZED) || (it?.statusCode == HttpStatusCodes.SC_NO_CONTENT)) {
                Toast.makeText(
                    requireContext(), "Failed to Submit",
                    Toast.LENGTH_LONG
                ).show()
//                val builder = AlertDialog.Builder(context!!)
//                builder.setMessage(context?.getString(R.string.invalid_credentials))
//                        .setPositiveButton(context?.getString(R.string.ok)) { dialog, _ ->
//                            dialog.dismiss()
//                        }
//                builder.create().show()
            } else {
//                Toast.makeText(
//                    requireContext(), "An Error Occurred",
//                    Toast.LENGTH_LONG
//                ).show()
                if (waitDialog.isShowing) waitDialog.dismiss()
                NotificationUtil.showShortToast(context!!, context!!.getString(R.string.error_occurred), Type.DANGER)
            }
        })
    }
    private fun clearCartAfterSubmission() {
        binding.btnSubmitCart.visibility = View.GONE
        binding.cvCartCustomerDetails.visibility = View.GONE
        binding.llCustomerRemarks.visibility = View.GONE
        val editor = requireContext().getSharedPreferences(MY_PREFS_NAME, AppCompatActivity.MODE_PRIVATE).edit()
        editor.putInt("badgeCount", 0)
        editor.putInt("customerId", 0) //0 is the default value.
        editor.putString("customerName", "")//"" is the default value.
        editor.putString("customerDeliveryDate", "" )
        editor.apply()
    }

    fun initRecyclerView() {
        binding.tvDeliveryDateCart.text = deliveryDate
        //binding = CreateCartFragmentBinding.inflate(layoutInflater)
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = MyCartRecyclerViewAdapter { selectedItem: CartEntityClass ->
            listItemClicked(selectedItem)
        }
        binding.cartRecyclerView.adapter = adapter
        displaySubscribersList()
    }


    private fun displaySubscribersList() {
        cartViewModel.getSavedProducts().observe(viewLifecycleOwner, Observer {
            cartDataListCheck = it
            if (it.isNotEmpty()) {
                cartDataList = it
                binding.btnSubmitCart.visibility = View.VISIBLE
                binding.cvCartCustomerDetails.visibility = View.VISIBLE
                binding.llCustomerRemarks.visibility = View.VISIBLE
                adapter.setList(it)
                adapter.notifyDataSetChanged()
            } else if (it.isNotEmpty() && it != null) {
                binding.btnSubmitCart.visibility = View.VISIBLE
                binding.cvCartCustomerDetails.visibility = View.VISIBLE
                binding.llCustomerRemarks.visibility = View.VISIBLE
                adapter.setList(it)
                adapter.notifyDataSetChanged()
            }
            else {
                clearCartAfterSubmission()
                binding.btnSubmitCart.visibility = View.GONE
                binding.cvCartCustomerDetails.visibility = View.GONE
                binding.llCustomerRemarks.visibility = View.GONE
            }
        })
    }

    private fun listItemClicked(product: CartEntityClass) {
        Log.i("cartItemSize", cartDataList.size.toString())
        if (cartDataList.isNotEmpty()) {
            cartViewModel.deleteProduct(product)
            badgeCount -= 1
            val editor =
                requireContext().getSharedPreferences(MY_PREFS_NAME, AppCompatActivity.MODE_PRIVATE)
                    .edit()
            editor.putInt("badgeCount", badgeCount)
            editor.apply()
        }
    }

}

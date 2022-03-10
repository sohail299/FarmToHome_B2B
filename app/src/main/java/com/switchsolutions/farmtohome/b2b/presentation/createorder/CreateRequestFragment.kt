package com.switchsolutions.farmtohome.b2b.presentation.createorder

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.switchsolutions.farmtohome.b2b.*
import com.switchsolutions.farmtohome.b2b.databinding.CreateRequestFragmentBinding
import com.switchsolutions.farmtohome.b2b.interfaces.CartBadge
import com.switchsolutions.farmtohome.b2b.interfaces.OpenProductDetail
import com.switchsolutions.farmtohome.b2b.presentation.createorder.data.request.CreateB2BRequestModel
import com.switchsolutions.farmtohome.b2b.presentation.createorder.data.response.Data
import com.switchsolutions.farmtohome.b2b.roomdb.cart.CartDatabase
import com.switchsolutions.farmtohome.b2b.roomdb.cart.CartEntityClass
import com.switchsolutions.farmtohome.b2b.roomdb.cart.CartRepository
import java.util.*

class CreateRequestFragment : Fragment() {
    companion object {
        const val TAG: String = "createFragment"
        fun newInstance() = CreateRequestFragment()
        var item: ArrayList<String>? = ArrayList()
        lateinit var binding : CreateRequestFragmentBinding
        lateinit var cartDataList: List<CartEntityClass>
        var productDataCopy: ArrayList<Data> = ArrayList()
        var productData: ArrayList<Data> = ArrayList()
        lateinit var productDetails : OpenProductDetail
       //  var customerNamesData: ArrayList<String> = ArrayList()

    }

    private lateinit var autoTvCustomers: AppCompatAutoCompleteTextView
    private lateinit var createB2BRequestModel : CreateB2BRequestModel
    private lateinit var cartVM : CartViewModel
    var deliveryDate : String = ""
    var productId : Int = 0
    var productImgUrl : String = ""
    var productName : String = ""
    var badgeCount: Int = 0
    var previousProduct: Boolean = false
    var productSelected: Boolean = false
    private lateinit var productVM: ProductViewModel
    var productUnit : String = ""
    lateinit var itemToUpdate : CartEntityClass
    private val MY_PREFS_NAME = "FarmToHomeB2B"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CreateRequestFragmentBinding.inflate(layoutInflater)
        binding.searchView.queryHint = "Search Product"
        val prefs = requireContext().getSharedPreferences(MY_PREFS_NAME, AppCompatActivity.MODE_PRIVATE)
        deliveryDate = prefs.getString("customerDeliveryDate", "" ).toString()
        badgeCount = prefs.getInt("badgeCount", 0)
        createB2BRequestModel = CreateB2BRequestModel( null, "", null)
        binding.llDeliveryDate.setOnClickListener {
        //datePick()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dao = CartDatabase.getInstance(requireContext()).cartDAO
        val repository = CartRepository(dao)
        val factory = CartViewModelFactory(repository)
        cartVM = ViewModelProvider(this, factory)[CartViewModel::class.java]
        productDetails = activity as OpenProductDetail
        productData.clear()
        productDataCopy.clear()
        productDataCopy.addAll(MainActivity.productData)
        productData.addAll(MainActivity.productData)
        val adapter = ProductListAdapter( productData, productDataCopy, View.OnClickListener {
            // showEditOrderDialog()
        })
        binding.recyclerviewListProducts.setHasFixedSize(true)
        binding.recyclerviewListProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerviewListProducts.adapter = adapter
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String): Boolean {
                adapter.filter(query);
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter.filter(newText);
                return true
            }
        })
        cartVM.cartStatus.observe(viewLifecycleOwner, Observer {
            if (it!!)
            {
                badgeCount = 0
                val editor = requireContext().getSharedPreferences(MY_PREFS_NAME, AppCompatActivity.MODE_PRIVATE).edit()
                editor.putInt("badgeCount", badgeCount)
                editor.apply()
                val cb : CartBadge = activity as CartBadge
                cb.cartBadge(badgeCount)
            }
        })
        getCartItems()

    }


    private fun getCartItems() {
        cartVM.getSavedProducts().observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                cartDataList= it
            }
            else
                cartDataList = ArrayList()
        })
    }



}
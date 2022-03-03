package com.switchsolutions.farmtohome.b2b

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.switchsolutions.farmtohome.b2b.data.viewmodel.ProductsApiViewModel
import com.switchsolutions.farmtohome.b2b.databinding.ActivityMainBinding
import com.switchsolutions.farmtohome.b2b.presentation.cart.ui.CartFragment
import com.switchsolutions.farmtohome.b2b.presentation.createorder.CreateRequestFragment
import com.switchsolutions.farmtohome.b2b.presentation.createorder.data.response.Data
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.ui.DashboardFragment
import com.switchsolutions.farmtohome.b2b.presentation.login.viewmodel.LogInViewModel
import com.switchsolutions.farmtohome.b2b.roomdb.product.ProductDatabase
import com.switchsolutions.farmtohome.b2b.roomdb.product.ProductEntityClass
import com.switchsolutions.farmtohome.b2b.roomdb.product.ProductRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.sql.Array

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    companion object {
        var badgeCount: Int = 0
        var productName: ArrayList<String> = ArrayList()
        var productID: ArrayList<Int> = ArrayList()
        var productImgUrl: ArrayList<String> = ArrayList()
        var productUnit: ArrayList<String> = ArrayList()
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var productApiViewModel: ProductsApiViewModel
    private lateinit var productData: List<Data>

    private lateinit var productVM: ProductViewModel
    private var USER_STORED_CITY_ID: Int = 0
    private val MY_PREFS_NAME = "FarmToHomeB2B"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarMain)
        productApiViewModel = ViewModelProvider(this).get(ProductsApiViewModel::class.java)
        val prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE)
        USER_STORED_CITY_ID = prefs.getInt("cityId", 0)
        supportActionBar?.apply {
            // toolbar button click listener
            binding.cvToolbarLocation.setOnClickListener {
                // change toolbar title
            }
        }
        supportActionBar?.apply {
            // toolbar button click listener
        }
        redirectToMainFragment(savedInstanceState)
        startObservers()
        productApiViewModel.startObserver(USER_STORED_CITY_ID)
        //var badgeCount = cartVM.getCartItemCount()
        val badge = binding.bottomNavigation.getOrCreateBadge(R.id.item3)
        badge.isVisible = true
        badge.number = badgeCount
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item0 -> {
                    replaceFragment(DashboardFragment(), DashboardFragment.TAG)
                    title = "Processing"
                    true
                }
                R.id.item2 -> {
                    replaceFragment(CreateRequestFragment(), CreateRequestFragment.TAG)
                    title = "Create Order"
                    true
                }
                R.id.item3 -> {
                    replaceFragment(CartFragment(), CartFragment.TAG)
                    title = "Cart"
                    true
                }
                else -> false
            }
        }
//        binding.bottomBar.onItemSelected = {
//            binding.bottomBar
//            Log.i("Bottom Navigation", "$it selected")
//            if (it == 0) {
////                val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
////                toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
//                replaceFragment(DashboardFragment(), DashboardFragment.TAG)
//                title = "Processing"
//            } else if (it == 1) {
//                replaceFragment(DispatchFragment(), DispatchFragment.TAG)
//                title = "Dispatch"
//            } else if (it == 2) {
//                Log.i("ProductIdList", customerNamesData.size.toString())
//                replaceFragment(CreateRequestFragment(), CreateRequestFragment.TAG)
//                title= getString(R.string.create_request)
//
//            } else if (it == 3) {
//                replaceFragment(CartFragment(), CartFragment.TAG)
//                title= "Cart"
//            } else if (it == 4) {
//                replaceFragment(DeliveredFragment(), DeliveredFragment.TAG)
//                title="Delivered"
//            }
//        }
//        binding.bottomBar.onItemReselected = {
//            Log.i("Bottom Navigation", "$it Re-selected")
//        }
    }

    private fun startObservers() {
        productApiViewModel.statusProductResponseSuccess.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                if (it.data.isNotEmpty()) {
                    productData = it.data
                    updateAutofillProducts() // Save products response to local database
                    getProductsNames() // Get products data from local database
                } else {
                    productData = ArrayList()
                }
            }
        }
        productApiViewModel.statusProductResponseFailure.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                productData = ArrayList()
            }
        }
    }

    private fun updateAutofillProducts() {
        val daoProduct = ProductDatabase.getInstance(this).productDao
        val repositoryProduct = ProductRepository(daoProduct)
        val factoryProduct = ProductViewModelFactory(repositoryProduct)
        productVM = ViewModelProvider(this, factoryProduct)[ProductViewModel::class.java]
        if (productData.isNotEmpty()) {
            productVM.clearAll()
        }
        //saving products data from api response to ROOM database for later use
        for (element in productData) {
            productVM.insertProduct(
                ProductEntityClass(
                    0,
                    (element.imgUrl) ?: "",
                    element.label,
                    element.site_product_id,
                    element.unit
                )
            )
        }
    }

    private fun getProductsNames() {
        val daoProduct = ProductDatabase.getInstance(this).productDao
        val repositoryProduct = ProductRepository(daoProduct)
        val factoryProduct = ProductViewModelFactory(repositoryProduct)
        productVM = ViewModelProvider(this, factoryProduct)[ProductViewModel::class.java]
        productVM.getSavedProducts().observe(this, Observer {
            if (it.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    productName.clear()
                    productID.clear()
                    productUnit.clear()
                    productImgUrl.clear()
                    for ((index) in it.withIndex()) {
                        productName.add(it[index].label)
                        productID.add(it[index].site_product_id)
                        productUnit.add(it[index].unit)
                        productImgUrl.add(it[index].imgUrl)
                        Log.i(
                            "NullCheck",
                            it[index].imgUrl
                        ) // log to check if any image url is null
                    }
                }
            }
        })
    }

    private fun redirectToMainFragment(bundle: Bundle?) {
        if (findViewById<View?>(R.id.fragment_container) != null) {
            if (bundle != null) return
            clearBackStack()
            title = "Processing"
            val fragment = DashboardFragment()
            replaceFragment(fragment, DashboardFragment::class.java.simpleName)
        }
    }


    fun replaceFragment(fragment: Fragment, tag: String?) {
        clearBackStack()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment).commit()
    }

    private fun clearBackStack() {
        val fm = supportFragmentManager
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }


    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        //TODO
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}
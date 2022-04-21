package com.switchsolutions.farmtohome.b2b

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.switchsolutions.farmtohome.b2b.data.viewmodel.ProductsApiViewModel
import com.switchsolutions.farmtohome.b2b.databinding.ActivityMainBinding
import com.switchsolutions.farmtohome.b2b.databinding.ProductDetailDialogBinding
import com.switchsolutions.farmtohome.b2b.interfaces.CartBadge
import com.switchsolutions.farmtohome.b2b.interfaces.OpenProductDetail
import com.switchsolutions.farmtohome.b2b.interfaces.ReplaceFragment
import com.switchsolutions.farmtohome.b2b.interfaces.ShowOrderDetail
import com.switchsolutions.farmtohome.b2b.presentation.cart.ui.CartFragment
import com.switchsolutions.farmtohome.b2b.presentation.createorder.CreateRequestFragment
import com.switchsolutions.farmtohome.b2b.presentation.createorder.data.response.Data
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.data.response.singleorder.EditOrdersData
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.data.response.singleorder.OrderProductsData
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.data.response.singleorder.adapter.ViewSingleOrderAdapter
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.ui.DashboardFragment
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.viewmodel.DashboardViewModel
import com.switchsolutions.farmtohome.b2b.presentation.history.ui.OrderHistoryFragment
import com.switchsolutions.farmtohome.b2b.presentation.history.ui.SingleHistoryOrderAdapter
import com.switchsolutions.farmtohome.b2b.presentation.profile.ui.ProfileFragment
import com.switchsolutions.farmtohome.b2b.roomdb.cart.CartDatabase
import com.switchsolutions.farmtohome.b2b.roomdb.cart.CartEntityClass
import com.switchsolutions.farmtohome.b2b.roomdb.cart.CartRepository
import com.switchsolutions.farmtohome.b2b.roomdb.product.ProductDatabase
import com.switchsolutions.farmtohome.b2b.roomdb.product.ProductEntityClass
import com.switchsolutions.farmtohome.b2b.roomdb.product.ProductRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, OpenProductDetail,
    ShowOrderDetail, CartBadge, ReplaceFragment {
    companion object {
        var badgeCount: Int = 0
        var token: String = ""
        var productName: ArrayList<String> = ArrayList()
        var productID: ArrayList<Int> = ArrayList()
        var productImgUrl: ArrayList<String> = ArrayList()
        var productUnitArray: ArrayList<String> = ArrayList()
        var productData: List<Data> = ArrayList()
        var previousProduct: Boolean = false
        lateinit var cartDataList: List<CartEntityClass>
        var USER_STORED_CITY_ID: Int = 0
        var customerId: Int = 0

    }

    private lateinit var frontAnim: AnimatorSet
    private lateinit var backAnim: AnimatorSet


    private lateinit var binding: ActivityMainBinding
    private lateinit var progressBar: ProgressBar
    private lateinit var productApiViewModel: ProductsApiViewModel
    private lateinit var productVM: ProductViewModel
    private lateinit var cartVM: CartViewModel

    private var customerId: Int = 0
    private val MY_PREFS_NAME = "FarmToHomeB2B"


    lateinit var itemToUpdate: CartEntityClass
    var quantity = 1
    var total = 0
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var orderData: EditOrdersData
    private var orderProducts: ArrayList<OrderProductsData> = ArrayList()
    private lateinit var animation: Animation
    private lateinit var tvTitle: TextView
    private var doubleBackToExitPressedOnce = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressBar = binding.progressMain
        binding.bottomNavigation.background = null
        binding.bottomNavigation.menu.getItem(2).isEnabled = false
        setSupportActionBar(binding.toolbarMain)
        tvTitle = binding.toolbarTitleTv
        animation = AnimationUtils.loadAnimation(applicationContext, R.anim.toolbar_title_slide)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        tvTitle.text = "Processing Orders"
        tvTitle.startAnimation(animation)
        productApiViewModel = ViewModelProvider(this)[ProductsApiViewModel::class.java]
        dashboardViewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        val dao = CartDatabase.getInstance(this).cartDAO
        val repository = CartRepository(dao)
        val factory = CartViewModelFactory(repository)
        cartVM = ViewModelProvider(this, factory)[CartViewModel::class.java]
        val prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE)
        USER_STORED_CITY_ID = prefs.getInt("cityId", 0)
        customerId = prefs.getInt("User", 0)
        badgeCount = prefs.getInt("badgeCount", 0)
        token = prefs.getString("accessToken", "").toString()
        // Now we will set the front animation
        frontAnim =
            AnimatorInflater.loadAnimator(applicationContext, R.animator.flip_out) as AnimatorSet
        backAnim =
            AnimatorInflater.loadAnimator(applicationContext, R.animator.flip_in) as AnimatorSet

        redirectToMainFragment(savedInstanceState)
        startObservers()
        CoroutineScope(Dispatchers.Main).launch {
            getCartItems()
        }
        productApiViewModel.startObserver(USER_STORED_CITY_ID, customerId)
        //var badgeCount = cartVM.getCartItemCount()
        val badge = binding.bottomNavigation.getOrCreateBadge(R.id.item3)
        badge.isVisible = true
        badge.number = badgeCount
        binding.bottomNavigation.setOnItemReselectedListener {
        }
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item0 -> {
                    replaceFragment(DashboardFragment(), DashboardFragment.TAG)
                    tvTitle.text = "Processing Orders"
                    tvTitle.startAnimation(animation)
                    true
                }

                R.id.item1 -> {
                    replaceFragment(OrderHistoryFragment(), OrderHistoryFragment.TAG)
                    tvTitle.text = "History"
                    tvTitle.startAnimation(animation)
                    true
                }
//                R.id.item2 -> {
//                    replaceFragment(CreateRequestFragment(), CreateRequestFragment.TAG)
//                    title = "Create Order"
//                    true
//                }
                R.id.item3 -> {
                    replaceFragment(CartFragment(), CartFragment.TAG)
                    tvTitle.text = "Cart"
                    tvTitle.startAnimation(animation)
                    true
                }
                R.id.item4 -> {
                    replaceFragment(ProfileFragment(), ProfileFragment.TAG)
                    tvTitle.text = "Profile"
                    tvTitle.startAnimation(animation)
                    true
                }
                else -> false
            }
        }
        binding.fab.setOnClickListener {
            replaceFragment(CreateRequestFragment(), CreateRequestFragment.TAG)
            tvTitle.text = "Create Order"
            tvTitle.startAnimation(animation)
            binding.bottomNavigation.selectedItemId = R.id.placeholder
        }
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

    private fun getCartItems() {
        cartVM.getSavedProducts().observe(this, Observer {
            cartDataList = it.ifEmpty { ArrayList() }
        })
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
        productVM.getSavedProducts().observe(this) {
            if (it.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    productName.clear()
                    productID.clear()
                    productUnitArray.clear()
                    productImgUrl.clear()
                    for ((index) in it.withIndex()) {
                        productName.add(it[index].label)
                        productID.add(it[index].site_product_id)
                        productUnitArray.add(it[index].unit)
                        productImgUrl.add(it[index].imgUrl)
                        Log.i(
                            "NullCheck",
                            it[index].imgUrl
                        ) // log to check if any image url is null
                    }
                }
            }
        }
    }

    private fun redirectToMainFragment(bundle: Bundle?) {
        if (findViewById<View?>(R.id.fragment_container) != null) {
            if (bundle != null) return
            clearBackStack()
            tvTitle.text = "Processing"
            tvTitle.startAnimation(animation)
            val fragment = DashboardFragment()
            replaceFragment(fragment, DashboardFragment::class.java.simpleName)
        }
    }


    private fun replaceFragment(fragment: Fragment, tag: String?) {
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

    override fun showProductDetail(data: Data) {
        var requestOptions = RequestOptions()
        requestOptions = requestOptions.transforms(CenterCrop(), RoundedCorners(16))
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val binding: ProductDetailDialogBinding = ProductDetailDialogBinding.inflate(layoutInflater)
        // linearLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent))
        //  linearLayout.setBackgroundResource(Color.TRANSPARENT)
        binding.cardviewProductDetail.setCardBackgroundColor(Color.TRANSPARENT)
        binding.cardviewProductDetail.cardElevation = 0f
        builder.setView(binding.root)
        builder.setCancelable(true)
        val editOrderDialog: Dialog = builder.create()
        editOrderDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        editOrderDialog.show()
        binding.productNameProductDetailsLabel.text = data.label
        binding.productPriceProductDetails.text = "PKR ${data.price} / ${data.unit}"
        binding.totalAmountProductDetails.text = "PKR: ${data.price}"

        binding.totalQtyProductDetails.setText(quantity.toString())
        Glide.with(this)
            .load(data.imgUrl)
            .apply(requestOptions)
            .placeholder(R.drawable.logo_round)
            .into(binding.productImageProductDetails)
//        binding.btnCloseProductDetailDialog.setOnClickListener {
//            editOrderDialog.dismiss()
//            quantity = 1
//        }
        binding.totalQtyProductDetails.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (binding.totalQtyProductDetails.text.toString().isNotEmpty()) {
                    quantity = s.toString().toInt()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.totalQtyProductDetails.text.toString().isNotEmpty()) {
                    quantity = s.toString().toInt()
                    binding.totalAmountProductDetails.text = "PKR: ${data.price.toInt()*quantity}"
                }
            }
        })
        binding.addToCartButtonProductDetails.setOnClickListener {
            Log.i("cartItemSize", cartDataList.size.toString()+"MainActivity")
            if (binding.totalQtyProductDetails.text.isEmpty() || (binding.totalQtyProductDetails.text.toString()
                    .toIntOrNull())!! <= 0
            ) {
                Toast.makeText(this, "Quantity should not be 0", Toast.LENGTH_SHORT).show()

            }
            else if (cartDataList != null && cartDataList.isNotEmpty()) {
                for ((index) in cartDataList.withIndex()) {
                    if (data.site_product_id == cartDataList[index].site_product_id) {
                        previousProduct = true
                        itemToUpdate = cartDataList[index]
                        itemToUpdate.quantity = (itemToUpdate.quantity.toIntOrNull()
                            ?.plus(
                                binding.totalQtyProductDetails.text.toString().toIntOrNull()!!
                            )).toString()
                        break
                    }
                }
                if (previousProduct) {
                    cartVM.update(
                        itemToUpdate
                    )
                    previousProduct = false
                    quantity = 1
                    editOrderDialog.dismiss()
                } else {
                    cartVM.saveOrUpdate(
                        data.label,
                        data.site_product_id,
                        binding.totalQtyProductDetails.text.toString(),
                        data.unit,
                        data.imgUrl ?: "",
                        "123",
                        data.price
                    )
                    val editor =
                        this.getSharedPreferences(MY_PREFS_NAME, AppCompatActivity.MODE_PRIVATE)
                            .edit()
                    badgeCount += 1
                    editor.putInt("badgeCount", badgeCount)
                    editor.apply()
                    editOrderDialog.dismiss()
                    quantity = 1
                    cartBadge(badgeCount)
                }
                Toast.makeText(this, "Added To Cart", Toast.LENGTH_SHORT).show()
            } else {
                cartVM.saveOrUpdate(
                    data.label,
                    data.site_product_id,
                    binding.totalQtyProductDetails.text.toString(),
                    data.unit,
                    data.imgUrl ?: "",
                    "123",
                    data.price
                )
                badgeCount += 1
                val editor =
                    this.getSharedPreferences(MY_PREFS_NAME, AppCompatActivity.MODE_PRIVATE).edit()
                editor.putInt("badgeCount", badgeCount)
                editor.apply()
                cartBadge(badgeCount)
                Toast.makeText(this, "Added To Cart", Toast.LENGTH_SHORT).show()
                editOrderDialog.dismiss()
                quantity = 1
            }
        }
        binding.productQtyAddProductDetails.setOnClickListener {
            quantity = quantity.plus(1)
            binding.totalQtyProductDetails.setText(quantity.toString())

        }
        binding.productQtyMinusProductDetails.setOnClickListener {
            if (quantity > 1) {
                quantity = quantity.minus(1)
                binding.totalQtyProductDetails.setText(quantity.toString())
            }
            else
                Toast.makeText(this, "Quantity should not be less than 1", Toast.LENGTH_SHORT).show()
        }

        editOrderDialog.setOnDismissListener {
            quantity = 1
        }
    }

    override fun showOrderDetails(reqId: Int) {
        dashboardViewModel.startSingleOrderObserver(reqId)
    }

    override fun startObserverForSingleOrder() {
        dashboardViewModel.statusSingleOrderApi.observe(this){it ->
            it.getContentIfNotHandled()?.let {
                if (it) {
                    progressBar.visibility = View.VISIBLE
                    window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }
                else {
                    progressBar.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }
            }
        }
        dashboardViewModel.statusSingleOrderSuccess.observe(this) { it ->
            it.getContentIfNotHandled()?.let {
                orderProducts.clear()
                orderData = it.data
                orderProducts.addAll(it.products)
                showSingleOrderDialog(orderProducts, orderData)
            }
        }
    }
    override fun onBackPressed() {
        progressBar.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        if (!doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        replaceFragment(DashboardFragment(), DashboardFragment.TAG)
        tvTitle.text = "Processing Orders"
        tvTitle.startAnimation(animation)
        this.doubleBackToExitPressedOnce = false
        Handler(Looper.getMainLooper()).postDelayed(Runnable { doubleBackToExitPressedOnce = true }, 2000)
    }
    override fun showOrderHistory(data: com.switchsolutions.farmtohome.b2b.presentation.history.data.response.Data) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater: LayoutInflater =
            this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogLayout: View = inflater.inflate(R.layout.custom_single_product_dialog, null)
        val myCardView: CardView = dialogLayout.findViewById(R.id.cv_view_order)
        val btnCloseDialog: ImageButton = dialogLayout.findViewById(R.id.cancel_product_image_cart_view)
        val btnOk: Button = dialogLayout.findViewById(R.id.btn_ok_single_order)
        val delivDate: TextView = dialogLayout.findViewById(R.id.single_order_delivery_date)
        val grandTotalLayout: LinearLayout = dialogLayout.findViewById(R.id.ll_grand_total)
        val rejectedCommentsEt: TextView = dialogLayout.findViewById(R.id.et_rejected_comments)
        val recyclerView: RecyclerView = dialogLayout.findViewById(R.id.rv_edit_item_list_view)
        val commentsLayout: LinearLayout = dialogLayout.findViewById(R.id.layout_comments)
        if (data.status == 2){
            commentsLayout.visibility = View.VISIBLE
            rejectedCommentsEt.text = data.rejected_comments.toString()
        }
        val adapter = SingleHistoryOrderAdapter(dashboardViewModel, data, data.products, View.OnClickListener {
            //showEditOrderDialog()
        })
        grandTotalLayout.visibility = View.GONE

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
        // linearLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent))
        //  linearLayout.setBackgroundResource(Color.TRANSPARENT)
        myCardView.setCardBackgroundColor(Color.TRANSPARENT)
        myCardView.cardElevation = 0f
        builder.setView(dialogLayout)
        builder.setCancelable(true)
        val editOrderDialog: Dialog = builder.create()
        editOrderDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        editOrderDialog.show()
        delivDate.text = data.delivery_date
        // Create an ArrayAdapter using a simple spinner layout and languages array
        // Set layout to use when the list of choices appear
        // Set Adapter to Spinner

        btnOk.setOnClickListener {
            editOrderDialog.dismiss()
        }
        btnCloseDialog.setOnClickListener {
            //DashboardFragment.dialoClicked = false
            editOrderDialog.dismiss()
        }
    }
    fun showSingleOrderDialog(products: ArrayList<OrderProductsData>, data: EditOrdersData) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater: LayoutInflater =
            this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogLayout: View = inflater.inflate(R.layout.custom_single_product_dialog, null)
        val myCardView: CardView = dialogLayout.findViewById(R.id.cv_view_order)
        val btnOk: Button = dialogLayout.findViewById(R.id.btn_ok_single_order)
        val btnCloseDialog: ImageButton = dialogLayout.findViewById(R.id.cancel_product_image_cart_view)
        val delivDate: TextView = dialogLayout.findViewById(R.id.single_order_delivery_date)
        val total: TextView = dialogLayout.findViewById(R.id.single_order_total_amount)
        val recyclerView: RecyclerView = dialogLayout.findViewById(R.id.rv_edit_item_list_view)
        val adapter = ViewSingleOrderAdapter(dashboardViewModel, products, View.OnClickListener {
            //showEditOrderDialog()
        })
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
        var grandTotal = 0
        for ((index) in products.withIndex()){
            grandTotal += ((products[index].selling_price_b2b)!!.toInt() * ((products[index].quantity))!!.toInt())
        }
        total.text = grandTotal.toString() + " Rs"

        // linearLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent))
        //  linearLayout.setBackgroundResource(Color.TRANSPARENT)
        myCardView.setCardBackgroundColor(Color.TRANSPARENT)
        myCardView.cardElevation = 0f
        builder.setView(dialogLayout)
        builder.setCancelable(true)
        val editOrderDialog: Dialog = builder.create()
        editOrderDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        editOrderDialog.show()
        delivDate.text = data.delivery_date
        // Create an ArrayAdapter using a simple spinner layout and languages array
        // Set layout to use when the list of choices appear
        // Set Adapter to Spinner

        btnOk.setOnClickListener {
            editOrderDialog.dismiss()
        }
        btnCloseDialog.setOnClickListener {
            //DashboardFragment.dialoClicked = false
            editOrderDialog.dismiss()
        }
    }

    override fun cartBadge(count: Int) {
        val badge = binding.bottomNavigation.getOrCreateBadge(R.id.item3)
        badge.isVisible = true
        badge.number = count
    }

    override fun replaceWithMainFragment(fragment: Fragment?) {
        clearBackStack()
        binding.bottomNavigation.selectedItemId = R.id.item0
    }

}
package com.switchsolutions.farmtohome.b2b.presentation.cart.ui

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.switchsolutions.farmtohome.b2b.CartViewModel
import com.switchsolutions.farmtohome.b2b.CartViewModelFactory
import com.switchsolutions.farmtohome.b2b.MainActivity.Companion.badgeCount
import com.switchsolutions.farmtohome.b2b.MainActivity.Companion.cartDataList
import com.switchsolutions.farmtohome.b2b.R
import com.switchsolutions.farmtohome.b2b.callbacks.HttpStatusCodes
import com.switchsolutions.farmtohome.b2b.databinding.CartFragmentBinding
import com.switchsolutions.farmtohome.b2b.interfaces.CartBadge
import com.switchsolutions.farmtohome.b2b.interfaces.ReplaceFragment
import com.switchsolutions.farmtohome.b2b.presentation.cart.response.OrderSubmissionResponseModel
import com.switchsolutions.farmtohome.b2b.presentation.cart.viewmodel.SubmitCartViewModel
import com.switchsolutions.farmtohome.b2b.presentation.dashboard.ui.DashboardFragment
import com.switchsolutions.farmtohome.b2b.roomdb.cart.CartDatabase
import com.switchsolutions.farmtohome.b2b.roomdb.cart.CartEntityClass
import com.switchsolutions.farmtohome.b2b.roomdb.cart.CartRepository
import com.switchsolutions.farmtohome.b2b.utils.NotificationUtil
import com.switchsolutions.farmtohome.b2b.utils.enums.Type
import java.text.SimpleDateFormat
import java.util.*


class CartFragment : Fragment() {
    companion object {
        const val TAG: String = "cartFragment"
        var item: ArrayList<String>? = ArrayList()
        val placeOrderJson = JsonObject()
        fun newInstance() = CartFragment()
        var productName = ArrayList<CartEntityClass>()
        var productId: ArrayList<Int> = ArrayList()
        var productUnit: ArrayList<String> = ArrayList()
        var totalAmount = 0
        lateinit var cartViewModel: CartViewModel
    }

    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var adapter: MyCartRecyclerViewAdapter
    lateinit var binding: CartFragmentBinding
    lateinit var cartDataListCheck: List<CartEntityClass>
    lateinit var product: CartEntityClass

    private lateinit var submitViewModel: SubmitCartViewModel
    private lateinit var waitDialog: ProgressDialog
    private lateinit var submissionDataResponse: OrderSubmissionResponseModel
    var cityId: Int = 0
    private val MY_PREFS_NAME = "FarmToHomeB2B"
    var deliveryDate: String = ""
    var customerId: Int = 0
    var quantityCheck: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val prefs = requireContext().getSharedPreferences(MY_PREFS_NAME, AppCompatActivity.MODE_PRIVATE)
        cityId = prefs.getInt("cityId", 1)
        customerId = prefs.getInt("User", 0)
        deliveryDate = prefs.getString("customerDeliveryDate", "dd-mm-yyyy").toString() //0 is the default value.
        binding = CartFragmentBinding.inflate(layoutInflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        submitViewModel = ViewModelProvider(this).get(SubmitCartViewModel::class.java)

        val dao = CartDatabase.getInstance(requireContext()).cartDAO
        val repository = CartRepository(dao)
        val factory = CartViewModelFactory(repository)
        cartViewModel = ViewModelProvider(this, factory)[CartViewModel::class.java]
        binding.myCartViewModel = cartViewModel
        binding.lifecycleOwner = this

        cartViewModel.totalAmount.observe(viewLifecycleOwner, Observer {
            //binding.totalAmountCartFragment.text = it.toString()+" Rs"
            totalAmount = it
        })
        cartViewModel.message.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
               // Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        })
        binding.llDeliveryDate.setOnClickListener {
            datePick()
        }
        binding.btnSubmitCart.setOnClickListener {
            var zeroQuantity = false
            for ((index) in productName.withIndex())
            {
                if (productName[index].quantity == "0")
                    zeroQuantity = true
            }

            if (deliveryDate == "dd-mm-yyyy" || deliveryDate.isEmpty()){
                Toast.makeText(requireContext(), "Please select delivery date", Toast.LENGTH_SHORT).show()
                binding.ivCalenderCart.setImageResource(R.drawable.calendar_error)
            }
            else if (zeroQuantity){
                Toast.makeText(requireContext(), "Quantity can not be less than 1", Toast.LENGTH_SHORT).show()
            }
            else
            showBottomSheetDialog()
        }
        initRecyclerView()
    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun showBottomSheetDialog() {
         bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.bottom_sheet)
        val parentLayout =
            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        parentLayout?.let { it ->
            val behaviour = BottomSheetBehavior.from(it)
            setupFullHeight(it)
            behaviour.state = BottomSheetBehavior.STATE_EXPANDED
        }
        val recyclerView: RecyclerView? = bottomSheetDialog.findViewById(R.id.recyclerView_checkout)
        val btnConfirm: Button? = bottomSheetDialog.findViewById(R.id.btn_confirm_order)
        val totalAmountTv: TextView? = bottomSheetDialog.findViewById(R.id.total_amount_cart_fragment)
        val deliverDate: TextView? = bottomSheetDialog.findViewById(R.id.checkout_delivery_date)
        val deliveryComments: TextView? = bottomSheetDialog.findViewById(R.id.checkout_delivery_comments)
        val btnClose: ImageButton? = bottomSheetDialog.findViewById(R.id.close_bottom_sheet)
        val dm = Resources.getSystem().displayMetrics
        val rect = dm.run { Rect(0, 0, heightPixels, widthPixels) }
        requireView().minimumHeight = rect.height()
        btnClose!!.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        deliverDate!!.text = deliveryDate
        deliveryComments!!.text = binding.etCustomerRemarks.text.toString()
        var total = 0
        bottomSheetDialog.behavior.isDraggable = false
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        recyclerView!!.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            v.onTouchEvent(event)
            true
        }
        for ((index) in productName.withIndex())
        {
            if (productName[index].price.isNotEmpty())
            total += productName[index].price.toInt()
        }
        totalAmountTv!!.text = total.toString()+" Rs"
        btnConfirm!!.setOnClickListener {
            startOrderSubmission()
        }

        val adapter = ViewCheckoutDetailsAdapter(productName,
            cartDataList as ArrayList<CartEntityClass>, View.OnClickListener {
            //showEditOrderDialog()
        })
        recyclerView!!.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
        bottomSheetDialog.show()
    }

    private fun startOrderSubmission() {

            if (productName.isNotEmpty()) {
                for ((index) in productName.withIndex()) {
                    if (productName[index].quantity.toInt() == 0) {
                        Toast.makeText(
                            requireContext(),
                            "Quantity cannot be less than 1",
                            Toast.LENGTH_SHORT
                        ).show()
                        quantityCheck = false
                        break
                    } else {
                        quantityCheck = true
                    }
                }
            }
            if (cartDataListCheck.isNotEmpty()) {
                val productsArray = JsonArray()
                if (cartDataList.isNotEmpty() && quantityCheck) {
                    placeOrderJson.addProperty("customer_id", customerId)
                    placeOrderJson.addProperty("delivery_date", deliveryDate)
                    placeOrderJson.addProperty("city_id", cityId)
                    placeOrderJson.addProperty("total_amount", totalAmount)
                    placeOrderJson.addProperty(
                        "comments",
                        binding.etCustomerRemarks.text.toString()
                    )
                    for ((index) in cartDataList.withIndex()) {
                        product = cartDataList[index]
                        val productObject = JsonObject()
                        productObject.addProperty("site_product_id", cartDataList[index].site_product_id)
                        productObject.addProperty("quantity", productName[index].quantity.toInt())
                        Log.i("ProductQuantity", product.quantity)
                        productObject.addProperty("is_removed", 0)
                        productsArray.add(productObject)
                    }
                    placeOrderJson.add("products", productsArray)
                    //CreateBdoRequestService(cartViewModel, this, requireContext(), placeOrderJson).callApi()
                    startObservers()
                    submitViewModel.startObserver()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please add items to Cart before submitting order.",
                    Toast.LENGTH_LONG
                ).show()
            }
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
            //productQuantity.clear()
            cartViewModel.clearAll()
            showSubmissionSuccessMessage()
            bottomSheetDialog.dismiss()
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
                NotificationUtil.showShortToast(context!!, it.message, Type.DANGER)
            }
        })
    }
    private fun clearCartAfterSubmission() {
        binding.btnSubmitCart.visibility = View.GONE
        binding.cvCartCustomerDetails.visibility = View.GONE
        binding.llCustomerRemarks.visibility = View.GONE
        val editor = requireContext().getSharedPreferences(MY_PREFS_NAME, AppCompatActivity.MODE_PRIVATE).edit()
        badgeCount = 0
        editor.putInt("badgeCount", badgeCount)
        editor.putInt("customerId", 0) //0 is the default value.
        editor.putString("customerName", "")//"" is the default value.
        editor.putString("customerDeliveryDate", "" )
        editor.apply()
        val cb : CartBadge = activity as CartBadge
        cb.cartBadge(0)
        getCartItems()
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
            if (it.isNotEmpty() ) {
                cartDataList = it
                binding.emptyCartImageView.visibility = View.GONE
                binding.emptyCartTitleLabel.visibility = View.GONE
                binding.btnSubmitCart.visibility = View.VISIBLE
                binding.cvCartCustomerDetails.visibility = View.VISIBLE
                binding.llCustomerRemarks.visibility = View.VISIBLE
                adapter.setList(it)
                adapter.notifyDataSetChanged()
            } else if (it.isNotEmpty() && it != null) {
                binding.emptyCartImageView.visibility = View.GONE
                binding.emptyCartTitleLabel.visibility = View.GONE
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
                binding.emptyCartImageView.visibility = View.VISIBLE
                binding.emptyCartTitleLabel.visibility = View.VISIBLE
                adapter.setList(it)
                adapter.notifyDataSetChanged()
            }
        })
    }

    private fun listItemClicked(product: CartEntityClass) {
        val builder = android.app.AlertDialog.Builder(context)
        val inflater = context!!.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView: View = inflater.inflate(R.layout.delete_dialog, null)
        builder.setView(dialogView)
        val alertDialog = builder.create()
        //        alertDialog.setCancelable(isCancelable);

        //        alertDialog.setCancelable(isCancelable);
        val deleteTitle = dialogView.findViewById<TextView>(R.id.delete_dialog_title_tv)
        val deleteMessage = dialogView.findViewById<TextView>(R.id.delete_dialog_message_tv)
        val deletePositive = dialogView.findViewById<Button>(R.id.delete_dialog_positive_btn)
        val deleteNegative = dialogView.findViewById<Button>(R.id.delete_dialog_negative_btn)

        deleteTitle.text = "DELETE PRODUCT"
        deleteMessage.setText("Are you sure you want to delete this product?")

        deletePositive.setOnClickListener { view: View? ->
            alertDialog.dismiss()
            if (cartDataList.isNotEmpty() && cartDataList.size>1) {
                cartViewModel.deleteProduct(product)
                badgeCount -= 1
                val editor = requireContext().getSharedPreferences(MY_PREFS_NAME, AppCompatActivity.MODE_PRIVATE)
                    .edit()
                editor.putInt("badgeCount", badgeCount)
                editor.apply()
                val cb: CartBadge = activity as CartBadge
                cb.cartBadge(badgeCount)
                //  Log.i("cartItemSize", cartDataList.size.toString()+" after")
//            if (badgeCount<1)
//                MainActivity.previousProduct = false
            }
            if (cartDataList.isNotEmpty() && cartDataList.size == 1){
                cartViewModel.clearAll()
                getCartItems()
                val editor = requireContext().getSharedPreferences(MY_PREFS_NAME, AppCompatActivity.MODE_PRIVATE).edit()
                badgeCount = 0
                editor.putInt("badgeCount", badgeCount)
                editor.apply()
                val cb: CartBadge = activity as CartBadge
                cb.cartBadge(badgeCount)
            }

        }

        deleteNegative.setOnClickListener { view: View? -> alertDialog.dismiss() }
        alertDialog.show()

        // Log.i("cartItemSize", cartDataList.size.toString()+" before")

    }
    private fun getCartItems() {
        cartViewModel.getSavedProducts().observe(viewLifecycleOwner, Observer {
            cartDataList = it.ifEmpty { ArrayList() }
        })
    }
    fun datePick() {
        //function to show the date picker

        /**
         * This Gets a calendar using the default time zone and locale.
         * The calender returned is based on the current time
         * in the default time zone with the default.
         */
        val myCalendar = Calendar.getInstance()
        val year = myCalendar.get(Calendar.YEAR)
        val month = myCalendar.get(Calendar.MONTH)
        val day = myCalendar.get(Calendar.DAY_OF_MONTH)

        //get the id of the textviews from the layout

        /**
         * Creates a new date picker dialog for the specified date using the parent
         * context's default date picker dialog theme.
         */
        val dpd = DatePickerDialog(
            requireContext(), { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                /**
                 * The listener used to indicate the user has finished selecting a date.
                 */

                /**
                 * The listener used to indicate the user has finished selecting a date.
                 */

                /**
                 *Here the selected date is set into format i.e : Year-Month-Day
                 * And the month is counted in java is 0 to 11 so we need to add +1 so it can be as selected.
                 * */
                /**
                 *Here the selected date is set into format i.e : Year-Month-Day
                 * And the month is counted in java is 0 to 11 so we need to add +1 so it can be as selected.
                 * */
                val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDayOfMonth"
                // Selected date it set to the TextView to make it visible to user.
                // tvSelectedDate.text = selectedDate

                /**
                 * Here we have taken an instance of Date Formatter as it will format our
                 * selected date in the format which we pass it as an parameter and Locale.
                 * Here I have passed the format as dd/MM/yyyy.
                 */
                /**
                 * Here we have taken an instance of Date Formatter as it will format our
                 * selected date in the format which we pass it as an parameter and Locale.
                 * Here I have passed the format as dd/MM/yyyy.
                 */
                val sdf = SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH)
                // The formatter will parse the selected date in to Date object
                // so we can simply get date in to milliseconds.
                val theDate = sdf.parse(selectedDate)
                //use the safe call operator with .let to avoid app crashing it theDate is null
                theDate?.let {
                    deliveryDate = sdf.format(theDate)
                    binding.tvDeliveryDateCart.text = deliveryDate
                    binding.ivCalenderCart.setImageResource(R.drawable.calendar)
                    val editor =requireContext().getSharedPreferences(MY_PREFS_NAME, AppCompatActivity.MODE_PRIVATE).edit()
                    editor.putString("customerDeliveryDate", deliveryDate)
                    editor.apply()
                    // Here we have parsed the current date with the Date Formatter which is used above.
                    val currentDate = sdf.parse(sdf.format(System.currentTimeMillis()))
                    //use the safe call operator with .let to avoid app crashing it currentDate is null
                }
            },
            year, month, day
        )

        /**
         * Sets the maximal date supported by this in
         * milliseconds since January 1, 1970 00:00:00 in time zone.
         *
         * @param maxDate The maximal supported date.
         */
        // 86400000 is milliseconds of 24 Hours. Which is used to restrict the user from selecting today and future day.
        dpd.datePicker.minDate = System.currentTimeMillis() - 1000
        dpd.show()

    }

    fun showSubmissionSuccessMessage() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        val inflater: LayoutInflater =
            requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogLayout: View = inflater.inflate(R.layout.dialog_custom_cart_submission, null)
        val myCardView: CardView = dialogLayout.findViewById(R.id.dialog_sign_in_box)
        val buttonOk: Button = dialogLayout.findViewById(R.id.btn_ok_submission)
        // linearLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent))
        //  linearLayout.setBackgroundResource(Color.TRANSPARENT)
        myCardView.setCardBackgroundColor(Color.TRANSPARENT)
        myCardView.cardElevation = 0f
        ///
        val anim: LottieAnimationView = dialogLayout.findViewById(R.id.animation_view)
        builder.setView(dialogLayout)
        builder.setCancelable(false)
        val loginDialog: Dialog = builder.create()
        loginDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        loginDialog.show()
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 2000
        animator.addUpdateListener(ValueAnimator.AnimatorUpdateListener { valueAnimator ->
            anim.setProgress(
                valueAnimator.animatedValue as Float
            )
        })
        animator.start()
//        val handler = Handler()
//        handler.postDelayed({
//            loginDialog.dismiss()
//            jumpToMainFragment()
//           // MainActivity.newInstance().redirectToMainFragment(savedStateRegistry)
//
//        }, 2000)
        buttonOk.setOnClickListener {
            loginDialog.dismiss()
            //jumpToMainFragment()
            //jumpToMainFragment()

            ///

        }
        loginDialog.setOnDismissListener {
            jumpToMainFragment()
        }
    }
    private fun jumpToMainFragment() {
        val fr: Fragment = DashboardFragment()
        val rf: ReplaceFragment? = activity as ReplaceFragment?
        rf!!.replaceWithMainFragment(fr)
    }
}

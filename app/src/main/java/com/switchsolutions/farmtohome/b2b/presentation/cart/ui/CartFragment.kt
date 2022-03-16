package com.switchsolutions.farmtohome.b2b.presentation.cart.ui

import android.animation.ValueAnimator
import android.app.DatePickerDialog
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
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.switchsolutions.farmtohome.b2b.CartViewModel
import com.switchsolutions.farmtohome.b2b.CartViewModelFactory
import com.switchsolutions.farmtohome.b2b.MainActivity
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
import kotlin.collections.ArrayList

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
        cartViewModel = ViewModelProvider(this, factory).get(CartViewModel::class.java)
        binding.myCartViewModel = cartViewModel
        binding.lifecycleOwner = this
        cartViewModel.message.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
               // Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        })
        binding.llDeliveryDate.setOnClickListener {
            datePick()
        }
        binding.btnSubmitCart.setOnClickListener {
            if (deliveryDate == "dd-mm-yyyy" || deliveryDate.isEmpty()){
                Toast.makeText(requireContext(), "Please select delivery date", Toast.LENGTH_SHORT).show()
                binding.ivCalenderCart.setImageResource(R.drawable.calendar_error)
            }
            else {
                if (productQuantity.isNotEmpty()) {
                    for ((index) in productQuantity.withIndex()) {
                        if (productQuantity[index].toInt() == 0) {
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
                        placeOrderJson.addProperty(
                            "comments",
                            binding.etCustomerRemarks.text.toString()
                        )
                        for ((index) in cartDataList.withIndex()) {
                            product = cartDataList[index]
                            val productObject = JsonObject()
                            productObject.addProperty("site_product_id", cartDataList[index].site_product_id)
                            productObject.addProperty("quantity", productQuantity[index].toInt())
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
            showSubmissionSuccessMessage()
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
       // Log.i("cartItemSize", cartDataList.size.toString()+" before")
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

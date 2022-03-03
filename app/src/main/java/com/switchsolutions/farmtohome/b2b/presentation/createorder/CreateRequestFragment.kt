package com.switchsolutions.farmtohome.b2b.presentation.createorder

import android.app.DatePickerDialog
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.switchsolutions.farmtohome.b2b.CartViewModel
import com.switchsolutions.farmtohome.b2b.CartViewModelFactory
import com.switchsolutions.farmtohome.b2b.MainActivity
import com.switchsolutions.farmtohome.b2b.R
import com.switchsolutions.farmtohome.b2b.databinding.CreateRequestFragmentBinding
import com.switchsolutions.farmtohome.b2b.presentation.createorder.data.request.CreateB2BRequestModel
import com.switchsolutions.farmtohome.b2b.roomdb.cart.CartDatabase
import com.switchsolutions.farmtohome.b2b.roomdb.cart.CartEntityClass
import com.switchsolutions.farmtohome.b2b.roomdb.cart.CartRepository
import java.text.SimpleDateFormat
import java.util.*


class CreateRequestFragment : Fragment() {

    companion object {
        const val TAG: String = "createFragment"
        fun newInstance() = CreateRequestFragment()
        var item: ArrayList<String>? = ArrayList()
        lateinit var binding : CreateRequestFragmentBinding
        lateinit var cartDataList: List<CartEntityClass>
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
    var productUnit : String = ""
    lateinit var itemToUpdate : CartEntityClass
    private val MY_PREFS_NAME = "FarmToHomeBDO"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CreateRequestFragmentBinding.inflate(getLayoutInflater())
        val prefs = requireContext().getSharedPreferences(MY_PREFS_NAME, AppCompatActivity.MODE_PRIVATE)
        deliveryDate = prefs.getString("customerDeliveryDate", "" ).toString()
        badgeCount = prefs.getInt("badgeCount", 0)
        createB2BRequestModel = CreateB2BRequestModel( null, "", null)
        binding.llDeliveryDate.setOnClickListener {
        datePick()
        }
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dao = CartDatabase.getInstance(requireContext()).cartDAO
        val repository = CartRepository(dao)
        val factory = CartViewModelFactory(repository)
        cartVM = ViewModelProvider(this, factory)[CartViewModel::class.java]
       // getCustomers()
        val adapterTvProducts = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, MainActivity.productName)
        binding.etSelectProduct.setAdapter(adapterTvProducts)
        cartVM.cartStatus.observe(viewLifecycleOwner, Observer {
            if (it!!)
            {
                badgeCount = 0
                val editor = requireContext().getSharedPreferences(MY_PREFS_NAME, AppCompatActivity.MODE_PRIVATE).edit()
                editor.putInt("badgeCount", badgeCount)
                editor.apply()
            }
        })
        val prefs =requireContext().getSharedPreferences(MY_PREFS_NAME, AppCompatActivity.MODE_PRIVATE)
        getCartItems()

        binding.etSelectProduct.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                productSelected = false
                if (binding.etSelectProduct.isPerformingCompletion) {
                    productSelected = true
                ///    Log.i("CustomerIndex", "Change Text  " + MainActivity.productIdData[count] +  " Item  " + MainActivity.productNamesData[count] )
                    binding.etSelectProduct.error = null
                    return
                }
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (binding.etSelectProduct.isPerformingCompletion) {
                    productSelected = true
                    binding.etSelectProduct.error = null
                    return
                }
                if (binding.etSelectProduct.text.toString().isNotEmpty())
                binding.etSelectProduct.error = "Choose Product"
                else {
                    binding.etSelectProduct.error = null
                }

            }
        })
        binding.etSelectProduct.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                productSelected = false
                if (binding.etSelectProduct.isPerformingCompletion) {
                    productSelected = true
                    ///    Log.i("CustomerIndex", "Change Text  " + MainActivity.productIdData[count] +  " Item  " + MainActivity.productNamesData[count] )
                    binding.etSelectProduct.error = null
                    for ((index) in MainActivity.productUnit.withIndex())
                    {
                        if (binding.etSelectProduct.text.toString().equals(MainActivity.productName[index]))
                        {
                            productId = MainActivity.productID[index]
                            productUnit = MainActivity.productUnit[index]
                            productImgUrl = MainActivity.productImgUrl[index]
                            "Quantity $productUnit".also { binding.tvProductQuantity.text = it }
                            Log.i("CustomerIndex", "ID:   " + productId +  " Customer:   " + productUnit )
                            break
                        }
                    }
                    return
                }
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (binding.etSelectProduct.isPerformingCompletion) {
                    productSelected = true
                    binding.etSelectProduct.error = null
                    return
                }
                if (binding.etSelectProduct.text.toString().isNotEmpty())
                    binding.etSelectProduct.error = "Choose Product"
                else {
                    binding.etSelectProduct.error = null
                }

            }
        })
        binding.btnAddToCart.setOnClickListener {
              if (binding.tvDateSelected.text.equals("dd/mm/yyyy")) {
                binding.ivDatePicker.setImageResource(R.drawable.calendar_error)
                Toast.makeText(requireContext(), "Please Select Delivery Date", Toast.LENGTH_LONG)
                    .show()
            } else if (binding.etSelectProduct.text.isEmpty() || !productSelected) {
                binding.etSelectProduct.error = "Choose Product"
            } else if (binding.etSelectProductQuantity.text.isEmpty() || (binding.etSelectProductQuantity.text.toString().toIntOrNull())!! <= 0) {
                binding.etSelectProductQuantity.error = "Enter Valid Quantity"
            } else if (cartDataList != null && cartDataList.isNotEmpty()){
                val inputMethodManager: InputMethodManager? = requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
                inputMethodManager!!.hideSoftInputFromWindow(view.applicationWindowToken, 0)
                binding.etSelectProduct.error = null
                binding.etSelectProductQuantity.error= null
                for ((index) in cartDataList.withIndex()){
                    if (productId == cartDataList[index].id) {
                        itemToUpdate = cartDataList[index]
                        itemToUpdate.quantity =(itemToUpdate.quantity.toIntOrNull()
                            ?.plus(binding.etSelectProductQuantity.text.toString().toIntOrNull()!!)).toString()
                        previousProduct = true
                        break
                    }
                }
                if (previousProduct) {
                    cartVM.update(
                        itemToUpdate
                    )
                    previousProduct = false
                }
                else
                {
                    cartVM.saveOrUpdate(
                        binding.etSelectProduct.text.toString(),
                        productId,
                        binding.etSelectProductQuantity.text.toString(),
                        productUnit,
                        productImgUrl,
                        binding.tvDateSelected.text.toString()
                    )
                    val editor = requireContext().getSharedPreferences(MY_PREFS_NAME, AppCompatActivity.MODE_PRIVATE).edit()
                    badgeCount += 1
                    editor.putInt("badgeCount", badgeCount)
                    editor.putString("customerDeliveryDate", binding.tvDateSelected.text.toString())
                    editor.apply()
                }
                binding.etSelectProduct.setText("")
                binding.etSelectProductQuantity.setText("")
                productSelected = false
                Toast.makeText(requireContext(), "Added To Cart", Toast.LENGTH_SHORT).show()
            }
              else {
                  val inputMethodManager: InputMethodManager? = requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
                  inputMethodManager!!.hideSoftInputFromWindow(view.applicationWindowToken, 0)
                  cartVM.saveOrUpdate(
                      binding.etSelectProduct.text.toString(),
                      productId,
                      binding.etSelectProductQuantity.text.toString(),
                      productUnit,
                      productImgUrl,
                      binding.tvDateSelected.text.toString()
                  )
                  binding.etSelectProduct.setText("")
                  binding.etSelectProductQuantity.setText("")
                  badgeCount += 1
                  val editor = requireContext().getSharedPreferences(MY_PREFS_NAME, AppCompatActivity.MODE_PRIVATE).edit()
                  editor.putInt("badgeCount", badgeCount)
                  editor.putString("customerDeliveryDate", binding.tvDateSelected.text.toString())
                  editor.apply()
                  productSelected = false
                  Toast.makeText(requireContext(), "Added To Cart", Toast.LENGTH_SHORT).show()
              }
        }
    }

//    private fun getCustomers() {
//            customerVM.getSavedProducts().observe(viewLifecycleOwner, Observer {
//                if (it.isNotEmpty()) {
//                    for ((index) in it.withIndex()) {
//                        customerNamesData.add(it[index].customer_name)
//                    }
//                }
//                else
//                    customerNamesData = ArrayList()
//            })
//
//    }

    private fun getCartItems() {
        cartVM.getSavedProducts().observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                cartDataList= it
            }
            else
                cartDataList = ArrayList()
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
                val sdf = SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH)
                // The formatter will parse the selected date in to Date object
                // so we can simply get date in to milliseconds.
                val theDate = sdf.parse(selectedDate)
                //use the safe call operator with .let to avoid app crashing it theDate is null
                theDate?.let {

                    binding.tvDateSelected.text = sdf.format(theDate)
                    binding.ivDatePicker.setImageResource(R.drawable.calendar)
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


}
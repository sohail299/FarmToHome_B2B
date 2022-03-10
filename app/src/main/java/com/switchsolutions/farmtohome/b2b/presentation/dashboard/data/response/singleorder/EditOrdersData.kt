package com.switchsolutions.farmtohome.b2b.presentation.dashboard.data.response.singleorder

import com.google.gson.annotations.SerializedName

data class EditOrdersData(
    @SerializedName("customer_id") var customer_id: Int? = null,
    @SerializedName("customer") var customer: String? = null,
    @SerializedName("id") var id: Int? = null,
    @SerializedName("delivery_date") var delivery_date :  String? = null,
    @SerializedName("status") var status :  Int? = null
)

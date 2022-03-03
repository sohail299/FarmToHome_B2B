package com.switchsolutions.farmtohome.b2b.presentation.createorder.data.request

import com.google.gson.annotations.SerializedName

class CreateB2BRequestModel(
    @SerializedName("customer_id")
    var customer_id: String? = null,

    @SerializedName("delivery_date")
    var delivery_date: String? = null,

    @SerializedName("city_id")
    var city_id: String? = null
        )

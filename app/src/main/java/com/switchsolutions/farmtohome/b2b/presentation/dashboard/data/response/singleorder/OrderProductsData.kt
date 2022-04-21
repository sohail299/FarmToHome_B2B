package com.switchsolutions.farmtohome.b2b.presentation.dashboard.data.response.singleorder

import com.google.gson.annotations.SerializedName

data class OrderProductsData(
    @SerializedName("quantity") var quantity: Int? = null,
    @SerializedName("label") var label: String? = null,
    @SerializedName("unit") var unit: String? = null,
    @SerializedName("imgUrl") var imgUrl: String? = null,
    @SerializedName("selling_price_b2b") var selling_price_b2b: String? = null,
    @SerializedName("is_removed") var is_removed: String? = null,
    @SerializedName("value") var value: Int? = null,

    )

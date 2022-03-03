package com.switchsolutions.farmtohome.b2b.presentation.login.data.request

class LoginRequestModel (
    var msisdn : String,
    var password : String,
    var platform : String,
    var fcmTokenIos : String,
    var fcmTokenAndroid : String,
    var fcmTokenWeb : String
)
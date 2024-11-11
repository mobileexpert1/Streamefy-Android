package com.streamefy.country_code.model

data class CountryCodeModel(
    var nameCode:String="",
    var phoneCode:String="",
    var name:String="",
    var englishName:String="",
    var flagResID:Int=0,
    var isSelected:Boolean=false
)

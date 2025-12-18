package com.example.belarusuniversities.data

import com.google.gson.annotations.SerializedName

data class ApiUniversity(
    @SerializedName("name")
    val name: String,

    @SerializedName("country")
    val country: String,

    @SerializedName("web_pages")
    val webPages: List<String>,

    @SerializedName("domains")
    val domains: List<String>,

    @SerializedName("alpha_two_code")
    val countryCode: String
)
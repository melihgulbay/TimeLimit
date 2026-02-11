package com.example.timelimit.core.model

import com.google.gson.annotations.SerializedName

data class Quote(
    @SerializedName("q")
    val text: String,
    @SerializedName("a")
    val author: String
)

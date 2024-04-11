package com.example.irde.file_inventory.model


import com.google.gson.annotations.SerializedName

//data class InventorySubmitModelClass(
//    @SerializedName("found")
//    val found: Int,
//    @SerializedName("notFound")
//    val notFound: Int,
//    @SerializedName("rfidnumbers")
//    val rfidnumbers: List<Rfidnumber>,
//    @SerializedName("submittedBy")
//    val submittedBy: String
//) {
//    data class Rfidnumber(
//        @SerializedName("rfidno")
//        val rfidno: String,
//        @SerializedName("status")
//        val status: Int
//    )
//}


data class InventorySubmitModelClass(
    @SerializedName("found")
    val found: Int,
    @SerializedName("notFound")
    val notFound: Int,
    @SerializedName("rfidnumbers")
    val rfidnumbers: List<Rfidnumber>,
    @SerializedName("submittedBy")
    val submittedBy: String)

data class Rfidnumber(@SerializedName("rfidno")
val rfidno: String,
@SerializedName("status")
var status: Int
)

package com.example.irde.retrofit


import com.example.irde.file_inventory.model.GetRegisteredItemModel
import com.example.irde.file_inventory.model.InventorySubmitModelClass
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface RetrofitApi {
    @GET("/api/get-registerd-files")
    fun GetregistedData():Call<GetRegisteredItemModel>

    @GET("/api/search-register-file")
    fun searchByCRD(@Query("fileno") fetchcrdbysingleRFID:String):Call<GetRegisteredItemModel>

    @GET("/api/search-register-file")
    fun identifyFile(@Query("fileno") fetchcrdbysingleRFID:String):Call<GetRegisteredItemModel>

    @POST("/api/create-Inventory")
    fun inventorySubmit(@Body data: InventorySubmitModelClass): Call<String>








}
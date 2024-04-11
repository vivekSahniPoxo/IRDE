package com.example.irde.file_inventory.model


import com.google.gson.annotations.SerializedName

class GetRegisteredItemModel : ArrayList<GetRegisteredItemModel.GetRegisteredItemModelItem>(){
    data class GetRegisteredItemModelItem(
        @SerializedName("created_on")
        val createdOn: String,
        @SerializedName("department_id")
        val departmentId: String,
        @SerializedName("department_name")
        val departmentName: String,
        @SerializedName("div_head_name")
        val divHeadName: String,
        @SerializedName("estimated_cost")
        val estimatedCost: String,
        @SerializedName("file_closing_date")
        val fileClosingDate: String,
        @SerializedName("file_color")
        val fileColor: String,
        @SerializedName("file_name")
        val fileName: String,
        @SerializedName("file_no")
        val fileNo: String,
        @SerializedName("intiating_officers_name")
        val intiatingOfficersName: String,
        @SerializedName("is_in_record")
        val isInRecord: Boolean,
        @SerializedName("mmg_no")
        val mmgNo: String,
        @SerializedName("mmg_staff_name")
        val mmgStaffName: String,
        @SerializedName("mode_of_bidding")
        val modeOfBidding: String,
        @SerializedName("pdc")
        val pdc: String,
        @SerializedName("project_no")
        val projectNo: String,
        @SerializedName("qty_nos")
        val qtyNos: Int,
        @SerializedName("rfid_no")
        val rfidNo: String,
        @SerializedName("tel_no1")
        val telNo1: String,
        @SerializedName("tel_no2")
        val telNo2: String,
        @SerializedName("tel_no3")
        val telNo3: String,
        @SerializedName("tender_no")
        val tenderNo: String,
        @SerializedName("tender_opening_date")
        val tenderOpeningDate: String,
        @SerializedName("updated_on")
        val updatedOn: String
    )
}
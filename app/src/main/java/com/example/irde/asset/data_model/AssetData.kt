package com.example.irde.asset.data_model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(tableName = "all_excel_data_items",indices = [Index(value = ["rfidNo"], unique = true)])
data class AssetDataItem(
    @PrimaryKey
    val id:Int,
    var asset: String,
    var model: String,
    var rfidNo: String,
    var quantity: String,
    var category: String,
    var astvverDate: String,
    var doneby: String,
    var inventoryHolder: String,
    var trensferedTo: String,
    var permanentissue: String,
    var piv:String,
    var inventoryNo:String,
    val volume:String,
    val inventoryPageNo:String,
    val lagederNo:String,
    val departMent:String,
    val inventoryOfficerName:String,
    val createdOn:String,
    val action:String,
    var status:String
)
//@Entity(tableName = "all_excel_data_items",indices = [Index(value = ["rfidNo"], unique = true)])
//data class AssetDataItem(
//    @PrimaryKey
//    val id:Int,
//    var ledger: String,
//    var ledgerPageNo: String,
//    var division: String,
//    var section: String,
//    var divisionalInventoryOfficer: String,
//    var itemName: String,
//    var aU: String,
//    var qty: String,
//    var totalCost: String,
//    var remarks: String,
//    var rfidNo:String,
//    var inventory:String,
//    val invPageNo:String,
//    var status:String
//)


@Entity(tableName = "file_excel_data_items",indices = [Index(value = ["rfidNo"], unique = true)])
data class FileDataItem(
    @PrimaryKey
    val id:Int,
    val fileNo:String,
    val fileName:String,
    val quantityNOS:String,
    val tenderOpDate:String,
    val cost:String,
    val fileClosingDate:String,
    val biddingDate:String,
    val projectNo:String,
    val mmg:String,
    val tenderNo:String,
    val rfidNo:String,
    val pdc:String,
    val officerName:String,
    val divHeadName:String,
    val mmgStaffName:String,
    val fileColor:String,
    val telNo1:String,
    val telNo2:String,
    val telNo3:String,
    val action:String,
    var status:String


)

//@Entity(tableName = "file_excel_data_items",indices = [Index(value = ["tag_id"], unique = true)])
//data class FileDataItem(
//    @PrimaryKey
//    val id:Int,
//    val fileNo:String,
//    val fileNo_one:String,
//    val fileNo_two:String,
//    val fileNo_three:String,
//    //val part:String,
//    val immsNo:String,
//    val itemName:String,
//    val estimate_cost:String,
//    val project_or_buildUp:String,
//    val projectNo:String,
//    val division:String,
//    val identingOfficerL:String,
//    val divisional_head:String,
//    val contactNo:String,
//    val modeOfTender:String,
//    val mmg_officer:String,
//    val tag_id:String,
//    var status:String
//
//
//)

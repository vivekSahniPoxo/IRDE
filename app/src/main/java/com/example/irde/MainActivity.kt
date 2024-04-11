package com.example.irde

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.Menu

import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.viewModels
import com.example.irde.asset.asset_audit.AssetAuditActivity
import com.example.irde.asset.data_model.AssetDataItem
import com.example.irde.asset.data_model.FileDataItem
import com.example.irde.databinding.ActivityMainBinding
import com.example.irde.excel.AddAssetInActivity
import com.example.irde.excel_import.ExcelImporter
import com.example.irde.file_indentification.FileIdentificationActivity
import com.example.irde.file_inventory.FileInventoryActivity
import com.example.irde.file_searching.FileSearchingActivity
import com.example.irde.local_db.LocalDBExcelViewModel

import com.example.irde.utils.Cons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream


class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding
    lateinit var excelImporter: ExcelImporter
    val READ_REQUEST_CODE: Int = 42
    private var file: File? = null

    private val excelViewModelAudit: LocalDBExcelViewModel by viewModels()
    var isImportExcel=""
    companion object {
        private const val MANAGE_EXTERNAL_STORAGE_REQUEST_CODE = 123
    }

//    val DataHandler = DataBaseHandler.getDatabase(this)
//    val allItemDao = DataHandler.allDataItem()
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
      excelImporter = ExcelImporter(contentResolver)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (Environment.isExternalStorageManager()) {
            // Permission granted, proceed with writing to external storage
        } else {
            // Request the permission
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            startActivityForResult(intent, MANAGE_EXTERNAL_STORAGE_REQUEST_CODE)
        }
    } else {
        // For versions below Android 11, handle accordingly
    }

        binding.mCardInventory.setOnClickListener {
            val intent = Intent(this, FileInventoryActivity::class.java)
            startActivity(intent)
        }

        binding.mCardAddInExel.setOnClickListener {
            val intent = Intent(this, AddAssetInActivity::class.java)
            startActivity(intent)
        }

        binding.cardAudit.setOnClickListener {
            val intent = Intent(this, AssetAuditActivity::class.java)
            startActivity(intent)
        }


        binding.mIdentiryCard.setOnClickListener {
            val intent = Intent(this, FileIdentificationActivity::class.java)
            startActivity(intent)
        }

        binding.mAudit.setOnClickListener {
            val intent = Intent(this, FileSearchingActivity::class.java)
            startActivity(intent)
        }

        binding.showMenuButton.setOnClickListener {
            showPopupMenu(it)
        }



}


    @SuppressLint("ResourceType")
    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        val inflater = popupMenu.menuInflater
       // inflater.inflate(R.menu.menu_item, popupMenu.menu)
        inflater.inflate(R.menu.items,popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.action_import_excel -> {
                    // Handle the "Import Excel" action
                    requestStoragePermission()
                    isImportExcel = Cons.isImportFile
                    true
                }
                R.id.import_asset_excel->{
                    requestStoragePermission()
                    isImportExcel = Cons.isImportAsset
                    true
                }
                R.id.action_exit -> {
                    // Handle the "Exit" action

                    Toast.makeText(applicationContext, "Exit", Toast.LENGTH_LONG).show()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }


    private fun requestStoragePermission() {
        // Start the file picker activity
        val mimeTypes = arrayOf(
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",  // .xls & .xlsx

        )

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.type = if (mimeTypes.size == 1) mimeTypes[0] else "*/*"
            if (mimeTypes.size > 0) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            }
        } else {
            var mimeTypesStr = ""
            for (mimeType in mimeTypes) {
                mimeTypesStr += "$mimeType|"
            }
            intent.type = mimeTypesStr.substring(0, mimeTypesStr.length - 1)
        }
        startActivityForResult(Intent.createChooser(intent, "ChooseFile"), READ_REQUEST_CODE)
    }


    @SuppressLint("SuspiciousIndentation")
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {


            handleAssetUI(resultData)

        }
    }



    @SuppressLint("SuspiciousIndentation")
    fun handleAssetUI(resultData :Intent?){
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait...")
        progressDialog.setCancelable(false)
        progressDialog.show()

   GlobalScope.launch {
        resultData?.data?.let { uri ->
            val contentResolver = contentResolver
            val inputStream: InputStream? = contentResolver.openInputStream(uri)

            //val fileName = excelImporter.getFileName(uri)
            val pickedFileData = inputStream?.let {excelImporter.importFromExcel(uri) }


                            if (isImportExcel == Cons.isImportAsset) {
                                excelViewModelAudit.deleteAllItem()
                                pickedFileData?.let { it ->
                                    if(!it.isNullOrEmpty() && it.size>0) {
                                        var count=0
                                        var indexcount=0
                                        it as ArrayList
                                        for (row in it) {


                                val asset = row[0]
                                val model = row[1]
                                val rfidNo = row[2]
                                val quantity =row[3]
                                val category = row[4]
                                val astv3verDate = row[5]
                                val doneBy = row[6]
                                val inventoryHolde = row[7]
                                val transferredTo = row[8]
                                val parmanentIssue = row[9]
                                val piv = row[10]
                                val inventoryNo = row[11]
                                val volume = row[12]
                                val inventoryPage = if (row.size>13)row[13] else "NA"
                                val ledgerNo = if (row.size>14)row[14] else "NA"
                                val department =  if (row.size>15)row[15] else "NA"
                                val inventoryOfficerName = if (row.size > 16) row[16] else "NA"
                                val createdOn = if (row.size > 17) row[17] else "NA"
                                val action = if (row.size > 18) row[18] else "NA"

                                            excelViewModelAudit.insertAllDataItems(
                                                AssetDataItem(
                                                    count,
                                                    asset,
                                                    model,
                                                    rfidNo,
                                                    quantity,
                                                    category,
                                                    astv3verDate,
                                                    doneBy,
                                                    inventoryHolde,
                                                    transferredTo,
                                                    parmanentIssue,
                                                    piv,
                                                    inventoryNo,
                                                    volume,
                                                    inventoryPage,
                                                    ledgerNo,
                                                    department,
                                                    inventoryOfficerName,
                                                    createdOn,
                                                    action,
                                                    Cons.NOTFOUND


                                                )
                                            )
                                            count++

                                        }
                                        GlobalScope.launch {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    applicationContext,
                                                    "Total Rows found : " + count.toString(),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                    }
                                }

                                    }
                                }
                            } else if (isImportExcel == Cons.isImportFile){

                                pickedFileData?.let { it ->
                                    if(!it.isNullOrEmpty() && it.size>0) {
                                        var count=0
                                        var indexcount=0
                                        it as ArrayList
                                        for (row in it) {

                                val fileNo = row[0]
                                val fileName = row[1]
                                val quantityNos = row[2]
                                val tender_op_date = row[3]
                                val cost = row[4]
                                val file_closing_date = row[5]
                                val bidding_mode = row[6]
                                val projectN0 =row[7]
                                val mmg = row[8]
                                val tenderNo = row[9]
                                val rfidNo = row[10]
                                val pdc = row[11]
                                val officerName =row[12]
                                val diviHeadName =row[13]
                                val mmgStaffName = if (row.size>14)row[14] else "NA"
                                val fileColor = if (row.size>15)row[15] else "NA"
                                val telNo1 = if (row.size>16)row[16] else "NA"
                                val telNo2 = if (row.size>17)row[17] else "NA"
                                val telNo3 = if (row.size>18)row[18] else "NA"
                                val action = if (row.size>19)row[19] else "NA"

                                excelViewModelAudit.insertAllFileDataItems(FileDataItem(count,fileNo,fileName,quantityNos,
                                    tender_op_date, cost ,file_closing_date,bidding_mode,projectN0,mmg,tenderNo,rfidNo.toUpperCase(),pdc,officerName,
                                    diviHeadName,mmgStaffName,fileColor,telNo1,telNo2,telNo3,action
                                    ,Cons.NOTFOUND))
                                count++
                            }
                                        GlobalScope.launch {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    applicationContext,
                                                    "Total Rows found : " + count.toString(),
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                            }
                                        }
                                    }
                                }
                            }
                            else {
                                        GlobalScope.launch {
                                            withContext(Dispatchers.Main) {
                                                Log.e("IdentifyActivity", "Row doesn't have enough elements")
                                                Toast.makeText(applicationContext, "No value found", Toast.LENGTH_SHORT)
                                                    .show()
                                            }
                                        }

                                    }
                                inputStream?.close()
                            }
                                   progressDialog.dismiss()
                            }
    }

 }

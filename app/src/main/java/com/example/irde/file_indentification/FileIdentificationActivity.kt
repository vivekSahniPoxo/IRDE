package com.example.irde.file_indentification

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.media.SoundPool
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.irde.utils.sharPref.SharePref
import com.example.filetracking.retrofit.RetrofitClient
import com.example.irde.MainActivity
import com.example.irde.R
import com.example.irde.databinding.ActivityIdentifyBinding
import com.example.irde.file_inventory.model.GetRegisteredItemModel
import com.example.irde.local_db.AllExcelDataItemDao
import com.example.irde.local_db.DataBaseHandler
import com.example.irde.utils.App
import com.example.irde.utils.Cons
import com.google.android.material.snackbar.Snackbar
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager
import com.speedata.libuhf.utils.ErrorStatus
import com.speedata.libuhf.utils.StringUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class FileIdentificationActivity : AppCompatActivity() {
    lateinit var binding:ActivityIdentifyBinding
    var isInventoryRunning = false
    lateinit var iuhfService: IUHFService
    lateinit var progressDialog: ProgressDialog

    private var soundId = 0
    private var soundPool: SoundPool? = null
    var lastTimeMillis: Long = 0
    lateinit var  handler: Handler
    var rfidNo = ""
    lateinit var  handlerr: Handler
   // lateinit var sharePref: SharePref
    lateinit var dataHandler:DataBaseHandler
    lateinit var allItemDao:AllExcelDataItemDao
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIdentifyBinding.inflate(layoutInflater)
        setContentView(binding.root)
         dataHandler = DataBaseHandler.getDatabase(this)
         allItemDao = dataHandler.allDataItem()
        handlerr = Handler()
        handler = Handler()
        progressDialog = ProgressDialog(this)


//        try {
//            sharePref = SharePref()
//            val savedBaseUrl = sharePref.getData("baseUrl")
//            if (savedBaseUrl != null && savedBaseUrl.isNotEmpty()) {
//                Cons.BASE_URL = savedBaseUrl
//                Log.d("baseURL", savedBaseUrl)
//            }
//        } catch (e: Exception) {
//            Log.d("exception", e.toString())
//        }

        binding.imBack.setOnClickListener {
//            if (isInventoryRunning==true) {
//                stopSearching()
//            }
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            //iuhfService.closeDev()
            finish()
        }

        binding.button.setOnClickListener {
//            if (!isInventoryRunning) {
                iuhfService.setOnReadListener { var1 ->
                    //iuhfService.inventoryStart()
                    val stringBuilder = StringBuilder()
                    val epcData = var1.epcData
                    val hexString = StringUtils.byteToHexString(epcData, var1.epcLen)
                    if (!TextUtils.isEmpty(hexString)) {
                        stringBuilder.append("EPC：").append(hexString).append("\n")
                    }
                    if (var1.status == 0) {
                        val readData = var1.readData
                        val readHexString = StringUtils.byteToHexString(readData, var1.dataLen)
                        stringBuilder.append("ReadData:").append(readHexString).append("\n")
                        Toast.makeText(this, readHexString.toUpperCase(), Toast.LENGTH_SHORT).show()
                        handleUIItem(readHexString.toUpperCase())

                    } else {
                        stringBuilder.append(this.resources.getString(R.string.read_fail)).append(":").append(
                                ErrorStatus.getErrorStatus(this,var1.status)
                            ).append("\n")
                    }
                    handler.sendMessage(handler.obtainMessage(1, stringBuilder))

                }
                val readArea = iuhfService.readArea(1, 2, 6, "00000000")
                if (readArea != 0) {
                    val err: String = this.resources.getString(R.string.read_fail) + ":" + ErrorStatus.getErrorStatus(this,
                        readArea
                    ) + "\n"
                    handler.sendMessage(handler.obtainMessage(1, err))

                }


            //} else{
               // stopSearching()
           // }
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode ==  KeyEvent.KEYCODE_BUTTON_R2 || keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_BUTTON_L1) {
//            if (!isInventoryRunning) {
//                startSearching()

            iuhfService.setOnReadListener { var1 ->
                val stringBuilder = StringBuilder()
                val epcData = var1.epcData
                val hexString = StringUtils.byteToHexString(epcData, var1.epcLen)
                if (!TextUtils.isEmpty(hexString)) {
                    stringBuilder.append("EPC：").append(hexString).append("\n")
                } else if (var1.status == 0) {
                    val readData = var1.readData
                    val readHexString = StringUtils.byteToHexString(readData, var1.dataLen)
                    stringBuilder.append("ReadData:").append(readHexString).append("\n")
                    Toast.makeText(this, readHexString, Toast.LENGTH_SHORT).show()
                    //showProgressbar()
                    handleUIItem(readHexString.toUpperCase())
//                    progressDialog = ProgressDialog(this)
//                    progressDialog.setMessage("Please wait...")
//                    progressDialog.setCancelable(false) // Prevent users from dismissing it by tapping outside
//                    progressDialog.show()
//                     identifyFile(readHexString)

                    //handlerr.sendMessage(handlerr.obtainMessage(1, stringBuilder))
                } else {
                    stringBuilder.append(this.resources.getString(R.string.read_fail))
                        .append(":").append(ErrorStatus.getErrorStatus(this, var1.status))
                        .append("\n")
                    handlerr.sendMessage(handlerr.obtainMessage(1, stringBuilder))
                }
            }

            // This line initiates the readArea operation
            val readArea = iuhfService.readArea(1, 2, 6, "00000000")
            if (readArea != 0) {
                val err: String =
                    this.resources.getString(R.string.read_fail) + ":" + ErrorStatus.getErrorStatus(
                        this,
                        readArea
                    ) + "\n"
                handlerr.sendMessage(handlerr.obtainMessage(1, err))
            }

            // Return true to indicate that you have handled the event
            return true
//            } else {
//                stopSearching()
//            }
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Handle back button press
            // startActivity(Intent(this, MainActivity::class.java))
            finish()
            return true // Return true to indicate that you have handled the event
        }

        // If the event is not handled, call the superclass implementation
        return super.onKeyDown(keyCode, event)
    }





    override fun onStop() {
        super.onStop()
        stopSearching()
    }






    @SuppressLint("ResourceAsColor")
    fun stopSearching(){
        isInventoryRunning = false
        iuhfService.closeDev()
    }

    override fun onStart() {
        super.onStart()
       initializeUHF()
    }


    @SuppressLint("ResourceAsColor")
    fun  initializeUHF(){


        try {
            iuhfService = UHFManager.getUHFService(this)
            iuhfService.openDev()
            iuhfService.antennaPower = 12
           // iuhfService.setFrequency(2)


        } catch (e:Exception){
            Log.d("Exception",e.toString())
        }


    }







    @RequiresApi(Build.VERSION_CODES.M)
    private fun identifyFile(RfidNo:String){
        if (!App.get().isConnected()) {
            //InternetConnectionDialog(this, null).show()
            progressDialog.dismiss()
            Snackbar.make(binding.root,"No Network",Snackbar.LENGTH_SHORT).show()
            return
        }
        RetrofitClient.getResponseFromApi().identifyFile(RfidNo).enqueue(object:
            Callback<GetRegisteredItemModel> {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(call: Call<GetRegisteredItemModel>, response: Response<GetRegisteredItemModel>) {


                if (response.isSuccessful) {
                    stopSearching()
                    progressDialog.dismiss()
                    try {
                        response.body()?.forEach {


//                        binding.apply {
//                            tvFileNo.text = it.fileNo
//                            tvFileNo.text = it.fileName
//                            tvQtyNos.text = it.qtyNos.toString()
//                           // tenderOpeningDate.text = it.tenderOpeningDate
//                            tvEstimatedCos.text = it.estimatedCost.toString()
//                           // tvFileClosingDate.text = it.fileClosingDate
//                            tvModeOfBindding.text = it.modeOfBidding
//                            tvMmgStaffName.text = it.mmgStaffName
//                            tvDivHeadName.text = it.divHeadName
//                            tvFileColor.text = it.fileColor
//                            tvTelNo1.text = it.telNo1
//                            tvTelNo2.text = it.telNo2
//                            tvTelNo3.text = it.telNo3
//                            //tvCreatedOn.text = it.createdOn
//                            //tvUpdateOn.text = it.updatedOn
//                            tvInitialOfficerName.text = it.intiatingOfficersName
//                            tvProjectName.text = it.projectNo
//                            tvTenderName.text = it.tenderNo
//
//
//                            tenderOpeningDate.text = dateFormatConverter(it.tenderOpeningDate)
//                            tvFileClosingDate.text = dateFormatConverter(it.fileClosingDate)
//                            tvCreatedOn.text = dateFormatConverter(it.createdOn)
//                            tvUpdateOn.text = dateFormatConverter(it.updatedOn)
//                        }



                        }





                    } catch (e: Exception) {
                        Log.d("exception", e.toString())
                    }

                } else if (response.code()==400){
                    progressDialog.dismiss()
                    Toast.makeText(this@FileIdentificationActivity,response.message(), Toast.LENGTH_SHORT).show()
                } else if (response.code()==500){
                    progressDialog.dismiss()
                    Toast.makeText(this@FileIdentificationActivity,response.message(), Toast.LENGTH_SHORT).show()
                } else if (response.code()==404){
                    progressDialog.dismiss()
                    Toast.makeText(this@FileIdentificationActivity,response.message(), Toast.LENGTH_SHORT).show()
                }
                // handle  Api error



            }

            override fun onFailure(call: Call<GetRegisteredItemModel>, t: Throwable) {
                Toast.makeText(this@FileIdentificationActivity,t.localizedMessage, Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }

        })
    }

    fun dateFormatConverter(dateString: String?): String {
        if (dateString == null) {
            // Handle null case if needed
            return ""
        }

        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
        val date: Date = inputFormat.parse(dateString) ?: Date()

        val outputFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        return outputFormat.format(date)
    }




    fun handleUIItem(RfidNo:String){
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait...")
        progressDialog.setCancelable(false) // Prevent users from dismissing it by tapping outside
        progressDialog.show()
        try {
            val getBookItem = allItemDao.getFileDataByRfidNumber(RfidNo)

            if (getBookItem != null) {
                getBookItem.let { item ->

                    binding.apply {
                        tvFileNo.text = item.fileNo
                        tvFileName.text = item.fileName
                        tvQuantityNos.text =
                            item.quantityNOS.toDoubleOrNull()?.toInt().toString() ?: "NA"
                        tvTenderOpeningDate.text = item.tenderOpDate
                        tvCost.text = item.cost.toDoubleOrNull()?.toInt().toString() ?: "NA"
                        tvFileClosingDate.text = item.fileClosingDate
                        tvBiddingDate.text = item.biddingDate
                        tvProjectNo.text = item.projectNo
                        tvMmg.text = item.mmg
                        tvTenderNo.text = item.tenderNo
                        tvRfidNo.text = item.rfidNo
                        tvPdc.text = item.pdc
                        tvOfficerName.text = item.officerName
                        tvDivHeadName.text = item.divHeadName
                        tvMmgStaffName.text = item.mmgStaffName
                        tvFileColor.text = item.fileColor
                        tvTel1.text = item.telNo1
                        tvTelNo2.text = item.telNo2
                        tvTelNo3.text = item.telNo3

                    }

                }
            } else if (getBookItem == null) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "No Item Found", Toast.LENGTH_SHORT).show()
                }
            }
            progressDialog.dismiss()
        } catch (e:Exception){
            e.printStackTrace()
        }
    }








}
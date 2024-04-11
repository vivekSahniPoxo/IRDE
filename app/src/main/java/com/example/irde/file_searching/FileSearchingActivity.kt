package com.example.irde.file_searching

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.SoundPool
import android.os.*
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.filetracking.retrofit.RetrofitClient
import com.example.irde.MainActivity
import com.example.irde.R
import com.example.irde.databinding.ActivitySearchBinding
import com.example.irde.file_inventory.model.GetRegisteredItemModel
import com.example.irde.local_db.DataBaseHandler
import com.example.irde.local_db.LocalDBExcelViewModel
import com.example.irde.utils.App
import com.example.irde.utils.Cons
import com.example.irde.utils.sharPref.SharePref
import com.google.android.material.snackbar.Snackbar
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager
import com.speedata.libuhf.bean.SpdInventoryData
import com.speedata.libuhf.interfaces.OnSpdInventoryListener
import kotlinx.android.synthetic.main.activity_search.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class FileSearchingActivity : AppCompatActivity() {
    lateinit var binding:ActivitySearchBinding
    lateinit var progressDialog: ProgressDialog
    lateinit var iuhfService: IUHFService
    lateinit var  handler: Handler
    private var soundPool: SoundPool? = null
    private var soundId = 0
    private var soundId1 = 0
    var isInventoryRunning = false
    private var epcToStr: String? = null
    var rfidNo  = ""
    var isSearchingStart = false
    lateinit var sharePref: SharePref
    val viewModel:LocalDBExcelViewModel by viewModels()
    var spinnerItems=  arrayListOf<String>()
    var status = false
    @SuppressLint("SuspiciousIndentation", "ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getAllSpinner()




        binding.imBack.setOnClickListener {
            finish()
        }



        et_search_file.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position).toString()
            Toast.makeText(applicationContext, selectedItem, Toast.LENGTH_SHORT).show()

            if (selectedItem == "Select") {

            } else {
                getSearchItem(selectedItem)


                }



        }





        handler = object : Handler(Looper.getMainLooper()) {
            @SuppressLint("SetJavaScriptEnabled")
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if (msg.what == 1) {
                    if (!TextUtils.isEmpty(rfidNo)) {
                        val spdInventoryData = msg.obj as SpdInventoryData
                        val epc = spdInventoryData.getEpc().toUpperCase()
                       Log.d("wkfpkp",epc)
                        if (epc == rfidNo) {

                            val rssi = spdInventoryData.getRssi().toInt()
                            Log.d("rssi",rssi.toString())
                            val i = -60
                            val j = -40
                            if (rssi > i) {
                                if (rssi > j) {
                                    Log.d("rssiSound1",rssi.toString())

                                    soundPool!!.play(soundId1, 1f, 1f, 0, 0, 3f)
                                } else {
                                    Log.d("rssiSound2",rssi.toString())
                                    soundPool!!.play(soundId1, 0.6f, 0.6f, 0, 0, 2f)
                                }
                            } else {
                                Log.d("rssiSound3",rssi.toString())
                                soundPool!!.play(soundId1, 0.3f, 0.3f, 0, 0, 1f)
                            }
                        } else{
//
                        }
                    }
                }
            }
        }






        try {
            sharePref = SharePref()
            val savedBaseUrl = sharePref.getData("baseUrl")
            if (savedBaseUrl != null && savedBaseUrl.isNotEmpty()) {
                Cons.BASE_URL = savedBaseUrl
                Log.d("baseURL", savedBaseUrl)
            }
        } catch (e: Exception) {
            Log.d("exception", e.toString())
        }

        binding.tvBtnSearch.setOnClickListener {
//            val DataHandler = DataBaseHandler.getDatabase(this)
//            val allItemDao = DataHandler.allDataItem()
            val fileNo = binding.etSearchFile.text.toString()
            if (binding.tvBtnSearch.text=="Search") {
                hideKeyboard()
                if (binding.etSearchFile.text.isNotEmpty()) {
                    getSearchItem(fileNo)


                } else {
                    binding.etSearchFile.error = "Please input field should not empty"
                }
            } else if(binding.tvBtnSearch.text=="Stop"){
                stopSearching()
            }
        }




        binding.btnStart.setOnClickListener {

            try {
                if (rfidNo.isEmpty()){
                    Snackbar.make(binding.root,"No data found for search",Snackbar.LENGTH_SHORT).show()
                } else {
                    startSearching()
                    binding.btnStart.isVisible = false
                    binding.btnStop.isVisible = true
                    iuhfService.setOnInventoryListener(object : OnSpdInventoryListener {
                        override fun getInventoryData(var1: SpdInventoryData) {
                            handler.sendMessage(handler.obtainMessage(1, var1))
                            Log.d("as3992_6C", "id is $soundId")
                        }

                        override fun onInventoryStatus(status: Int) {
//                        iuhfService.inventoryStart()
                        }
                    })
                }
            } catch (e:Exception){

            }

        }

        binding.btnStop.setOnClickListener {
            isInventoryRunning = false
            binding.btnStart.isVisible=  true
            binding.btnStop.isVisible=  false
            iuhfService.inventoryStop()
            //stopInventoryService()
        }

    }



    @SuppressLint("ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BUTTON_R2 || keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_BUTTON_L1) {
            if (rfidNo.isNotEmpty()) {
                if (!isSearchingStart) {
                    startSearching()
                    iuhfService.setOnInventoryListener(object : OnSpdInventoryListener {
                        override fun getInventoryData(var1: SpdInventoryData) {
                            handler.sendMessage(handler.obtainMessage(1, var1))
                            Log.d("as3992_6C", "id is $soundId")
                        }

                        override fun onInventoryStatus(status: Int) {

//                            runOnUiThread {
                                //iuhfService.inventoryStart()
                            //}
                        }
                    })

                } else {
                    stopSearching()
                }
            } else{
                Snackbar.make(binding.root,"No Data for search",Snackbar.LENGTH_SHORT).show()
            }

            return true
        }
        else {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                // startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    @SuppressLint("ResourceAsColor")
    fun  startSearching(){

        try {
            initSoundPool()
        }catch (e:Exception){
           e.printStackTrace()
            Toast.makeText(applicationContext,e.toString(),Toast.LENGTH_SHORT).show()
        }

        try {
            iuhfService = UHFManager.getUHFService(this)
            iuhfService.openDev()
            iuhfService.antennaPower = 30
            iuhfService.inventoryStart()
            isSearchingStart = true
            runOnUiThread {   binding.gifImage.isVisible = true }
        } catch (e:Exception){
            e.printStackTrace()
            Toast.makeText(applicationContext,e.toString(),Toast.LENGTH_SHORT).show()
        }
        if (isSearchingStart){
            runOnUiThread {
                binding.tvBtnSearch.text = "Stop"
                binding.tvBtnSearch.setBackgroundColor(ContextCompat.getColor(this, R.color.red))

            }

        }

    }

    @SuppressLint("ResourceAsColor")
    fun stopSearching(){
        try {
            soundPool!!.release()
        } catch (e:Exception){
            e.printStackTrace()
            Toast.makeText(applicationContext,e.toString(),Toast.LENGTH_SHORT).show()
        }

        try {
            iuhfService.inventoryStop()
            iuhfService.closeDev()
            isSearchingStart = false
        }
        catch (e:Exception){
            e.printStackTrace()
            Toast.makeText(applicationContext,e.toString(),Toast.LENGTH_SHORT).show()
        }
        if (!isSearchingStart) {
            runOnUiThread(kotlinx.coroutines.Runnable {

                binding.tvBtnSearch.text = "Search"
                binding.tvBtnSearch.setBackgroundColor(ContextCompat.getColor(this, R.color.btn_color))
                binding.gifImage.isVisible = false

            })
        }

    }








    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }




    fun initSoundPool() {
        soundPool = SoundPool(2, AudioManager.STREAM_MUSIC, 0)
        soundId = soundPool!!.load("/system/media/audio/ui/VideoRecord.ogg", 0)
        Log.w("as3992_6C", "id is $soundId")
        soundId1 = soundPool!!.load(this, R.raw.scankey, 0)
    }






    override fun onStart() {
        super.onStart()

    }


    override fun onStop() {
        super.onStop()
        stopSearching()


    }




    fun Activity.hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusView = currentFocus
        if (currentFocusView != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocusView.windowToken, 0)
        }
    }




    fun getAllSpinner(){
        viewModel.getAllFileData.observe(this@FileSearchingActivity, androidx.lifecycle.Observer{items->
               spinnerItems.add("Select")
                items.forEach {
                    spinnerItems.add(it.fileNo)
                    spinnerItems.add(it.fileName)
                }
                val adapter: ArrayAdapter<String> =
                    ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, spinnerItems)
                val textView = findViewById<View>(R.id.et_search_file) as AutoCompleteTextView
                textView.setAdapter(adapter)

            })

    }

    @SuppressLint("SuspiciousIndentation")
    fun getSearchItem(fileNo:String) {
        val DataHandler = DataBaseHandler.getDatabase(this)
        val allItemDao = DataHandler.allDataItem()
        val getBookItem = allItemDao.getAllFileDataItem(fileNo)

        if (getBookItem != null) {
            progressDialog = ProgressDialog(this)
                    progressDialog.setMessage("Please wait...")
                    progressDialog.setCancelable(false) // Prevent users from dismissing it by tapping outside
                    progressDialog.show()
            getBookItem.let { item ->
                binding.apply {
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
                        rfidNo = item.rfidNo

                    }
                }
            }
        } else {
            Toast.makeText(applicationContext,"No item found",Toast.LENGTH_SHORT).show()
        }
        progressDialog.dismiss()
    }
}
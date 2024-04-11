package com.example.irde.file_inventory

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.*
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.filetracking.retrofit.RetrofitClient
import com.example.irde.MainActivity
import com.example.irde.R
import com.example.irde.asset.data_model.FileDataItem
import com.example.irde.databinding.ActivityInventoryBinding
import com.example.irde.file_inventory.adapter.InventoryAdapter
import com.example.irde.file_inventory.model.InventorySubmitModelClass
import com.example.irde.file_inventory.model.Rfidnumber
import com.example.irde.local_db.LocalDBExcelViewModel
import com.example.irde.utils.App
import com.example.irde.utils.Cons
import com.example.irde.utils.sharPref.SharePref
import com.google.android.material.snackbar.Snackbar
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager
import com.speedata.libuhf.bean.SpdInventoryData
import com.speedata.libuhf.interfaces.OnSpdInventoryListener
import kotlinx.coroutines.*
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

class FileInventoryActivity : AppCompatActivity() {

    var isInventoryRunning = false
    lateinit var iuhfService: IUHFService
    lateinit var binding:ActivityInventoryBinding
    private var soundId = 0
    private var soundPool: SoundPool? = null
    var lastTimeMillis: Long = 0
    var rfidNo = ""
    lateinit var temList:ArrayList<String>
    lateinit var rfidNoList:ArrayList<Rfidnumber>
    private val handlerthread = Handler(Looper.getMainLooper())
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    lateinit var progressDialog: ProgressDialog
    lateinit var inventoryAdapter: InventoryAdapter
    lateinit var tempList:ArrayList<String>
    lateinit var mList:ArrayList<FileDataItem>
    val temp = arrayListOf<String>()
    private var scanningJob: Job? = null
    lateinit var sharePref: SharePref
    var filePath: File?=null
     val found =  arrayListOf<FileDataItem>()
    val notFound =  arrayListOf<FileDataItem>()
     val excelViewModel: LocalDBExcelViewModel by viewModels()
    var isCalledInOnStart = true
    @SuppressLint("ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mList = arrayListOf()
        rfidNoList = arrayListOf()
        temList = arrayListOf()
        inventoryAdapter = InventoryAdapter(mList)
        binding.recyclerview.adapter = inventoryAdapter
        tempList = arrayListOf()
        try {
            iuhfService = UHFManager.getUHFService(this)
        } catch (e:Exception){
            e.printStackTrace()
        }


        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        binding.etUserId.clearFocus()


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
      //  inventoryData()




        binding.llFoundHeading.setOnClickListener {
            if (found.isNotEmpty()){
                found.clear()
            }

            mList.forEach { fileItem ->
                if (fileItem.status == Cons.FOUND) {
                    found.add(
                        FileDataItem(
                            fileItem.id,
                            fileItem.fileNo,
                            fileItem.fileName,
                            fileItem.quantityNOS,
                            fileItem.tenderOpDate,
                            fileItem.cost,
                            fileItem.fileClosingDate,
                            fileItem.biddingDate,
                            fileItem.projectNo,
                            fileItem.mmg,
                            fileItem.tenderNo,
                            fileItem.rfidNo,
                            fileItem.pdc,
                            fileItem.officerName,
                            fileItem.divHeadName,
                            fileItem.mmgStaffName,
                            fileItem.fileColor,
                            fileItem.telNo1,
                            fileItem.telNo2,
                            fileItem.telNo3,
                            fileItem.action,
                            Cons.FOUND

                        )
                    )


                }

            }
                binding.llFoundHeading.setBackgroundResource(R.drawable.txt_background)
                binding.llNotHeading.setBackgroundResource(R.drawable.gray_back)
                binding.llTotalHeading.setBackgroundResource(R.drawable.gray_back)

                binding.tvFountCount.text = found.size.toString()
                inventoryAdapter = InventoryAdapter(found)
                binding.recyclerview.adapter=inventoryAdapter
                inventoryAdapter.notifyDataSetChanged()

        }

        binding.llNotHeading.setOnClickListener {
            if (notFound.isNotEmpty()){
                notFound.clear()
            }
            mList.forEach { fileItem ->
                if (fileItem.status == Cons.NOTFOUND) {
                    notFound.add(
                        FileDataItem(
                            fileItem.id,
                            fileItem.fileNo,
                            fileItem.fileName,
                            fileItem.quantityNOS,
                            fileItem.tenderOpDate,
                            fileItem.cost,
                            fileItem.fileClosingDate,
                            fileItem.biddingDate,
                            fileItem.projectNo,
                            fileItem.mmg,
                            fileItem.tenderNo,
                            fileItem.rfidNo,
                            fileItem.pdc,
                            fileItem.officerName,
                            fileItem.divHeadName,
                            fileItem.mmgStaffName,
                            fileItem.fileColor,
                            fileItem.telNo1,
                            fileItem.telNo2,
                            fileItem.telNo3,
                            fileItem.action,
                            Cons.NOTFOUND
                        )
                    )

                }


            }

            runOnUiThread {
                binding.llNotHeading.setBackgroundResource(R.drawable.txt_background)
                binding.llFoundHeading.setBackgroundResource(R.drawable.gray_back)
                binding.llTotalHeading.setBackgroundResource(R.drawable.gray_back)
                inventoryAdapter = InventoryAdapter(notFound)
                binding.tvNotFoundCount.text = notFound.size.toString()
                binding.recyclerview.adapter=inventoryAdapter
                inventoryAdapter.notifyDataSetChanged()
            }


        }


        binding.llTotalHeading.setOnClickListener {
            runOnUiThread {
                val textColor = ContextCompat.getColor(this, R.color.white)
                binding.tvTotal.setTextColor(textColor)
                binding.llTotalHeading.setBackgroundResource(R.drawable.txt_background)
                val notFoundColor = ContextCompat.getColor(this, R.color.white)
                binding.notFoundHeading.setTextColor(notFoundColor)
                binding.llNotHeading.setBackgroundResource(R.drawable.gray_back)
                val totalColor = ContextCompat.getColor(this, R.color.white)
                binding.FoundHeading.setTextColor(totalColor)
                binding.llFoundHeading.setBackgroundResource(R.drawable.gray_back)
                binding.tvTotalCount.text=mList.size.toString()
            }
            onClickTotalCount()
        }

        binding.imBack.setOnClickListener {
            if (isInventoryRunning) {
                stopSearching()
            }
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            //iuhfService.closeDev()
            mList.clear()
            finish()
        }


        binding.SubmitButton.setOnClickListener {
            try {
                if (binding.etUserId.text?.isEmpty() == true){
                    binding.etUserId.error = "Name field should not be empty"
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        updateFile()
                    }else{
                        Log.d("NotCreatingFile","NotCreating")
                    }

                }
            } catch (e:Exception){

            }

        }

        binding.NewButton.setOnClickListener {
            inventoryAdapter.clearAllData()
            if (isInventoryRunning==true) {
                stopSearching()
            }
            rfidNoList.clear()
            temList.clear()
            inventoryAdapter.refreshAdapter()
            binding.Total.text = ""
//            binding.Found.text=""
//            binding.notFound.text=""
            binding.tvFountCount.text=""
            binding.tvNotFoundCount.text=""


        }

        binding.BackButton.setOnClickListener {
            if (isInventoryRunning==true) {
                stopSearching()
            }
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            //iuhfService.closeDev()
            finish()
        }



        binding.btnStart.setOnClickListener {
            //val selectedItem = binding.spType.selectedItemPosition

            if (mList.isEmpty() ) {
                Snackbar.make(binding.root,"No item found for search", Snackbar.LENGTH_SHORT).show()
            } else {
                if (!isInventoryRunning) {
                    startSearching()

                    scanningJob = coroutineScope.launch(Dispatchers.IO) {
                        iuhfService.setOnInventoryListener(object : OnSpdInventoryListener {
                            override fun getInventoryData(var1: SpdInventoryData) {

                                handlerthread.post {
                                    temp.add(var1.getEpc())
                                    binding.tvcount.text = temp.size.toString()
                                    temp.clear()
                                }
                                coroutineScope.launch{
                                   // Log.d("vccc",var1.getEpc())
                                    handleInventoryData(var1)
                                }

                            }

                            override fun onInventoryStatus(status: Int) {
                                threadpooling(status)
                            }


                        })
                    }
                } else {
                    stopSearching()
                    scanningJob?.cancel()
                }
            }
        }

    }

    fun threadpooling(p0:Int) {
        // Create a thread pool with 4 threads
        val threadPool: ExecutorService = Executors.newFixedThreadPool(4)

        // Define a Runnable with your code
        val runnable = kotlinx.coroutines.Runnable {
            Looper.prepare()
            if (p0 == 65277) {
                // Log.d("p0", p0.toString())
                iuhfService.closeDev()
                SystemClock.sleep(100)
                startSearching()
            } else {
                iuhfService.inventoryStart()
            }
            Looper.loop()
        }

        // Submit the Runnable to the thread pool
        threadPool.submit(runnable)

        // Shutdown the thread pool when done
        threadPool.shutdown()
    }
//    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
//        if (keyCode == KeyEvent.KEYCODE_BUTTON_R2 || keyCode == KeyEvent.KEYCODE_F1) {
//            if (!isInventoryRunning) {
//                startSearching()
//
//                // Start inventory service
//                iuhfService.setOnInventoryListener(object : OnSpdInventoryListener {
//                    @RequiresApi(Build.VERSION_CODES.O)
//                    @SuppressLint("NotifyDataSetChanged")
//                    override fun getInventoryData(var1: SpdInventoryData) {
//                        try {
//                            val timeMillis = System.currentTimeMillis()
//                            val l: Long = timeMillis - lastTimeMillis
//                            if (l < 100) {
//                                return
//                            }
//                            lastTimeMillis = System.currentTimeMillis()
//                            soundPool!!.play(soundId, 1f, 1f, 0, 0, 1f)
//                            Log.d("RFFFF", var1.getEpc())
//                            rfidNo = var1.getEpc()
//
//                            if (rfidNo!=null) {
//                                if (!tempList.contains(rfidNo)) {
//                                    tempList.add(rfidNo)
//
//
//                                    rfidNo = var1.getEpc()
//
//
//                                    runOnUiThread {
//
//                                       // use rfid matching and found or not found file by rfid number
//                                    }
//
//
//
//                                }
//                            }
//
//
//                        } catch (e: Exception) {
//                            Log.d("exception", e.toString())
//                        }
//                    }
//
//                    override fun onInventoryStatus(p0: Int) {
//                        Looper.prepare()
//                        if (p0 == 65277) {
//                            iuhfService.closeDev()
//                            SystemClock.sleep(100)
//                            iuhfService.openDev()
//                            iuhfService.inventoryStart()
//                        } else {
//                            iuhfService.inventoryStart()
//                        }
//                        Looper.loop()
//                    }
//                })
//            } else {
//                stopSearching()
//            }
//        }
//
//
//        return super.onKeyDown(keyCode, event)
//    }

    @SuppressLint("ResourceAsColor")
    fun  startSearching(){
        isInventoryRunning = true
        initSoundPool()
        try {
            iuhfService = UHFManager.getUHFService(this)
            iuhfService.openDev()
            iuhfService.antennaPower = 30

//            val twoPointSevenFiveAdapter = UpdateMidPointAdapter(rfidList)
//            binding.listOfItem.adapter = twoPointSevenFiveAdapter


        } catch (e:Exception){
            Log.d("Exception",e.toString())
        }
         binding.btnStart.text = "Stop"
        iuhfService.inventoryStart()


    }

    @SuppressLint("ResourceAsColor")
    fun stopSearching(){
        try {
            soundPool!!.release()

        }catch(e:Exception){

        }finally {
            iuhfService.inventoryStop()
            iuhfService.closeDev()
        }

        isInventoryRunning = false

        runOnUiThread(Runnable {
            binding.btnStart.text = "Start"
            inventoryAdapter.moveFoundItemsToTop(binding.loader,binding.recyclerview)
            inventoryAdapter.refreshAdapter()
        })
    }

    fun initSoundPool() {
        soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val attributes = AudioAttributes.Builder()
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .build()
            SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(attributes)
                .build()
        } else {
            SoundPool(1, AudioManager.STREAM_MUSIC, 0)
        }
        soundId = soundPool!!.load(this@FileInventoryActivity, R.raw.beep, 0)
    }



    private fun inventoryData(){

            progressDialog = ProgressDialog(this@FileInventoryActivity)
            progressDialog.setMessage("Please wait...")
            progressDialog.setCancelable(false)
            progressDialog.show()
//            binding.Total.text = mList.size.toString()
           // binding.tvTotalCount.text = mList.size.toString()
                mList.clear()

                excelViewModel.getAllFileData.observe(
                    this@FileInventoryActivity,
                    Observer { items ->
                        items.forEach { fileItem ->
                            mList.add(
                                FileDataItem(
                                    fileItem.id,
                                    fileItem.fileNo,
                                    fileItem.fileName,
                                    fileItem.quantityNOS,
                                    fileItem.tenderOpDate,
                                    fileItem.cost,
                                    fileItem.fileClosingDate,
                                    fileItem.biddingDate,
                                    fileItem.projectNo,
                                    fileItem.mmg,
                                    fileItem.tenderNo,
                                    fileItem.rfidNo,
                                    fileItem.pdc,
                                    fileItem.officerName,
                                    fileItem.divHeadName,
                                    fileItem.mmgStaffName,
                                    fileItem.fileColor,
                                    fileItem.telNo1,
                                    fileItem.telNo2,
                                    fileItem.telNo3,
                                    fileItem.action,
                                    Cons.NOTFOUND
                                )
                            )

                        }

                        val textColor =
                            ContextCompat.getColor(this@FileInventoryActivity, R.color.white)
                        inventoryAdapter = InventoryAdapter(mList)

                        binding.tvTotal.setTextColor(textColor)
                        binding.tvTotalCount.text = mList.size.toString()
                        binding.recyclerview.adapter = inventoryAdapter
                        inventoryAdapter.notifyDataSetChanged()
                        progressDialog.dismiss()
                    })


    }

    private fun onClickTotalCount() {
        if (mList.isNotEmpty()) {
            inventoryAdapter = InventoryAdapter(mList)
                val textColor = ContextCompat.getColor(this, R.color.white)
                binding.tvTotal.setTextColor(textColor)
        }
        binding.recyclerview.adapter=inventoryAdapter
        inventoryAdapter.notifyDataSetChanged()
    }

    private suspend fun handleInventoryData(var1: SpdInventoryData) {
        try {
            val timeMillis = System.currentTimeMillis()
            val l: Long = timeMillis - lastTimeMillis
            if (l < 100) {
                return
            }
            lastTimeMillis = System.currentTimeMillis()
            soundPool?.play(soundId, 1f, 1f, 0, 0, 1f)

            val epc = var1.getEpc()
            if (!temList.contains(epc)) {
                temList.add(epc)
                Log.d("epc1",epc)

                withContext(Dispatchers.Main) {
                    val foundItem = mList.find { it.rfidNo == epc }
                    if (foundItem != null) {
                        foundItem.status = "Found"


                        val foundValue = mList.count { it.status == "Found" }
                       // binding.Found.text = foundValue.toString()
                        binding.tvFountCount.text=foundValue.toString()


//                        val total = rfidNoList.count()
                        try {
                            val notFound = mList.size - foundValue
                            //binding.notFound.text = notFound.toString()

                            binding.tvNotFoundCount.text=notFound.toString()
                        } catch (e: Exception) {
                            // Handle NumberFormatException
                        }
                        inventoryAdapter.refreshAdapter()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("handleInventoryData", "Exception: ${e.message}", e)
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun submitInventory(createInventory: InventorySubmitModelClass) {
        if (!App.get().isConnected()) {

            return
        }

        val progressDialog = ProgressDialog(applicationContext)
        progressDialog.setMessage("Please wait...")
        progressDialog.setCancelable(false) // Prevent users from dismissing it by tapping outside
        progressDialog.show()

        RetrofitClient.getResponseFromApi().inventorySubmit(createInventory).enqueue(object : Callback<String> {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(call: Call<String>, response: Response<String>) {
                progressDialog.dismiss() // Dismiss the progressDialog if it's not null

                if (response.isSuccessful) {
                    stopInventoryService()


                    val intent = Intent(this@FileInventoryActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                    Toast.makeText(this@FileInventoryActivity, response.body(), Toast.LENGTH_SHORT).show()
                    try {
                        progressDialog.dismiss()



                    } catch (e: Exception) {
                        Log.d("exception", e.toString())
                    }
                } else if (response.code() == 400) {
                    Toast.makeText(this@FileInventoryActivity, response.message(), Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                } else if (response.code() == 500) {
                    Toast.makeText(this@FileInventoryActivity, response.message(), Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                } else if (response.code() == 404) {
                    Toast.makeText(this@FileInventoryActivity, response.message(), Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
                // Handle API errors
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                progressDialog.dismiss() // Dismiss the progressDialog if it's not null
                Toast.makeText(this@FileInventoryActivity, t.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun stopInventoryService() {
        if (isInventoryRunning) {
            iuhfService.inventoryStop()
            iuhfService.closeDev()
            isInventoryRunning = false
        }
    }

    override fun onPause() {
        super.onPause()
        if (isInventoryRunning){
            stopSearching()
        }


    }

    override fun onStop() {
        super.onStop()
        try {
            soundPool?.release()
        } catch (e:Exception){

        }
         stopSearching()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart() {
        super.onStart()
        if (isCalledInOnStart) {
                inventoryData()
            isCalledInOnStart = false
            }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BUTTON_R2 || keyCode==131) {

            if (mList.isEmpty()){
                Snackbar.make(binding.root,"No item found for search", Snackbar.LENGTH_SHORT).show()
            } else
                if (!isInventoryRunning) {
                    startSearching()

                    // Start inventory service
                    iuhfService.setOnInventoryListener(object : OnSpdInventoryListener {
                        @RequiresApi(Build.VERSION_CODES.O)
                        @SuppressLint("NotifyDataSetChanged")
                        override fun getInventoryData(var1: SpdInventoryData) {
                            handlerthread.post {
                                temp.add(var1.getEpc())
                                binding.tvcount.text = temp.size.toString()
                                temp.clear()
                            }
                            coroutineScope.launch{
                                // Log.d("vccc",var1.getEpc())
                                handleInventoryData(var1)
                            }


                        }


                        override fun onInventoryStatus(p0: Int) {
                            Looper.prepare()
                            if (p0 == 65277) {
                                iuhfService.closeDev()
                                SystemClock.sleep(100)
                                iuhfService.openDev()
                                iuhfService.inventoryStart()
                            } else {
                                iuhfService.inventoryStart()
                            }
                            Looper.loop()
                        }
                    })

                } else {
                    stopSearching()
                }
        }
        return super.onKeyDown(keyCode, event)
    }



    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    fun updateFile() {
        if (mList.isNotEmpty()) {

            try {
                val currentDateTime = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "File_Inventory${binding.etUserId.text.toString()}$currentDateTime.xls"

                filePath = File(Environment.getExternalStorageDirectory().toString(),
                    fileName
                )
                if (!filePath?.exists()!!) {
                    // Create a new workbook and sheet if the file doesn't exist
                    val hssfWorkbook = HSSFWorkbook()
                    val hssfSheet = hssfWorkbook.createSheet("MySheet")

                    // Create the header row
                    val headerRow = hssfSheet.createRow(0)

                    headerRow.createCell(0).setCellValue("Timestamp")
                    headerRow.createCell(1).setCellValue("File No")
                    headerRow.createCell(2).setCellValue("FileName")
                    headerRow.createCell(3).setCellValue("QuantityNOS")
                    headerRow.createCell(4).setCellValue("File Closing Date")
                    headerRow.createCell(5).setCellValue("Bidding Date")
                    headerRow.createCell(6).setCellValue("ProjectNo")
                    headerRow.createCell(7).setCellValue("MMG")
                    headerRow.createCell(8).setCellValue("Tender No")
                    headerRow.createCell(9).setCellValue("Rfid No")
                    headerRow.createCell(10).setCellValue("PDC")
                    headerRow.createCell(11).setCellValue("Officer Name")
                    headerRow.createCell(12).setCellValue("Division Head Name")
                    headerRow.createCell(13).setCellValue("MMG Staff Name")
//                    headerRow.createCell(14).setCellValue("Mode of Tender")
//                    headerRow.createCell(15).setCellValue("MMG Officer")
                    headerRow.createCell(14).setCellValue("File Color")
                    headerRow.createCell(15).setCellValue("Tel No1")
                    headerRow.createCell(16).setCellValue("Tel No2")
                    headerRow.createCell(17).setCellValue("Tel No3")
                    headerRow.createCell(18).setCellValue("Action")
                    headerRow.createCell(19).setCellValue("Status")




                    mList.forEachIndexed { index, item ->
                        val lastRowNum = hssfSheet.lastRowNum + 1

                        val dataRow = hssfSheet.createRow(lastRowNum)
                        dataRow.createCell(0).setCellValue(Cons.getDatetime("gettingdate").toString())
                        dataRow.createCell(1).setCellValue(item.fileNo.toString() ?: "NA")
                        dataRow.createCell(2).setCellValue(item.fileName)
                        dataRow.createCell(3).setCellValue(item.quantityNOS.toDoubleOrNull()?.toInt().toString())
                        dataRow.createCell(4).setCellValue(item.fileClosingDate)
                        dataRow.createCell(5).setCellValue(item.biddingDate)
                        dataRow.createCell(6).setCellValue(item.projectNo)
                        dataRow.createCell(7).setCellValue(item.mmg.toString() ?: "NA")
                        dataRow.createCell(8).setCellValue(item.tenderNo)
                        dataRow.createCell(9).setCellValue(item.rfidNo)
                        dataRow.createCell(10).setCellValue(item.pdc.toDoubleOrNull()?.toInt().toString())
                        dataRow.createCell(11).setCellValue(item.officerName)
                        dataRow.createCell(12).setCellValue(item.divHeadName)
                        dataRow.createCell(13).setCellValue(item.mmgStaffName.toString() ?: "NA")
                        dataRow.createCell(14).setCellValue(item.fileColor)
                        dataRow.createCell(15).setCellValue(item.telNo1)
                        dataRow.createCell(16).setCellValue(item.telNo2)
                        dataRow.createCell(17).setCellValue(item.telNo3)
                        dataRow.createCell(18).setCellValue(item.action)
                        dataRow.createCell(19).setCellValue(item.status)


                    }

                    filePath!!.createNewFile()
                    val fileOutputStream = FileOutputStream(filePath)
                    hssfWorkbook.write(fileOutputStream)

                    Toast.makeText(applicationContext, "File Created", Toast.LENGTH_SHORT)
                        .show()
                    fileOutputStream.flush()
                    fileOutputStream.close()


                } else {
                    // If the file exists, open it and update the data
                    val fileInputStream = FileInputStream(filePath)
                    val hssfWorkbook = HSSFWorkbook(fileInputStream)
                    val hssfSheet: HSSFSheet = hssfWorkbook.getSheetAt(0)

                    mList.forEach { item ->
                        // Calculate the next available row number
                        var lastRowNum = hssfSheet.lastRowNum + 1


                        val dataRow = hssfSheet.createRow(lastRowNum)
                        dataRow.createCell(0).setCellValue(Cons.getDatetime("gettingdate").toString())
                        dataRow.createCell(1).setCellValue(item.fileNo.toString() ?: "NA")
                        dataRow.createCell(2).setCellValue(item.fileName)
                        dataRow.createCell(3).setCellValue(item.quantityNOS.toString())
                        dataRow.createCell(4).setCellValue(item.fileClosingDate)
                        dataRow.createCell(5).setCellValue(item.biddingDate)
                        dataRow.createCell(6).setCellValue(item.projectNo)
                        dataRow.createCell(7).setCellValue(item.mmg.toString() ?: "NA")
                        dataRow.createCell(8).setCellValue(item.tenderNo)
                        dataRow.createCell(9).setCellValue(item.rfidNo)
                        dataRow.createCell(10).setCellValue(item.pdc)
                        dataRow.createCell(11).setCellValue(item.officerName)
                        dataRow.createCell(12).setCellValue(item.divHeadName)
                        dataRow.createCell(13).setCellValue(item.mmgStaffName.toString() ?: "NA")
                        dataRow.createCell(14).setCellValue(item.fileName)
                        dataRow.createCell(15).setCellValue(item.telNo1)
                        dataRow.createCell(16).setCellValue(item.telNo2)
                        dataRow.createCell(17).setCellValue(item.telNo3)
                        dataRow.createCell(18).setCellValue(item.action)
                        dataRow.createCell(19).setCellValue(item.status)






                        fileInputStream.close()
                        val fileOutputStream = FileOutputStream(filePath)
                        hssfWorkbook.write(fileOutputStream)

                        Toast.makeText(this@FileInventoryActivity, "File Updated", Toast.LENGTH_SHORT)
                            .show()
                        fileOutputStream.close()
                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("exception", e.toString())
            }
        }else{
            Toast.makeText(applicationContext,"No Item For Export",Toast.LENGTH_SHORT).show()
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
       // val intent = Intent(this@FileInventoryActivity,MainActivity::class.java)
        //startActivity(intent)
        finish()
    }


}
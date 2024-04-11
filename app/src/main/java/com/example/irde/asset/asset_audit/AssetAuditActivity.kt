package com.example.irde.asset.asset_audit

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.irde.MainActivity
import com.example.irde.R
import com.example.irde.asset.Asset_inventory_adapter.AssetInventoryAdapter
import com.example.irde.asset.data_model.AssetDataItem
import com.example.irde.asset.data_model.FileDataItem
import com.example.irde.databinding.ActivityFileInventoryBinding
import com.example.irde.file_inventory.adapter.InventoryAdapter
import com.example.irde.local_db.LocalDBExcelViewModel
import com.example.irde.utils.Cons
import com.google.android.material.snackbar.Snackbar
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager
import com.speedata.libuhf.bean.SpdInventoryData
import com.speedata.libuhf.interfaces.OnSpdInventoryListener
import kotlinx.coroutines.*
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

class AssetAuditActivity : AppCompatActivity() {
    lateinit var binding:ActivityFileInventoryBinding
    private val handlerthread = Handler(Looper.getMainLooper())
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    lateinit var mList: ArrayList<AssetDataItem>
     var foundAssetList = arrayListOf<AssetDataItem>()
    lateinit var progressDialog: ProgressDialog
    lateinit var assetAdapter:AssetInventoryAdapter
      var isStart = true
     var temList =  arrayListOf<String>()
    val excelViewModel: LocalDBExcelViewModel by viewModels()
    private var soundId = 0
    private var soundPool: SoundPool? = null
    var lastTimeMillis: Long = 0
    lateinit var iuhfService: IUHFService
    val temp = arrayListOf<String>()
    var isInventoryRunning = false
    private var scanningJob: Job? = null
    var filePath: File?=null
    var found =  arrayListOf<AssetDataItem>()
    var notFound =  arrayListOf<AssetDataItem>()

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mList = arrayListOf()
        assetAdapter = AssetInventoryAdapter(mList)
        try {
            iuhfService = UHFManager.getUHFService(this)
        }catch (e:Exception){
            e.printStackTrace()
        }




        binding.imBack.setOnClickListener {
            if (isInventoryRunning==true) {
                stopSearching()
            }
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            //iuhfService.closeDev()
            finish()
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




        binding.llFoundHeading.setOnClickListener {
            if (found.isNotEmpty()){
                found.clear()
            }

            mList.forEach { assetItem ->
                if (assetItem.status == Cons.FOUND) {
                    found.add(AssetDataItem(assetItem.id,
                        assetItem.asset,
                        assetItem.model,
                        assetItem. rfidNo,
                        assetItem. quantity,
                        assetItem.category,
                        assetItem.astvverDate,
                        assetItem.doneby,
                        assetItem. inventoryHolder,
                        assetItem. trensferedTo,
                        assetItem.permanentissue,
                        assetItem. piv,
                        assetItem. inventoryNo,
                        assetItem. volume,
                        assetItem.inventoryPageNo,
                        assetItem.lagederNo,
                        assetItem.departMent,
                        assetItem.inventoryOfficerName,
                        assetItem.createdOn,
                        assetItem.action,
                        Cons.FOUND))
                } }
            binding.llFoundHeading.setBackgroundResource(R.drawable.txt_background)
            binding.llNotHeading.setBackgroundResource(R.drawable.gray_back)
            binding.llTotalHeading.setBackgroundResource(R.drawable.gray_back)
            assetAdapter = AssetInventoryAdapter(found)
            binding.tvFountCount.text = found.size.toString()
            binding.recyclerview.adapter = assetAdapter
            assetAdapter.notifyDataSetChanged()

        }

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        binding.etUserId.clearFocus()


        binding.llNotHeading.setOnClickListener {
            if (notFound.isNotEmpty()){
                notFound.clear()
            }
            mList.forEach { assetItem ->
                if (assetItem.status == Cons.NOTFOUND) {
                    notFound.add(AssetDataItem(assetItem.id,
                        assetItem.asset,
                        assetItem.model,
                        assetItem. rfidNo,
                        assetItem. quantity,
                        assetItem.category,
                        assetItem.astvverDate,
                        assetItem.doneby,
                        assetItem. inventoryHolder,
                        assetItem. trensferedTo,
                        assetItem.permanentissue,
                        assetItem. piv,
                        assetItem. inventoryNo,
                        assetItem. volume,
                        assetItem.inventoryPageNo,
                        assetItem.lagederNo,
                        assetItem.departMent,
                        assetItem.inventoryOfficerName,
                        assetItem.createdOn,
                        assetItem.action,Cons.NOTFOUND))
                }
            }
            assetAdapter = AssetInventoryAdapter(notFound)
            binding.recyclerview.adapter = assetAdapter
            assetAdapter.notifyDataSetChanged()
            binding.tvNotFoundCount.text = notFound.size.toString()

            binding.llNotHeading.setBackgroundResource(R.drawable.txt_background)
            binding.llFoundHeading.setBackgroundResource(R.drawable.gray_back)
            binding.llTotalHeading.setBackgroundResource(R.drawable.gray_back)
        }


        binding.llTotalHeading.setOnClickListener {
            val textColor = ContextCompat.getColor(this, R.color.white)
            binding.tvTotal.setTextColor(textColor)
            binding.llTotalHeading.setBackgroundResource(R.drawable.txt_background)
            val notFoundColor = ContextCompat.getColor(this, R.color.white)
            binding.notFoundHeading.setTextColor(notFoundColor)
            binding.llNotHeading.setBackgroundResource(R.drawable.gray_back)
            val totalColor = ContextCompat.getColor(this, R.color.white)
            binding.FoundHeading.setTextColor(totalColor)
            binding.llFoundHeading.setBackgroundResource(R.drawable.gray_back)
            binding.tvTotalCount.text = mList.size.toString()
            init()
        }






        binding.SubmitButton.setOnClickListener {
            if (binding.etUserId.text?.isNotEmpty() == true) {
                updateFile()
            } else{
                Toast.makeText(applicationContext,"Please enter your name",Toast.LENGTH_SHORT).show()
            }
        }



        binding.btnStart.setOnClickListener {
            //val selectedItem = binding.spType.selectedItemPosition

//            if (repsonseBody.isEmpty() || selectedItem == 0) {
//                Snackbar.make(binding.root, "No data found for search", Snackbar.LENGTH_SHORT).show()
//            } else {
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

    override fun onStart() {
        super.onStart()
        if (isStart){
           init()
            isStart = false
        }

    }

    fun onTotalClick() {
        if (mList.isNotEmpty()) {
            //mList.clear()
            val textColor = ContextCompat.getColor(this, R.color.white)
            binding.tvTotal.setTextColor(textColor)
            assetAdapter = AssetInventoryAdapter(mList)
            binding.recyclerview.adapter = assetAdapter
            assetAdapter.notifyDataSetChanged()
        }
    }


   fun init() {

       progressDialog = ProgressDialog(this@AssetAuditActivity)
       progressDialog.setMessage("Please wait...")
       progressDialog.setCancelable(false)
       progressDialog.show()
       mList.clear()

           excelViewModel.getAllAssetDataItem.observe(this, Observer { item ->
               item.forEach { assetItem ->
                   mList.add(
                       AssetDataItem(
                           assetItem.id,
                           assetItem.asset,
                           assetItem.model,
                           assetItem. rfidNo,
                           assetItem. quantity,
                           assetItem.category,
                           assetItem.astvverDate,
                           assetItem.doneby,
                           assetItem. inventoryHolder,
                           assetItem. trensferedTo,
                           assetItem.permanentissue,
                           assetItem. piv,
                           assetItem. inventoryNo,
                           assetItem. volume,
                           assetItem.inventoryPageNo,
                           assetItem.lagederNo,
                           assetItem.departMent,
                           assetItem.inventoryOfficerName,
                           assetItem.createdOn,
                           assetItem.action,
                           Cons.NOTFOUND
                       )
                   )
               }
               assetAdapter = AssetInventoryAdapter(mList)
               binding.recyclerview.adapter = assetAdapter
               // binding.Total.text = mList.size.toString()
               binding.tvTotalCount.text = mList.size.toString()
               assetAdapter.notifyDataSetChanged()
               progressDialog.dismiss()

           })
       }



    //}


    @SuppressLint("ResourceAsColor")
    fun  startSearching(){
        isInventoryRunning = true
        initSoundPool()
        try {
            iuhfService = UHFManager.getUHFService(this)
            iuhfService.openDev()
            iuhfService.antennaPower =30

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
        soundPool!!.release()
        binding.btnStart.text = "Start"
        isInventoryRunning = false
        iuhfService.inventoryStop()
        iuhfService.closeDev()
        runOnUiThread(Runnable {
            assetAdapter.moveFoundItemsToTop(binding.loader,binding.recyclerview)
            assetAdapter.refreshAdapter()
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
        soundId = soundPool!!.load(this, R.raw.beep, 0)
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

            val epc = var1.getEpc().uppercase().trim()
            if (!temList.contains(epc)) {
                temList.add(epc)

                withContext(Dispatchers.Main) {
                    val foundItem = mList.find { it.rfidNo == epc }
                    if (foundItem != null) {
                        foundItem.status = "Found"
//
//                        foundAssetList.add(AssetDataItem(foundItem.id,foundItem.ledger,foundItem.ledgerPageNo,foundItem.division,
//                            foundItem.section,foundItem.divisionalInventoryOfficer,foundItem.itemName,foundItem.aU,foundItem.qty,foundItem.totalCost,foundItem.remarks,foundItem.rfidNo,foundItem.inventory,
//                            foundItem.invPageNo,foundItem.status))


                        val foundValue = mList.count { it.status == "Found" }
                        binding.tvFountCount.text = foundValue.toString()

//                        val total = rfidNoList.count()
                        try {
                            val notFound = mList.size - foundValue
                            binding.tvNotFoundCount.text = notFound.toString()
                        } catch (e: Exception) {
                            // Handle NumberFormatException
                        }
                        assetAdapter.refreshAdapter()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("handleInventoryData", "Exception: ${e.message}", e)
        }
    }



    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    fun updateFile() {
        if (mList.isNotEmpty()) {

//            filePath = File(
//                Environment.getExternalStorageDirectory().toString() + "/" + "Asset_Inventory${binding.etUserId.text.toString()}${Cons.getDatetime("gettingdate").toString()}" + ".xls"
//            )
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
                    headerRow.createCell(1).setCellValue("Asset")
                    headerRow.createCell(2).setCellValue("Model")
                    headerRow.createCell(3).setCellValue("Rfid No")
                    headerRow.createCell(4).setCellValue("Quantity")
                    headerRow.createCell(5).setCellValue("Category")
                    headerRow.createCell(6).setCellValue("ASTB Ver Date")
                    headerRow.createCell(7).setCellValue("Done By")
                    headerRow.createCell(8).setCellValue("Inventory Holder")
                    headerRow.createCell(9).setCellValue("Transferred To")
                    headerRow.createCell(10).setCellValue("Permanent issue To")
                    headerRow.createCell(11).setCellValue("PIV")
                    headerRow.createCell(12).setCellValue("Inventory No")
                    headerRow.createCell(13).setCellValue("Volume")
                    headerRow.createCell(14).setCellValue("Inventory Page No")

                    headerRow.createCell(15).setCellValue("Ledger No")
                    headerRow.createCell(16).setCellValue("Department")
                    headerRow.createCell(17).setCellValue("Inventory Officer Name")

                    headerRow.createCell(18).setCellValue("Created On")
                    headerRow.createCell(19).setCellValue("Action")
                    headerRow.createCell(20).setCellValue("Status")
                    //headerRow.createCell(16).setCellValue("Not Found")



                    mList.forEachIndexed { index, item ->

                        val lastRowNum = hssfSheet.lastRowNum + 1

                        val dataRow = hssfSheet.createRow(lastRowNum)
                        dataRow.createCell(0).setCellValue(Cons.getDatetime("gettingdate").toString())
                        dataRow.createCell(1).setCellValue(item.asset.toString())
                        dataRow.createCell(2).setCellValue(item.model.toString())
                        dataRow.createCell(3).setCellValue(item.rfidNo.toString())
                        dataRow.createCell(4).setCellValue(item.quantity)
                        dataRow.createCell(5).setCellValue(item.category.toString())
                        dataRow.createCell(6).setCellValue(item.astvverDate)
                        dataRow.createCell(7).setCellValue(item.doneby)
                        dataRow.createCell(8).setCellValue(item.inventoryHolder)
                        dataRow.createCell(9).setCellValue(item.trensferedTo)
                        dataRow.createCell(10).setCellValue(item.permanentissue)
                        dataRow.createCell(11).setCellValue(item.piv.toString())
                        dataRow.createCell(12).setCellValue(item.inventoryNo.toDoubleOrNull()?.toInt().toString())
                        dataRow.createCell(13).setCellValue(item.volume)
                        dataRow.createCell(14).setCellValue(item.inventoryPageNo.toDoubleOrNull()?.toInt().toString())

                        dataRow.createCell(15).setCellValue(item.lagederNo)
                        dataRow.createCell(16).setCellValue(item.departMent)
                        dataRow.createCell(17).setCellValue(item.inventoryOfficerName)
                        dataRow.createCell(18).setCellValue(item.createdOn)
                        dataRow.createCell(19).setCellValue(item.action)
                        dataRow.createCell(20).setCellValue(item.status)
                        //dataRow.createCell(16).setCellValue(item.rfidNo)


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
                        dataRow.createCell(1).setCellValue(item.asset.toString())
                        dataRow.createCell(2).setCellValue(item.model.toString())
                        dataRow.createCell(3).setCellValue(item.rfidNo.toString())
                        dataRow.createCell(4).setCellValue(item.quantity)
                        dataRow.createCell(5).setCellValue(item.category.toString())
                        dataRow.createCell(6).setCellValue(item.astvverDate)
                        dataRow.createCell(7).setCellValue(item.doneby)
                        dataRow.createCell(8).setCellValue(item.inventoryHolder)
                        dataRow.createCell(9).setCellValue(item.trensferedTo)
                        dataRow.createCell(10).setCellValue(item.permanentissue)
                        dataRow.createCell(11).setCellValue(item.piv.toString())
                        dataRow.createCell(12).setCellValue(item.inventoryNo.toDoubleOrNull()?.toInt().toString())
                        dataRow.createCell(13).setCellValue(item.volume)
                        dataRow.createCell(14).setCellValue(item.inventoryPageNo.toDoubleOrNull()?.toInt().toString())

                        dataRow.createCell(15).setCellValue(item.lagederNo)
                        dataRow.createCell(16).setCellValue(item.departMent)
                        dataRow.createCell(17).setCellValue(item.inventoryOfficerName)
                        dataRow.createCell(18).setCellValue(item.createdOn)
                        dataRow.createCell(19).setCellValue(item.action)
                        dataRow.createCell(20).setCellValue(item.status)






                        fileInputStream.close()
                        val fileOutputStream = FileOutputStream(filePath)
                        hssfWorkbook.write(fileOutputStream)

                        Toast.makeText(this@AssetAuditActivity, "File Updated", Toast.LENGTH_SHORT)
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

}
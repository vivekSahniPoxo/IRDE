package com.example.irde.excel

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.audiofx.BassBoost
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.Settings
import android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.irde.MainActivity
import com.example.irde.R
import com.example.irde.databinding.ActivityAddAssetInBinding
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager
import com.speedata.libuhf.utils.ErrorStatus
import com.speedata.libuhf.utils.StringUtils
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class AddAssetInActivity : AppCompatActivity() {
    lateinit var binding:ActivityAddAssetInBinding
    private val REQUEST_CODE_STORAGE_PERMISSION = 123
    lateinit var  handlerr: Handler
    var filePath: File?=null
    var isInventoryRunning = false
    lateinit var iuhfService: IUHFService
    companion object {
        private const val MANAGE_EXTERNAL_STORAGE_REQUEST_CODE = 123
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAssetInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handlerr = Handler()


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
        try {
            handlerr.post {
                iuhfService = UHFManager.getUHFService(this)
                startSearching()
            }
        } catch (e: Exception) {

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




        binding.SubmitButton.setOnClickListener {
            if (valid()) {
                filePath = File(
                    Environment.getExternalStorageDirectory().toString() + "/" + "ExportFile" + ".xls"
                )
//                filePath = File(
//                    Environment.getExternalStorageDirectory().toString() + "/" + "Assets" + ".xls"
//                )
                try {
                    if (!filePath?.exists()!!) {
                        // Create a new workbook and sheet if the file doesn't exist
                        val hssfWorkbook = HSSFWorkbook()
                        val hssfSheet = hssfWorkbook.createSheet("MySheet")

                        // Create the header row
                        val headerRow = hssfSheet.createRow(0)
                        headerRow.createCell(0).setCellValue("Asset ID")
                        headerRow.createCell(1).setCellValue("RFID No")

                        // Create a data row
                        val dataRow = hssfSheet.createRow(1)
                        dataRow.createCell(0).setCellValue(binding.etAssetID.text.toString())
                        // Add RFID No to the data row
                        dataRow.createCell(1).setCellValue(binding.tvRfidNo.text.toString())

                        filePath!!.createNewFile()
                        val fileOutputStream = FileOutputStream(filePath)
                        hssfWorkbook.write(fileOutputStream)
                        binding.etAssetID.setText("")
                        binding.tvRfidNo.text = ""
                        Toast.makeText(this@AddAssetInActivity, "File Created", Toast.LENGTH_SHORT)
                            .show()
                        fileOutputStream.flush()
                        fileOutputStream.close()
                    } else {
                        // If the file exists, open it and update the data
                        val fileInputStream = FileInputStream(filePath)
                        val hssfWorkbook = HSSFWorkbook(fileInputStream)
                        val hssfSheet: HSSFSheet = hssfWorkbook.getSheetAt(0)

                        // Calculate the next available row number
                        var lastRowNum = hssfSheet.lastRowNum + 1

                        // Create a new data row
                        val dataRow = hssfSheet.createRow(lastRowNum)
                        dataRow.createCell(0).setCellValue(binding.etAssetID.text.toString())
                        // Add RFID No to the data row
                        dataRow.createCell(1).setCellValue(binding.tvRfidNo.text.toString())

                        fileInputStream.close()
                        val fileOutputStream = FileOutputStream(filePath)
                        hssfWorkbook.write(fileOutputStream)
                        binding.etAssetID.setText("")
                        binding.tvRfidNo.setText("")
                        Toast.makeText(this@AddAssetInActivity, "File Updated", Toast.LENGTH_SHORT)
                            .show()
                        fileOutputStream.close()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("eception", e.toString())
                }
            }
        }


        }

        @RequiresApi(Build.VERSION_CODES.R)
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)

            if (requestCode == MANAGE_EXTERNAL_STORAGE_REQUEST_CODE) {
                if (Environment.isExternalStorageManager()) {
                    // Permission granted, proceed with writing to external storage
                } else {
                    // Permission denied, handle accordingly
                    Toast.makeText(
                        this,
                        "Manage external storage permission denied. Cannot write to file.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


        @RequiresApi(Build.VERSION_CODES.M)
        override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
            if (keyCode == KeyEvent.KEYCODE_BUTTON_R2 || keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_BUTTON_L1) {
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
                        binding.tvRfidNo.text = readHexString
                        handlerr.sendMessage(handlerr.obtainMessage(1, stringBuilder))
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


        override fun onPause() {
            super.onPause()
            if (isInventoryRunning == true) {
                stopSearching()

            }

            iuhfService.closeDev()
        }

        override fun onStop() {
            super.onStop()
            iuhfService.closeDev()
        }


        @SuppressLint("ResourceAsColor")
        fun stopSearching() {
            isInventoryRunning = false
            iuhfService.inventoryStop()
            iuhfService.closeDev()
        }

    override fun onRestart() {
        super.onRestart()
        startSearching()
    }


        @SuppressLint("ResourceAsColor")
        fun startSearching() {
            isInventoryRunning = true

            try {
                iuhfService = UHFManager.getUHFService(this)
                iuhfService.openDev()
                iuhfService.antennaPower = 15


            } catch (e: Exception) {
                Log.d("Exception", e.toString())
            }

            //iuhfService.inventoryStart()
        }

    fun valid(): Boolean {
        if (binding.tvRfidNo.text.toString().isEmpty()) {
            binding.tvRfidNo.error = "Please scan the RFID tag"
            return false
        } else if (binding.etAssetID.text.toString().isEmpty()) {
            binding.etAssetID.error = "Please provide asset ID"
            return false
        }
        return true
    }

   // }

}
package com.example.irde.excel_import

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.IOException


class ExcelImporter(private val contentResolver: ContentResolver) {

    fun importFromExcel(fileUri: Uri): List<List<String>> {
        val data = mutableListOf<List<String>>()

        try {
            val inputStream = contentResolver.openInputStream(fileUri)

            if (inputStream != null) {
                val workbook = WorkbookFactory.create(inputStream)
                val sheet = workbook.getSheetAt(0)

                for (rowIndex in 1 until sheet.physicalNumberOfRows) {
                    val row: Row = sheet.getRow(rowIndex) ?: continue
                    val rowData = mutableListOf<String>()

                    for (cellIndex in 0 until 19) {
                        val cell = row.getCell(cellIndex)

                        if (cell == null) {
                            rowData.add("Null")
                        } else {
                            when (cell.cellTypeEnum) {
                                CellType.STRING -> rowData.add(cell.stringCellValue)
                                CellType.NUMERIC -> rowData.add(cell.numericCellValue.toString())
                                CellType.BOOLEAN -> rowData.add(cell.booleanCellValue.toString())
                                CellType.BLANK -> rowData.add("NA")
                                CellType.ERROR -> rowData.add("NA")
                                else -> rowData.add("NA")
                            }
                        }
                    }

                    data.add(rowData)
                }
            }

        } catch (e: Exception) {
            Log.e("ExcelImporter", "Error importing data from Excel", e)
            throw IOException("Error importing data from Excel", e)
        }

        return data
    }

    @SuppressLint("Range")
    fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    result = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                result = result?.substring(cut!! + 1)
            }
        }
        return result ?: "Unknown"
    }


}

package com.example.irde.local_db

import androidx.lifecycle.LiveData
import com.example.irde.asset.data_model.AssetDataItem
import com.example.irde.asset.data_model.FileDataItem


class LocaleDBExcelRepo(val allExcelDataItemDao: AllExcelDataItemDao) {
    val getAllDataItem: LiveData<List<AssetDataItem>> = allExcelDataItemDao.getAllDataItems()
     val getAllFileDatItem:LiveData<List<FileDataItem>> = allExcelDataItemDao.getAllFileDataItems()

    suspend fun insertAllDataItems(items: AssetDataItem){
        allExcelDataItemDao.insertAllDataItems(items)
    }

    suspend fun insertAllFileDataItems(items: FileDataItem){
        allExcelDataItemDao.insertAllFileDataItems(items)
    }

    fun getBookDetailsByAccessNo(accessNo: String){
        allExcelDataItemDao.getDataFromAccessNo(accessNo)
    }

    suspend fun deleteAllItem(){
        allExcelDataItemDao.deleteAllItem()
    }

    suspend fun deleteAllFileItem(){
        allExcelDataItemDao.deleteAllFileItem()
    }

}
package com.example.irde.local_db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.irde.asset.data_model.AssetDataItem
import com.example.irde.asset.data_model.FileDataItem


@Dao
interface AllExcelDataItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDataItems(items: AssetDataItem)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFileDataItems(items: FileDataItem)

    @Query("SELECT * FROM file_excel_data_items where fileName = :immsNo OR fileNo=:immsNo")
    fun getAllFileDataItem(immsNo:String): FileDataItem

    @Query("SELECT * FROM file_excel_data_items where rfidNo = :rfidNo")
    fun getFileDataByRfidNumber(rfidNo:String): FileDataItem

    @Query("SELECT * FROM file_excel_data_items")
    fun getAllFileDataItems(): LiveData<List<FileDataItem>>

    @Query("DELETE FROM file_excel_data_items")
    suspend fun deleteAllFileItem()

    @Query("SELECT * FROM all_excel_data_items")
     fun getAllDataItems(): LiveData<List<AssetDataItem>>



     @Query("SELECT * FROM all_excel_data_items where id = :accessNo")
    fun getDataFromAccessNo(accessNo:String): AssetDataItem



    @Query("DELETE FROM all_excel_data_items")
    suspend fun deleteAllItem()
}
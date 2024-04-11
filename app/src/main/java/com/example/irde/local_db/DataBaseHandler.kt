package com.example.irde.local_db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.irde.asset.data_model.AssetDataItem
import com.example.irde.asset.data_model.FileDataItem

@Database(entities =[AssetDataItem::class, FileDataItem::class],version = 5, exportSchema = false)
abstract class DataBaseHandler : RoomDatabase() {

    abstract fun allDataItem(): AllExcelDataItemDao


    companion object {
        @Volatile
        private var INSTANCE: DataBaseHandler? = null

        fun getDatabase(context: Context): DataBaseHandler {
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DataBaseHandler::class.java,
                    "all_excel_item")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }


}
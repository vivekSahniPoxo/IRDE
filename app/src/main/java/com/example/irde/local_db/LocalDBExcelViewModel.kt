package com.example.irde.local_db

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.irde.asset.data_model.AssetDataItem
import com.example.irde.asset.data_model.FileDataItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocalDBExcelViewModel(application: Application): AndroidViewModel(application) {

    private var repository: LocaleDBExcelRepo
    val getAllAssetDataItem: LiveData<List<AssetDataItem>>
    val getAllFileData:LiveData<List<FileDataItem>>
    init {
        val userDao = DataBaseHandler.getDatabase(application).allDataItem()
        repository = LocaleDBExcelRepo(userDao)
        getAllAssetDataItem = repository.getAllDataItem
        getAllFileData = repository.getAllFileDatItem
    }

    fun insertAllDataItems(items: AssetDataItem){
        viewModelScope.launch(Dispatchers.Main) {
            repository.insertAllDataItems(items)
        }
    }

    fun insertAllFileDataItems(items: FileDataItem){
        viewModelScope.launch(Dispatchers.Main) {
            repository.insertAllFileDataItems(items)
        }
    }

    fun fetBookDetailsByAccessNo(accessNo:String){
        viewModelScope.launch(Dispatchers.Main) {
            repository.getBookDetailsByAccessNo(accessNo)
        }
    }

    fun deleteAllItem(){
        viewModelScope.launch {
            repository.deleteAllItem()
        }
    }


    fun deleteAllFileItem(){
        viewModelScope.launch {
            repository.deleteAllFileItem()
        }
    }

}
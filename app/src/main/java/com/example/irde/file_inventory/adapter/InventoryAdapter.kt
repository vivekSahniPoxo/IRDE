package com.example.irde.file_inventory.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.irde.R
import com.example.irde.asset.data_model.FileDataItem
import com.example.irde.databinding.StockTakeLayoutBinding
import com.example.irde.utils.Cons

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*



class InventoryAdapter(private val mList: MutableList<FileDataItem>) :
    RecyclerView.Adapter<InventoryAdapter.ViewHOlder>() {

    private val handler = Handler(Looper.getMainLooper())

    var hilightPositon = ""



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHOlder {
        val itemBinding = StockTakeLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHOlder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHOlder, position: Int) {
        val items = mList[position]
        holder.bind(items)
    }





    override fun getItemCount(): Int = mList.size

    inner class ViewHOlder(private val itemBinding: StockTakeLayoutBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        @SuppressLint("ResourceAsColor", "ResourceType")
        fun bind(item: FileDataItem) {
            itemBinding.apply {
                tvFileNo.text = item.fileNo
                tvFileName.text = item.fileName
                tvQuantityNos.text = item.quantityNOS.toDoubleOrNull()?.toInt().toString() ?: "NA"
                tvTenderOpeningDate.text = item.tenderOpDate
                tvCost.text = item.cost.toDoubleOrNull()?.toInt().toString()?: "NA"
                tvFileClosingDate.text = item.fileClosingDate
                tvBiddingDate.text  = item.biddingDate
                tvProjectNo.text = item.projectNo
                tvMmg.text = item.mmg
                tvTenderNo.text = item.tenderNo
                tvRfidNo.text  = item.rfidNo
                tvPdc.text = item.pdc
                tvOfficerName.text =item.officerName
                tvDivHeadName.text = item.divHeadName
                tvMmgStaffName.text = item.mmgStaffName
                tvFileColor.text = item.fileColor
                tvTel1.text = item.telNo1
                tvTelNo2.text = item.telNo2
                tvTelNo3.text = item.telNo3
            }
            if (item.status=="Found" || hilightPositon==Cons.FOUND ){
                itemBinding.llStock.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.green2))
                itemBinding.imAdd.setBackgroundResource(R.drawable.txt_background)
                itemBinding.imMinimize.setBackgroundResource(R.drawable.txt_background)
                itemBinding.imAdd.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                itemBinding.imMinimize.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
            } else{
                itemBinding.llStock.setBackgroundColor(Color.TRANSPARENT)
            }

            itemBinding.imAdd.setOnClickListener {
                itemBinding.imAdd.setBackgroundResource(R.drawable.txt_background)
                itemBinding.imAdd.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                itemBinding.tvFileClosingDateLl.isVisible = true
                itemBinding.biddingLl.isVisible = true
                itemBinding.projectNoLl.isVisible = true
                itemBinding.mmgLl.isVisible = true
                itemBinding.tenderNoLl.isVisible = true
                itemBinding.rfidNoLl.isVisible = true
                itemBinding.pdcLl.isVisible = true
                itemBinding.projectNoLl.isVisible = true
                itemBinding.officerNameLl.isVisible = true
                //itemBinding.tagIDLl.isVisible = true
                itemBinding.diviHeadName.isVisible=true
                itemBinding.mmgStaffNameLl.isVisible =true
                itemBinding.fillColorLl.isVisible=true
                itemBinding.telNo1Ll.isVisible=true
                itemBinding.telNo2Ll.isVisible=true
                itemBinding.telNo3Ll.isVisible=true
                itemBinding.imAdd.isVisible = false
                itemBinding.imMinimize.isVisible = true
            }

            itemBinding.imMinimize.setOnClickListener {
                itemBinding.imMinimize.setBackgroundResource(R.drawable.txt_background)
                itemBinding.imMinimize.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                itemBinding.tvFileClosingDateLl.isVisible = false
                itemBinding.biddingLl.isVisible = false
                itemBinding.projectNoLl.isVisible = false
                itemBinding.mmgLl.isVisible = false
                itemBinding.tenderNoLl.isVisible = false
                itemBinding.rfidNoLl.isVisible = false
                itemBinding.pdcLl.isVisible = false
                itemBinding.projectNoLl.isVisible = false
                itemBinding.officerNameLl.isVisible = false
                //itemBinding.tagIDLl.isVisible = true
                itemBinding.diviHeadName.isVisible=false
                itemBinding.mmgStaffNameLl.isVisible =false
                itemBinding.fillColorLl.isVisible=false
                itemBinding.telNo1Ll.isVisible=false
                itemBinding.telNo2Ll.isVisible=false
                itemBinding.telNo3Ll.isVisible=false
                itemBinding.imAdd.isVisible = true
                itemBinding.imMinimize.isVisible = false
            }
        }
    }






    fun clearAllData() {
        mList.clear()
        notifyDataSetChanged()
    }

    fun refreshAdapter() {
//        totalCountSize = temList.distinct().size
        handler.post {
            mList.sortByDescending { it.status == Cons.FOUND }
            notifyDataSetChanged()
        }
    }

    fun notFound() {
//        totalCountSize = temList.distinct().size
        handler.post {
            mList.sortByDescending { it.status == Cons.NOTFOUND }
            notifyDataSetChanged()
        }
    }








    fun moveFoundItemsToTop(loader: ProgressBar, recyclerView: RecyclerView) {
        loader.isVisible = true

        GlobalScope.launch(Dispatchers.Default) {
            refreshAdapter()
        }
        loader.isVisible = false
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












}





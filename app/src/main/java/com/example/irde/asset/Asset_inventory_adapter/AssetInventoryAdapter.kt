package com.example.irde.asset.Asset_inventory_adapter

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.irde.R
import com.example.irde.asset.data_model.AssetDataItem
import com.example.irde.databinding.AssetInventoryListBinding

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*



class AssetInventoryAdapter(private val mList: MutableList<AssetDataItem>) :
    RecyclerView.Adapter<AssetInventoryAdapter.ViewHOlder>() {

    private val handler = Handler(Looper.getMainLooper())


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHOlder {
        val itemBinding = AssetInventoryListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHOlder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHOlder, position: Int) {
        val items = mList[position]
        holder.bind(items)
    }





    override fun getItemCount(): Int = mList.size

    inner class ViewHOlder(private val itemBinding: AssetInventoryListBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(item: AssetDataItem) {
            itemBinding.apply {
                tvMouse.text = item.asset
                tvModel.text = item.model
                tvRfidNo.text = item.rfidNo
                tvQuantiry.text = item.quantity.toDoubleOrNull()?.toInt().toString()
                tvCategroy.text = item.category
                tvAstbVerDate.text = item.astvverDate.toDoubleOrNull()?.toInt().toString()
                tvDoneBy.text = item.doneby
                tvInventoryHolder.text = item.inventoryHolder
                tvTranserferdTo.text = item.trensferedTo
                tvPermanetIssue.text = item.permanentissue
                tvPiv.text  = item.piv
                tvInventoryNo.text = item.inventoryNo
                tvVolume.text = item.volume
                tvInventoryPageNo.text = item.inventoryPageNo
                ledgerNo.text = item.lagederNo
                tvDepartment.text = item.departMent
                tvInvnetoryOfficerName.text =  item.inventoryOfficerName
                tvCreatedOn.text = item.createdOn
                tvAction.text  =  item.action


            }


            if (item.status=="Found"){
                itemBinding.ll.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.green2))
                itemBinding.imAdd.setBackgroundResource(R.drawable.txt_background)
                itemBinding.imMinimize.setBackgroundResource(R.drawable.txt_background)
                itemBinding.imAdd.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                itemBinding.imMinimize.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
            } else{
                itemBinding.ll.setBackgroundColor(Color.TRANSPARENT)
            }



            itemBinding.imAdd.setOnClickListener {
                itemBinding.imAdd.setBackgroundResource(R.drawable.txt_background)
                itemBinding.imAdd.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
              itemBinding.apply {
                categoryLl.isVisible = true
                  astbLl.isVisible = true
                  doneLl.isVisible = true
                  inentoryHolderLl.isVisible = true
                  transefredLl.isVisible = true
                  parmanentIssueLl.isVisible = true
                  pivLl.isVisible = true
                  invetoryLl.isVisible = true
                  volumeLl.isVisible = true
                  inventoyrPageLl.isVisible = true
                  ledgerNoLl.isVisible = true
                  departmentLl.isVisible = true
                  inventoryOfficerNameLl.isVisible = true
                  createLl.isVisible = true
                  actionLl.isVisible = true

              }
                itemBinding.imAdd.isVisible = false
                itemBinding.imMinimize.isVisible = true
                notifyDataSetChanged()
            }

            itemBinding.imMinimize.setOnClickListener {
                itemBinding.imMinimize.setBackgroundResource(R.drawable.txt_background)
                itemBinding.imMinimize.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                itemBinding.apply {
                    categoryLl.isVisible = false
                    astbLl.isVisible = false
                    doneLl.isVisible = false
                    inentoryHolderLl.isVisible = false
                    transefredLl.isVisible = false
                    parmanentIssueLl.isVisible = false
                    pivLl.isVisible = false
                    invetoryLl.isVisible = false
                    volumeLl.isVisible = false
                    inventoyrPageLl.isVisible = false
                    ledgerNoLl.isVisible = false
                    departmentLl.isVisible = false
                    inventoryOfficerNameLl.isVisible = false
                    createLl.isVisible = false
                    actionLl.isVisible = false


                }

                itemBinding.imAdd.isVisible = true
                itemBinding.imMinimize.isVisible = false
                notifyDataSetChanged()
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
            mList.sortByDescending { it.status == "Found" }
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
package com.example.smartdrugcart.adapters

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.healthmessage.database.FunctionsLocker
import com.example.smartdrugcart.*
import com.example.smartdrugcart.models.ModelLocker
import java.util.*


class AdapterLogger(
    private val activity: Activity,
    private val dataList: ArrayList<ModelLocker>,
    private var mode: String,
) : RecyclerView.Adapter<AdapterLogger.ViewHolder>() {

    companion object{
        val EVENT_SHOW_INPUTDIALOG = "showInputDialog"
        val EVENT_SHOW_CLEARDIALOG = "showClearDialog"
    }

    private val TAG = "AdapterLogger"
    private val functions = FunctionsLocker(activity)

    private var l: ((event: String, possition: Int) -> Unit?)? = null
    fun setMyEvent(l: (event: String, possition: Int) -> Unit) {
        this.l = l
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.listview_logger, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        Log.i(TAG, "getItemCount " + dataList.size.toString())
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {   //สร้างเงื่อนไข//
        val model = dataList[position]
        //set detail
        when (model.hn) {
            null -> {
                holder.bgCV.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.white))
                holder.iconIV.setImageDrawable(
                    ContextCompat.getDrawable(
                        activity,
                        R.drawable.ic_box
                    )
                )
                holder.hnTV.text = "Empty"
                holder.hnTV.setTextColor(
                    ContextCompat.getColor(
                        activity,
                        R.color.colorWhiteDarkDark
                    )
                )
                holder.counterTV.text = ""
            }
            else -> {
                holder.bgCV.setCardBackgroundColor(
                    ContextCompat.getColor(
                        activity,
                        R.color.colorRed
                    )
                )
                holder.iconIV.setImageDrawable(
                    ContextCompat.getDrawable(
                        activity,
                        R.drawable.ic_pills
                    )
                )
                holder.hnTV.text = model.hn
                holder.hnTV.setTextColor(ContextCompat.getColor(activity, R.color.white))
                holder.counterTV.text = "${model.counter} ครั้ง"
            }
        }
        when (model.state) {
            KEY_LOCK -> {

            }
            KEY_UNLOCK -> {

            }
        }

        //event
        holder.itemLL.setOnClickListener {

            if (position in 4..6) {
                Toast.makeText(activity, "Not Found.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            when (mode) {
                KEY_MODE_REGISTER -> {
                    when (model.hn) {
                        null -> {
                            l?.let { it(EVENT_SHOW_INPUTDIALOG, position) }
                        }
                        else -> {
                            l?.let { it(EVENT_SHOW_CLEARDIALOG, position) }
                        }
                    }
                }
            }
        }
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var bgCV: CardView
        var itemLL: LinearLayout
        var iconIV: ImageView
        var hnTV: TextView
        var counterTV: TextView

        init {
            bgCV = itemView.findViewById(R.id.bgCV)
            itemLL = itemView.findViewById(R.id.itemLL)
            iconIV = itemView.findViewById(R.id.iconIV)
            hnTV = itemView.findViewById(R.id.hnTV)
            counterTV = itemView.findViewById(R.id.counterTV)
        }
    }
}

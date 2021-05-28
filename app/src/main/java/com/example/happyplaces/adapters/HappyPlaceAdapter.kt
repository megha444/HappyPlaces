package com.example.happyplaces.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.R
import com.example.happyplaces.activities.AddHappyyPlace
import com.example.happyplaces.activities.MainActivity
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.models.HappyPlacesModel
import de.hdodenhof.circleimageview.CircleImageView

open class HappyPlaceAdapter(
    private val context: Context,
    private var list: ArrayList<HappyPlacesModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var onClickListener: OnClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_happy_place,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
       val model =list[position]

        if(holder is MyViewHolder){
            holder.itemView.findViewById<CircleImageView>(R.id.iv_place_image_circleview).setImageURI(
                Uri.parse(model.image))
            holder.itemView.findViewById<TextView>(R.id.tvTitleCard).text=model.title
            holder.itemView.findViewById<TextView>(R.id.tvDescriptionCard).text=model.description

            holder.itemView.setOnClickListener{
                if(onClickListener!=null){
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }

    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int){
        val intent= Intent(context, AddHappyyPlace::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])
        activity.startActivityForResult(intent, requestCode)

        notifyItemChanged(position)

    }

   fun removeAt(position: Int){
       val dbHandler= DatabaseHandler(context)
       val isDelete = dbHandler.deleteHappyPlace(list[position])

       if(isDelete>0){
           list.removeAt(position)
           notifyItemRemoved(position)
       }

   }


    override fun getItemCount(): Int {
        return  list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener=onClickListener
    }

    interface OnClickListener{
        fun onClick(position: Int, model: HappyPlacesModel)
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view){

    }
}
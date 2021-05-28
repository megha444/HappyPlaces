package com.example.happyplaces.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.R
import com.example.happyplaces.adapters.HappyPlaceAdapter
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.models.HappyPlacesModel
import com.example.happyplaces.utils.SwipeToDeletCallback
import com.example.happyplaces.utils.SwipeToEditCallback
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {

    private lateinit var fabAddHappyyPlace: FloatingActionButton
    private lateinit var rv_happy_places_list: RecyclerView
    private lateinit var tv_no_records_available: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fabAddHappyyPlace=findViewById(R.id.fabAddHappyPlace)

        rv_happy_places_list=findViewById(R.id.rv_happy_places_list)
        tv_no_records_available=findViewById(R.id.tv_no_records_available)

        fabAddHappyyPlace.setOnClickListener{
            val intent = Intent(this, AddHappyyPlace::class.java)
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }

        getHappyPlacesListFromLocalDB()

    }

    private fun setupHappyPlacesRecyclerView(
        happyPlaceList: ArrayList<HappyPlacesModel>){

        rv_happy_places_list.layoutManager=LinearLayoutManager(this)

        val placesAdapter= HappyPlaceAdapter(this, happyPlaceList)
        rv_happy_places_list.adapter=placesAdapter

        rv_happy_places_list.setHasFixedSize(true)

        placesAdapter.setOnClickListener(object: HappyPlaceAdapter.OnClickListener{
            override fun onClick(position: Int, model: HappyPlacesModel) {
                val intent= Intent(this@MainActivity, HappyPlaceDetail:: class.java)

                intent.putExtra(EXTRA_PLACE_DETAILS, model)
                startActivity(intent)
            }

        })
//EDIT SWIPE HANDLER
        val editSwipeHandler= object :SwipeToEditCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter= rv_happy_places_list.adapter as HappyPlaceAdapter
                adapter.notifyEditItem(this@MainActivity, viewHolder.adapterPosition, ADD_PLACE_ACTIVITY_REQUEST_CODE)
            }
        }

        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(rv_happy_places_list)

//DELETE SWIPE HANDLER
        val deleteSwipeHandler= object :SwipeToDeletCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter= rv_happy_places_list.adapter as HappyPlaceAdapter
                adapter.removeAt(viewHolder.adapterPosition)

                getHappyPlacesListFromLocalDB()
            }
        }

        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(rv_happy_places_list)
    }

    private fun getHappyPlacesListFromLocalDB(){
        val dbHandler=DatabaseHandler(this)
        val getHappyPlaceList = dbHandler.getHappyPlacesListInDb()

        if(getHappyPlaceList.size>0){
            rv_happy_places_list.visibility= View.VISIBLE
            tv_no_records_available.visibility=View.GONE

            setupHappyPlacesRecyclerView(getHappyPlaceList)

        }else{
            rv_happy_places_list.visibility= View.GONE
            tv_no_records_available.visibility=View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode== ADD_PLACE_ACTIVITY_REQUEST_CODE){
            if(resultCode==Activity.RESULT_OK){
                getHappyPlacesListFromLocalDB()
            }else{
                Toast.makeText(this, "Could not add Happy Place", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object{
        var ADD_PLACE_ACTIVITY_REQUEST_CODE=5
        var EXTRA_PLACE_DETAILS="extra_place_details"
    }
}
package com.example.happyplaces.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.example.happyplaces.R
import com.example.happyplaces.models.HappyPlacesModel

class HappyPlaceDetail : AppCompatActivity() {

    private lateinit var toolbar_happy_place_detail: Toolbar
    private lateinit var iv_place_image: ImageView
    private lateinit var tv_description: TextView
    private lateinit var tv_location: TextView
    private lateinit var btn_view_on_map: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_happy_place_detail)

        var happyPlaceDetailModel: HappyPlacesModel? = null

        toolbar_happy_place_detail=findViewById(R.id.toolbar_happy_place_detail)
        iv_place_image=findViewById(R.id.iv_place_image)
        tv_description=findViewById(R.id.tv_description)
        tv_location=findViewById(R.id.tv_location)
        btn_view_on_map=findViewById(R.id.btn_view_on_map)


        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            happyPlaceDetailModel=intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlacesModel
        }

        if(happyPlaceDetailModel!=null){
            setSupportActionBar(toolbar_happy_place_detail)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title= happyPlaceDetailModel.title

            toolbar_happy_place_detail.setNavigationOnClickListener { onBackPressed() }

            iv_place_image.setImageURI(Uri.parse(happyPlaceDetailModel.image))
            tv_description.text=happyPlaceDetailModel.description
            tv_location.text=happyPlaceDetailModel.location


        }

        btn_view_on_map.setOnClickListener {
            val intent= Intent(this, MapActivity::class.java)
            intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, happyPlaceDetailModel)
            startActivity(intent)
        }

    }
}
package com.example.happyplaces.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.location.LocationProvider
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.webkit.PermissionRequest
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.happyplaces.R
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.models.HappyPlacesModel
import com.example.happyplaces.utils.GetAddressFromLatLng
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.NullPointerException
import java.text.SimpleDateFormat
import java.util.*



class AddHappyyPlace : AppCompatActivity(), View.OnClickListener {

    private lateinit var toolbarAddPlace: Toolbar
    private val cal =Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    private var mHappyPlaceDetail: HappyPlacesModel?=null

    private var saveImagetoInternalStorage: Uri? = null
    private var mLatitude: Double=0.0
    private var mLongitude: Double =0.0

    private lateinit var et_date:EditText
    private lateinit var et_title:EditText
    private lateinit var et_location:EditText
    private lateinit var et_description:EditText

    private lateinit var iv_place_image: ImageView

    private lateinit var tv_add_image: TextView
    private lateinit var btn_save: Button

    private lateinit var tv_select_current_location: TextView

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happyy_place)

        toolbarAddPlace=findViewById(R.id.toolbar_add_place)


        setSupportActionBar(toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbarAddPlace.setNavigationOnClickListener { onBackPressed() }

        mFusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)

        if(!Places.isInitialized()){
            Places.initialize(this@AddHappyyPlace, resources.getString(R.string.google_maps_api_key))
        }

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mHappyPlaceDetail=intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlacesModel
        }


        et_date=findViewById(R.id.et_date)
        et_description=findViewById(R.id.et_description)
        et_location=findViewById(R.id.et_location)
        et_title=findViewById(R.id.et_title)

        iv_place_image=findViewById(R.id.iv_place_image)

        tv_add_image=findViewById(R.id.tv_add_image)

        btn_save=findViewById(R.id.btn_save)

        tv_select_current_location= findViewById(R.id.tv_select_current_location)

        dateSetListener=DatePickerDialog.OnDateSetListener{
            view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        updateDateInView()

        if(mHappyPlaceDetail!=null){
            supportActionBar?.title="Edit Happy Place"

            et_title.setText(mHappyPlaceDetail!!.title)
            et_description.setText(mHappyPlaceDetail!!.description)
            et_location.setText(mHappyPlaceDetail!!.location)
            et_date.setText(mHappyPlaceDetail!!.date)
            mLatitude=mHappyPlaceDetail!!.latitude
            mLongitude=mHappyPlaceDetail!!.longitude

            saveImagetoInternalStorage=Uri.parse(mHappyPlaceDetail!!.image)
            iv_place_image.setImageURI(saveImagetoInternalStorage)

            btn_save.text="Update"
        }

        et_date.setOnClickListener(this)
        tv_add_image.setOnClickListener(this)
        btn_save.setOnClickListener(this)
        et_location.setOnClickListener(this)
        tv_select_current_location.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.et_date ->{
                DatePickerDialog(this@AddHappyyPlace,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
            }

            R.id.tv_add_image ->{
                val pictureDialog= AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems= arrayOf("Select Photo from Gallery", "Capture Photo from Camera")
                pictureDialog.setItems(pictureDialogItems){
                    _, which->
                        when(which){
                            0-> choosePhotoFromGallery()
                            1-> takePhotoFromCamera()
                        }
                }.show()
            }

            R.id.btn_save->{
                when{
                    et_title.text.isNullOrEmpty()->{Toast.makeText(this, "Please enter title", Toast.LENGTH_LONG).show()}
                    et_description.text.isNullOrEmpty()->{Toast.makeText(this, "Please enter description", Toast.LENGTH_LONG).show()}
                    et_location.text.isNullOrEmpty()->{Toast.makeText(this, "Please enter location", Toast.LENGTH_LONG).show()}
                    saveImagetoInternalStorage==null->{Toast.makeText(this, "Please select image", Toast.LENGTH_LONG).show()}

                    else->{
                        val happyPlaceModel= HappyPlacesModel(
                            if(mHappyPlaceDetail==null) 0 else mHappyPlaceDetail!!.id,
                            et_title.text.toString(),
                            saveImagetoInternalStorage.toString(),
                            et_description.text.toString(),
                            et_date.text.toString(),
                            et_location.text.toString(),
                            mLatitude,
                            mLongitude
                        )

                        val dbHandler= DatabaseHandler(this)

                        if(mHappyPlaceDetail==null){

                            val addHappyPlace=dbHandler.addHappyPlace(happyPlaceModel)
                            if(addHappyPlace>0){
                                setResult(Activity.RESULT_OK)

                                finish()
                            }else{Toast.makeText(this, "Happy Place Could not be Stored", Toast.LENGTH_SHORT).show()
                            }
                        }
                        else{
                            val updateHappyPlace=dbHandler.updateHappyPlace(happyPlaceModel)
                            if(updateHappyPlace>0){
                                setResult(Activity.RESULT_OK)

                                finish()
                            }else{Toast.makeText(this, "Happy Place Could not be Stored", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            R.id.et_location->{
                try{

                    val fields= listOf(
                        Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)

                    val intent= Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(this@AddHappyyPlace)

                    startActivityForResult(intent, PLACE_AUTO_COMPLETE_REQUEST_CODE)
                }catch(e: Exception){e.printStackTrace()}
            }

            R.id.tv_select_current_location->{
                if(!isLocationEnabled()){
                    Toast.makeText(this, "Your location is inaccessible. Please turn it on", Toast.LENGTH_SHORT).show()

                    val intent=Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }else{

                    Dexter.withActivity(this).withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                        .withListener(object : MultiplePermissionsListener {
                            override fun onPermissionsChecked(report: MultiplePermissionsReport) {

                                if(report.areAllPermissionsGranted()){
                                    Toast.makeText(this@AddHappyyPlace, "Access granted, cannot retrieve location",
                                    Toast.LENGTH_LONG).show()
                                    requestNewLocation()
                                }
                            }

                            override fun onPermissionRationaleShouldBeShown(
                                permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                                token: PermissionToken?
                            ) {
                                showRationaleDialogForPermissions()
                            }
                        }).onSameThread().check()
                }
            }
        }
    }

    private fun isLocationEnabled(): Boolean {

        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }


    @SuppressLint("MissingPermission")
    private fun requestNewLocation(){
        var locationRequest = LocationRequest()
        locationRequest.priority=LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval= 1000
        locationRequest.numUpdates=1

        mFusedLocationProviderClient.requestLocationUpdates(locationRequest, mLocationCallBack, Looper.myLooper())
    }

    private val mLocationCallBack= object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            val mLastLocation : Location = locationResult!!.lastLocation
            mLatitude=mLastLocation.latitude
            mLongitude=mLastLocation.longitude

            runBlocking {
                val addressTask =GetAddressFromLatLng(this@AddHappyyPlace, mLatitude, mLongitude)

                /**
                 *This async {} runs outside the normal program
                 */
                val address = async { addressTask.getAddress() }

                /**
                 * .await() waits for the variable to return from async.
                 * Must use .await() on the variable
                 */
                if (address.await() != "") {
                    et_location.setText(address.await())
                } else {
                    Toast.makeText(
                        this@AddHappyyPlace,
                        "Error, Something Went Wrong",
                        Toast.LENGTH_LONG
                    ).show()
                } }
        }

    }

    private fun updateDateInView(){
        val format= "dd.MM.yyyy"
        val sdf=SimpleDateFormat(format, Locale.getDefault())
        et_date.setText(sdf.format(cal.time).toString())

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== RESULT_OK){
            if(requestCode== GALLERY){
                if(data!=null){
                    val contentUri=data.data
                    try{
                        @Suppress("DEPRECATION")
                        val selectedImageBitmap= MediaStore.Images.Media.getBitmap(this.contentResolver, contentUri)
                        saveImagetoInternalStorage = saveImagetoInternalStorage(selectedImageBitmap)
                        Log.e("Saved Image", "Path:: $saveImagetoInternalStorage")

                        iv_place_image.setImageBitmap(selectedImageBitmap)
                    }catch (e: IOException){e.printStackTrace()
                    Toast.makeText(this@AddHappyyPlace, "Gallery Intent Failed", Toast.LENGTH_SHORT).show()}
                }
            }else if(requestCode== CAMERA){
                try{
                    val thumbNail: Bitmap = data!!.extras!!.get("data") as Bitmap
                    saveImagetoInternalStorage = saveImagetoInternalStorage(thumbNail)
                    Log.e("Saved Image", "Path:: $saveImagetoInternalStorage")
                    iv_place_image.setImageBitmap(thumbNail)


                }catch (e:NullPointerException){e.printStackTrace()}
            }else if(requestCode== PLACE_AUTO_COMPLETE_REQUEST_CODE){

                val place: Place= Autocomplete.getPlaceFromIntent(data!!)
                et_location.setText(place.address)
                mLatitude=place.latLng!!.latitude
                mLongitude=place.latLng!!.longitude
            }
        }else if (resultCode == Activity.RESULT_CANCELED) {
            Log.e("Cancelled", "Cancelled")
        }
    }


    private fun choosePhotoFromGallery(){
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
           )
            .withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport) {

                if(report.areAllPermissionsGranted()){
                 val galleryIntent=Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                 startActivityForResult(galleryIntent, GALLERY)
                }
            }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationaleDialogForPermissions()
                }
        }).onSameThread().check()
    }


    private fun takePhotoFromCamera(){

        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {

                    if(report.areAllPermissionsGranted()){
                        val galleryIntent=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(galleryIntent, CAMERA)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationaleDialogForPermissions()
                }
            }).onSameThread().check()
    }

    private fun showRationaleDialogForPermissions(){
        AlertDialog.Builder(this).setMessage("You have denied permission to access gallery."+
                " To still use Gallery, enable permissions from application settings")
            .setPositiveButton("Go to Settings")
            {
                _ , _ ->
                try{
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri= Uri.fromParts("package", packageName, null)
                    intent.data=uri
                    startActivity(intent)
                }catch (e:ActivityNotFoundException){e.printStackTrace()}
            }.setNegativeButton("Cancel"){
                dialog, _ -> dialog.dismiss()
            }.show()
    }

    private fun saveImagetoInternalStorage(bitmap: Bitmap) : Uri{
        val wrapper=ContextWrapper(applicationContext)
        var file=wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try{
            val stream:OutputStream= FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()

        }catch (e:IOException){e.printStackTrace()}

        return  Uri.parse(file.absolutePath)

    }

    companion object{
        private const val GALLERY=1
        private const val CAMERA=2

        private const val IMAGE_DIRECTORY="HappyPlacesImages"

        private const val PLACE_AUTO_COMPLETE_REQUEST_CODE= 3
    }
}





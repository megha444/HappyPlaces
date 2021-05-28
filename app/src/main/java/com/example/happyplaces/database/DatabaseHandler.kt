package com.example.happyplaces.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.happyplaces.activities.AddHappyyPlace
import com.example.happyplaces.models.HappyPlacesModel

class DatabaseHandler(context: Context):
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

        companion object{
            private const val DATABASE_VERSION=3
            private const val DATABASE_NAME="HappyPlacesDatabase.db"
            private const val TABLE_HAPPY_PLACE="HappyPlacesTable"

            private const val KEY_ID="_id"
            private const val KEY_TITLE="title"
            private const val KEY_IMAGE="image"
            private const val KEY_DESCRIPTION="description"
            private const val KEY_DATE="date"
            private const val KEY_LOCATION="location"
            private const val KEY_LATITUDE="latitude"
            private const val KEY_LONGITUDE="longitude"

        }

    override fun onCreate(db: SQLiteDatabase?) {
        // create table happyplacestable( id Integer primary key,)
        val CREATE_HAPPY_PLACE_TABLE=("Create table "+ TABLE_HAPPY_PLACE+"( "
        + KEY_ID+" INTEGER PRIMARY KEY, "
        + KEY_TITLE+" TEXT, "
        + KEY_IMAGE+" TEXT, "
        + KEY_DESCRIPTION+" TEXT, "
        + KEY_DATE+" TEXT, "
        + KEY_LOCATION+" TEXT, "
        + KEY_LATITUDE+" TEXT, "
        + KEY_LONGITUDE+" TEXT)" )
        Log.d("QUERY", CREATE_HAPPY_PLACE_TABLE)
        db?.execSQL(CREATE_HAPPY_PLACE_TABLE)

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_HAPPY_PLACE")
        onCreate(db)
    }

    fun addHappyPlace(happyPlace: HappyPlacesModel): Long{
        val db=this.writableDatabase

        val contentValues= ContentValues()
        contentValues.put(KEY_TITLE, happyPlace.title)
        contentValues.put(KEY_IMAGE, happyPlace.image)
        contentValues.put(KEY_DESCRIPTION, happyPlace.description)
        contentValues.put(KEY_DATE, happyPlace.date)
        contentValues.put(KEY_LOCATION, happyPlace.location)
        contentValues.put(KEY_LATITUDE, happyPlace.latitude)
        contentValues.put(KEY_LONGITUDE, happyPlace.longitude)

        val result = db.insert(TABLE_HAPPY_PLACE, null, contentValues)

        db.close()

        return result
    }

    fun updateHappyPlace(happyPlace: HappyPlacesModel): Int{
        val db=this.writableDatabase

        val contentValues= ContentValues()
        contentValues.put(KEY_TITLE, happyPlace.title)
        contentValues.put(KEY_IMAGE, happyPlace.image)
        contentValues.put(KEY_DESCRIPTION, happyPlace.description)
        contentValues.put(KEY_DATE, happyPlace.date)
        contentValues.put(KEY_LOCATION, happyPlace.location)
        contentValues.put(KEY_LATITUDE, happyPlace.latitude)
        contentValues.put(KEY_LONGITUDE, happyPlace.longitude)

        val success= db.update(TABLE_HAPPY_PLACE, contentValues, KEY_ID + "=" + happyPlace.id, null)

        db.close()

        return success
    }



    fun getHappyPlacesListInDb():ArrayList<HappyPlacesModel>{
        val happyPlaceList:ArrayList<HappyPlacesModel> = ArrayList()
        val selectQuery= "SELECT * FROM $TABLE_HAPPY_PLACE"
        val db= this.readableDatabase
        try{

            val cursor: Cursor = db.rawQuery(selectQuery, null)
            if(cursor.moveToFirst()){
                do{
                    val place = HappyPlacesModel(
                        cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                        cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                        cursor.getString(cursor.getColumnIndex(KEY_IMAGE)),
                        cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(KEY_DATE)),
                        cursor.getString(cursor.getColumnIndex(KEY_LOCATION)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE))
                    )

                    happyPlaceList.add(place)
                }while(cursor.moveToNext())
            }else{
                Log.e("DATA READ", "No data to be read")
            }
            cursor.close()

        }catch (e:SQLiteException){
            Log.e("IN CATCH", "Entered catch")
            db.execSQL(selectQuery)
            return ArrayList()
        }

        return happyPlaceList
    }

    fun deleteHappyPlace(happyPlace: HappyPlacesModel): Int {
        val db= this.writableDatabase
        val success = db.delete(TABLE_HAPPY_PLACE, KEY_ID + "=" + happyPlace.id, null)
        db.close()

        return success
    }

}
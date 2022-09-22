package com.omerakcinar.kotlinlandmarkbook_basic

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.omerakcinar.kotlinlandmarkbook_basic.databinding.ActivityMainBinding
import com.omerakcinar.kotlinlandmarkbook_basic.databinding.ActivityNewLandmarkBinding
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var database : SQLiteDatabase
    private lateinit var landmarkList : ArrayList<Landmark>
    private lateinit var landmarkAdapter : RecyclerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        landmarkList = ArrayList<Landmark>()
        landmarkAdapter = RecyclerAdapter(landmarkList)
        binding.landmarkRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.landmarkRecyclerView.adapter = landmarkAdapter
        getDataFromDb()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.options_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = Intent(this,NewLandmark::class.java)
        intent.putExtra("target","addNew")
        startActivity(intent)
        return super.onOptionsItemSelected(item)
    }

    fun getDataFromDb(){
        try {
            database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null)
            val cursor = database.rawQuery("SELECT * FROM arts",null)

            val idIx = cursor.getColumnIndex("id")
            val landmarkNameIx = cursor.getColumnIndex("landmarkName")
            val landmarkCityIx = cursor.getColumnIndex("landmarkCity")
            val imageIx = cursor.getColumnIndex("image")

            while (cursor.moveToNext()){
                val id = cursor.getInt(idIx)
                val landmarkName = cursor.getString(landmarkNameIx)
                val landmarkCity = cursor.getString(landmarkCityIx)
                val image = cursor.getBlob(imageIx)
                val landmark = Landmark(id,landmarkName,landmarkCity,image)
                landmarkList.add(landmark)
            }
            landmarkAdapter.notifyDataSetChanged()
            cursor.close()

        }catch (e:Exception){
            e.printStackTrace()
        }

    }

}
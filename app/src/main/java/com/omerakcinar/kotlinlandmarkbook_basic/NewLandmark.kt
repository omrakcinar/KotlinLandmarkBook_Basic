package com.omerakcinar.kotlinlandmarkbook_basic

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.omerakcinar.kotlinlandmarkbook_basic.databinding.ActivityNewLandmarkBinding
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.security.Permission

class NewLandmark : AppCompatActivity() {
    private lateinit var binding: ActivityNewLandmarkBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var selectedBitmap: Bitmap? = null
    private lateinit var database : SQLiteDatabase
    private var selectedId : Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)
        binding = ActivityNewLandmarkBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        registerLauncher()

        val intent = intent
        val target = intent.getStringExtra("target")
        selectedId = intent.getIntExtra("id",1)
        if (target.equals("addNew")){
            binding.landmarkNameText.setText("")
            binding.landmarkCityText.setText("")
            binding.commentText.setText("")
            binding.uploadImageView.setImageResource(R.drawable.uploadimg)
            binding.deleteButton.visibility = View.INVISIBLE
        }else if(target.equals("showItem")){
            binding.saveButton.visibility = View.INVISIBLE
            binding.landmarkNameText.inputType = InputType.TYPE_NULL
            binding.landmarkCityText.inputType = InputType.TYPE_NULL
            binding.commentText.inputType = InputType.TYPE_NULL
            val selectedItemId = intent.getIntExtra("id",1)
            val cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", arrayOf(selectedItemId.toString()))

            var landmarkNameIx = cursor.getColumnIndex("landmarkName")
            var landmarkCityIx = cursor.getColumnIndex("landmarkCity")
            var commentIx = cursor.getColumnIndex("comment")
            var imageIx = cursor.getColumnIndex("image")

            while (cursor.moveToNext()){
                binding.landmarkNameText.setText(cursor.getString(landmarkNameIx))
                binding.landmarkCityText.setText(cursor.getString(landmarkCityIx))
                binding.commentText.setText(cursor.getString(commentIx))

                val byteArray = cursor.getBlob(imageIx)
                val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                binding.uploadImageView.setImageBitmap(bitmap)
            }
            cursor.close()
        }

    }

    fun uploadImage(view: View) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(view, "Permission needed for this action.", Snackbar.LENGTH_INDEFINITE).setAction("Allow", View.OnClickListener {
                    // TODO: SYSTEM NEED TO SHOW RATIONALE. THEN ASK FOR PERMISSION IF USER CLICKS 'ALLOW'
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }).show()
            } else {
                // TODO: SYSTEM DOESNT NEED RATIONALE. ASK FOR PERMISSION DIRECTLY
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else {
            val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            // TODO: LAUNCH THE INTENT WITH RESULTLAUNCHER
            activityResultLauncher.launch(intentToGallery)
        }
    }

    fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val intentFromResult = result.data
                    if (intentFromResult != null) {
                        val imageData = intentFromResult.data
                        if (imageData != null) {
                            try {
                                if (Build.VERSION.SDK_INT >= 28) {
                                    val source = ImageDecoder.createSource(this.contentResolver, imageData)
                                    selectedBitmap = ImageDecoder.decodeBitmap(source)
                                    binding.uploadImageView.setImageBitmap(selectedBitmap)
                                } else {
                                    selectedBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageData)
                                    binding.uploadImageView.setImageBitmap(selectedBitmap)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) {
                    val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)
                } else {
                    Toast.makeText(this, "Permission required!", Toast.LENGTH_LONG).show()
                }
            }
    }

    fun saveLandmark(view: View) {
        val landmarkName = binding.landmarkNameText.text.toString()
        val landmarkCity = binding.landmarkCityText.text.toString()
        val comment = binding.commentText.text.toString()
        if (selectedBitmap != null){
            val resizedBitmap = bitmapResizer(selectedBitmap!!,300)
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray = outputStream.toByteArray()

            try{//ADDING DATAS TO SQLite DATABASE
                database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, landmarkName VARCHAR, landmarkCity VARCHAR, comment VARCHAR, image BLOB)")
                val sqlString =
                    "INSERT INTO arts (landmarkName, landmarkCity, comment, image) VALUES (?,?,?,?)"
                val statement = database.compileStatement(sqlString)
                statement.bindString(1, landmarkName)
                statement.bindString(2, landmarkCity)
                statement.bindString(3, comment)
                statement.bindBlob(4, byteArray)
                statement.execute()
            }catch (e:Exception){
                e.printStackTrace()
            }
        }

        val intent = Intent(this,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)

        Toast.makeText(this,"Saved",Toast.LENGTH_LONG).show()
    }

    fun deleteLandmark(view: View) {
        val alert = AlertDialog.Builder(this)
        alert.setTitle("Delete Landmark")
        alert.setMessage("Are you sure? This action can not be taken back.")
        alert.setNegativeButton("No",DialogInterface.OnClickListener { dialogInterface, i ->  })
        alert.setPositiveButton("Yes",DialogInterface.OnClickListener { dialogInterface, i ->
            val sqlString = "DELETE FROM arts WHERE id = ?"
            val statement = database.compileStatement(sqlString)
            statement.bindString(1,selectedId.toString())
            statement.execute()

            val returnIntent = Intent(this,MainActivity::class.java)
            returnIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(returnIntent)

            Toast.makeText(this,"Deleted",Toast.LENGTH_LONG).show()
        })
        alert.show()

    }

    private fun bitmapResizer(image: Bitmap, maximumSize: Int) : Bitmap{
        var width = image.width
        var height = image.height

        val bitmapRatio : Double = width.toDouble() / height.toDouble()

        if (bitmapRatio > 1){
            //LANDSCAPE
            width = maximumSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        }else{
            //PORTRAIT
            height = maximumSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }

        return Bitmap.createScaledBitmap(image,width,height,true)
    }

}
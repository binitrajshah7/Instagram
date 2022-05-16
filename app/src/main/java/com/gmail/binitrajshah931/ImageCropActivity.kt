package com.gmail.binitrajshah931

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.yalantis.ucrop.UCrop
import java.io.File
import java.lang.StringBuilder
import java.util.*

class ImageCropActivity : AppCompatActivity() {

    private var result: String = String()
    private lateinit var fileUri: Uri


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_crop)

        readIntent()

        var dest_uri =StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString()
        val options = UCrop.Options()

        UCrop.of(fileUri,Uri.fromFile(File(cacheDir,dest_uri)))
            .withOptions(options)
            .withAspectRatio(0F, 0F)
            .useSourceImageAspectRatio()
            .withMaxResultSize(2000,2000)
            .start(this@ImageCropActivity)


    }

    private fun readIntent() {
        result = intent.getStringExtra("key").toString()
        fileUri = Uri.parse(result)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode== RESULT_OK && requestCode== UCrop.REQUEST_CROP){
            val resultUri = UCrop.getOutput(data!!)
            val returnIntent = Intent()
            returnIntent.putExtra("Result",resultUri.toString())
            setResult(-1,returnIntent)
            finish()
        }
        else if(resultCode == UCrop.RESULT_ERROR){
            Toast.makeText(this, "Error fetching uri", Toast.LENGTH_SHORT).show()
        }
    }
}
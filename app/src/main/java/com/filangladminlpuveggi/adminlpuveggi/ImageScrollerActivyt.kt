package com.filangladminlpuveggi.adminlpuveggi

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.filangladminlpuveggi.adminlpuveggi.databinding.ActivityImageScrollerActivytBinding
import com.filangladminlpuveggi.adminlpuveggi.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

class ImageScrollerActivyt : AppCompatActivity() {
    lateinit var b: ActivityImageScrollerActivytBinding
    var selectedImage = mutableListOf<Uri>()

    val productStroge = Firebase.storage.reference
    val firestore = Firebase.firestore

    val emptyList = emptyList<String>()
    lateinit var imageAdapter: imageadapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityImageScrollerActivytBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.imagerv.layoutManager = LinearLayoutManager(this)

        val selectImageActivityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    val intent = it.data

                    if (intent?.clipData != null) {
                        val count = intent.clipData?.itemCount ?: 0
                        (0 until count).forEach {
                            val imageUri = intent.clipData?.getItemAt(it)?.uri
                            imageUri?.let {
                                selectedImage.add(it)
                            }
                        }
                    } else {
                        val imageUri = intent?.data
                        imageUri?.let {
                            selectedImage.add(it)
                        }


                    }
                    updateImageFunction()
                }
            }

        b.buttonImagesPicker.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.type = "image/*"
            selectImageActivityResult.launch(intent)
        }

        b.upload.setOnClickListener {
            saveProducts()
        }

    }

    private fun updateImageFunction() {

        imageAdapter = imageadapter(this, selectedImage) { po ->
            selectedImage.removeAt(po)
            imageAdapter.notifyDataSetChanged()
        }
        b.imagerv.adapter = imageAdapter

    }

    private fun saveProducts() {

        val imagesByteArrays = getImagesByteArrays()
        var images = mutableListOf<String>()

        lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {

            }

            try {
                async {
                    imagesByteArrays.forEach {
                        launch {
                            val id = UUID.randomUUID().toString()
                            val imageStroage = productStroge.child("scroller/images/$id")
                            val result = imageStroage.putBytes(it).await()
                            val downloadUrl = result.storage.downloadUrl.await().toString()
                            images.add(downloadUrl)
                        }
                    }
                }.await()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {

                }
            }


            val product = srollerdataclass(
                images
            )

            firestore.collection("Main Page Scroller").add(product)
                .addOnSuccessListener {
                    images = emptyList.toMutableList()
                    startActivity(Intent(this@ImageScrollerActivyt, MainActivity::class.java))
                }.addOnFailureListener {
                    Toast.makeText(this@ImageScrollerActivyt, "Something went wrong", Toast.LENGTH_SHORT)
                        .show()
                    images = emptyList.toMutableList()
                }
        }


    }


    private fun getImagesByteArrays(): List<ByteArray> {
        val imageByteArray = mutableListOf<ByteArray>()
        selectedImage.forEach {
            val stream = ByteArrayOutputStream()
            val imagebitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
            if (imagebitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)) {
                imageByteArray.add(stream.toByteArray())
            }
        }
        return imageByteArray

    }


}
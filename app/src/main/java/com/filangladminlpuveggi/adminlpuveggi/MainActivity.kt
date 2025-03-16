package com.filangladminlpuveggi.adminlpuveggi

import android.R
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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

class MainActivity : AppCompatActivity() {
    lateinit var b : ActivityMainBinding
    var selectedImage = mutableListOf<Uri>()

    val productStroge = Firebase.storage.reference
    val firestore = Firebase.firestore

    val emptyList  = emptyList<String>()
    lateinit var imageAdapter : imageadapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.viewItems.setOnClickListener {
            startActivity(Intent(this , productActivity::class.java))

        }
        b.scrolImagesbtn.setOnClickListener {
            startActivity(Intent(this , ImageScrollerActivyt::class.java))
        }

      b.imagerv.layoutManager = LinearLayoutManager(this , LinearLayoutManager.HORIZONTAL , false)

        val options = arrayOf("Fruits", "Vegetables" , "Ornamentals")
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        b.spinner.adapter = adapter

        b.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedOption = options[position]
                b.edCategory.text = selectedOption
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        val selectImageActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if(it.resultCode ==  RESULT_OK){
                val intent =  it.data

                if(intent?.clipData != null){
                    val count =  intent.clipData?.itemCount ?: 0
                    (0 until count).forEach{
                        val imageUri =  intent.clipData?.getItemAt(it)?.uri
                        imageUri?.let {
                            selectedImage.add(it)
                        }
                    }
                }else{
                    val imageUri = intent?.data
                    imageUri?.let {
                        selectedImage.add(it)
                    }


                }
                updateImageFunction()
            }
        }

        b.buttonImagesPicker.setOnClickListener {
            val intent =  Intent(Intent.ACTION_GET_CONTENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.type = "image/*"
            selectImageActivityResult.launch(intent)
        }

        b.save.setOnClickListener {
            saveProducts()
            startActivity(Intent(this , MainActivity::class.java))
        }

    }

    private fun updateImageFunction() {

         imageAdapter = imageadapter(this , selectedImage){ po ->
             selectedImage.removeAt(po)
            imageAdapter.notifyDataSetChanged()
        }
        b.imagerv.adapter =  imageAdapter

    }

    private fun saveProducts() {
        if(validatieIformation()){
            val name = b.edName.text.toString().trim()
            val category =  b.edCategory.text.toString().trim()
            val price =  b.edPrice.text.toString()
            val offerPercentage =  b.offerPercentage.text.toString().trim()
            val description =  b.edDescription.text.toString().trim()
            val size =  b.edSizes.text.toString().trim()
            val sizesingrams =  getSizeList(b.edQuantityInGrams.text.toString().trim())
            val imagesByteArrays = getImagesByteArrays()
            var images = mutableListOf<String>()

            lifecycleScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main){
                    showloading()
                }

                try {
                    async {
                        imagesByteArrays.forEach{
                            launch {
                                val id = UUID.randomUUID().toString()
                                val imageStroage = productStroge.child("products/images/$name/$id")
                                val result =  imageStroage.putBytes(it).await()
                                val downloadUrl = result.storage.downloadUrl.await().toString()
                                images.add(downloadUrl)
                            }
                        }
                    }.await()
                }catch(e: java.lang.Exception){
                    e.printStackTrace()
                    withContext(Dispatchers.Main){
                        hideLoading()
                    }
                }


                val product = Product(
                    UUID.randomUUID().toString(),
                    name,
                    name,
                    category,
                    price,
                    size,
                    if(offerPercentage.isEmpty()) null else offerPercentage.toFloat(),
                    if(description.isEmpty()) null else description,
                    sizesingrams,
                    images
                )

                firestore.collection("Products").document(name).set(product)
                    .addOnSuccessListener {
                        hideLoading()
                        b.edName.text.clear()
                        b.edPrice.text.clear()
                        b.offerPercentage.text.clear()
                        b.edDescription.text.clear()
                        images = emptyList.toMutableList()


                    }.addOnFailureListener {
                        hideLoading()
                        Toast.makeText(this@MainActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
                        b.edName.text.clear()
                        b.edCategory.text = null
                        b.edPrice.text.clear()
                        b.offerPercentage.text.clear()
                        b.edDescription.text.clear()
                        images = emptyList.toMutableList()
                        updateImageFunction()
                    }
            }
        }else{
            Toast.makeText(this, "Please enter all required data", Toast.LENGTH_SHORT).show()
        }


    }

    private fun hideLoading() {
        b.progressBar.visibility = View.GONE
    }

    private fun showloading() {
        b.progressBar.visibility = View.VISIBLE
    }

    private fun getImagesByteArrays(): List<ByteArray> {
        val imageByteArray =  mutableListOf<ByteArray>()
        selectedImage.forEach{
            val stream = ByteArrayOutputStream()
            val imagebitmap = MediaStore.Images.Media.getBitmap(contentResolver , it)
            if(imagebitmap.compress(Bitmap.CompressFormat.JPEG, 100 , stream)){
                imageByteArray.add(stream.toByteArray())
            }
        }
        return imageByteArray

    }

    private fun getSizeList(trim: String) : List<String>?{
        if(trim.isEmpty()){
            return null
        }
        val sizeList = trim.split(",")

        return sizeList
    }

    private fun validatieIformation(): Boolean {
        if(b.edPrice.text.toString().trim().isEmpty())
            return false
        if(b.edName.text.toString().trim().isEmpty()) {
            return false
        }
        if(b.edCategory.text.toString().trim().isEmpty()) {
            return false

        }
        if(selectedImage.isEmpty()){
            return false
        }
        return true
    }
}
package com.filangladminlpuveggi.adminlpuveggi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.filangladminlpuveggi.adminlpuveggi.databinding.ActivityProductBinding
import com.google.firebase.firestore.FirebaseFirestore

class productActivity : AppCompatActivity() {
    lateinit var  b : ActivityProductBinding
    lateinit var firebasestrore : FirebaseFirestore
   var d =  ArrayList<Product>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityProductBinding.inflate(layoutInflater)
        setContentView(b.root)

        firebasestrore =  FirebaseFirestore.getInstance()
        b.rv.layoutManager =  LinearLayoutManager(this)

        b.viewItems.setOnClickListener {
            b.progressBar.visibility = View.VISIBLE
            firebasestrore.collection("Products").get()
                .addOnSuccessListener { task ->
                    try {
                        for(i in task){
                            var  data =  i.toObject(Product::class.java)
                            d.add(data)
                        }

                        var adapter =  adapter(this , d)
                        b.rv.adapter = adapter
                        b.progressBar.visibility = View.GONE
                    }catch(e : Exception){
                        Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
                    }

                }.addOnFailureListener {
                    Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show()
                }
        }

    }

}



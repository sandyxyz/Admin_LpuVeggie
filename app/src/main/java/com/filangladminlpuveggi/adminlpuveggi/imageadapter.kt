package com.filangladminlpuveggi.adminlpuveggi

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.filangladminlpuveggi.adminlpuveggi.databinding.ImageLayoutBinding
import com.filangladminlpuveggi.adminlpuveggi.databinding.ItemLayoutBinding
import com.google.android.play.integrity.internal.c
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage

class imageadapter(var c : Context, var list : MutableList<Uri>  , var onclick : (Int)-> Unit) : Adapter<imageadapter.vhol>() {
    inner class vhol(v : View) : ViewHolder(v){
        var b = ImageLayoutBinding.bind(v)
    }

//    val diff = object : DiffUtil.ItemCallback<Product>(){
//        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
//           return oldItem.id == newItem.id
//        }
//
//        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
//            return oldItem ==  newItem
//        }
//
//    }
//    val differ = AsyncListDiffer(this, diff)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): vhol {
        return vhol(LayoutInflater.from(c).inflate(R.layout.image_layout , parent , false))
    }

    override fun getItemCount(): Int {
       return list.size
    }

    override fun onBindViewHolder(holder: vhol, position: Int) {
       val i =  list[position]
       Glide.with(c).load(i).into(holder.b.image)
      holder.b.cross.setOnClickListener {
          onclick.invoke(position)

      }
   }
}
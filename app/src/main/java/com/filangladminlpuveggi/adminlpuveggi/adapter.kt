package com.filangladminlpuveggi.adminlpuveggi

import android.app.AlertDialog
import android.app.Dialog
import android.app.Person
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.filangladminlpuveggi.adminlpuveggi.databinding.ItemLayoutBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.util.UUID

class adapter(var c : Context, var list : ArrayList<Product>) : Adapter<adapter.vhol>() {


    lateinit var imgadapter  : imageadapter


    inner class vhol(v : View) : ViewHolder(v){
        var b = ItemLayoutBinding.bind(v)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): vhol {
        return vhol(LayoutInflater.from(c).inflate(R.layout.item_layout , parent , false))
    }

    override fun getItemCount(): Int {
       return list.size
    }

    override fun onBindViewHolder(holder: vhol, position: Int) {
       val i =  list[position]
        Glide.with(c).load(i.images[0].toString()).into(holder.b.imageView)
        holder.b.name.text = i.name.toString()
        holder.b.price.text = "₹"+i.price.toString()

        holder.b.delete.setOnClickListener {
            val a = AlertDialog.Builder(c)
            a.setCancelable(false)
            a.setTitle("Confirmation!")
            a.setMessage("Are you sure you want to delete this item?")
            a.setPositiveButton("Yes"){ d,_ ->
                try {
                    var storage = FirebaseStorage.getInstance()
                     var ref = storage.reference.child("products/images/${i.name2.toString()}")
                    Toast.makeText(c, ref.toString(), Toast.LENGTH_LONG).show()
                        ref.delete().addOnSuccessListener {
                            Toast.makeText(c, "Data Deleted on storage", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            Toast.makeText(c, "Some thing went wrong in storage", Toast.LENGTH_SHORT).show()
                        }
                   var fr =  FirebaseFirestore.getInstance()
                       fr.collection("Products").document(i.name2).delete()
                        .addOnSuccessListener {
                            Toast.makeText(c, "Data Deleted", Toast.LENGTH_SHORT).show()

                        }.addOnFailureListener {
                            Toast.makeText(c, "Some thing went wrong", Toast.LENGTH_SHORT).show()
                        }


                }catch (e : Exception){
                    Toast.makeText(c, e.toString(), Toast.LENGTH_SHORT).show()
                }

               list.removeAt(position)
                notifyDataSetChanged()
                d.dismiss()
            }
            .setNegativeButton("No"){ d,_ ->
                    d.dismiss()
            }

            a.create().show()
        }

        holder.b.edit.setOnClickListener { task ->
                var dialog = Dialog(c)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                var a = LayoutInflater.from(c).inflate(R.layout.update_layout , null)

                dialog.setContentView(a)

                var btn  =  a.findViewById<Button>(R.id.save)
                var imagerv  =  a.findViewById<RecyclerView>(R.id.imagerv)
                var imgbtn  =  a.findViewById<Button>(R.id.buttonImagesPicker)
                var cate  =  a.findViewById<TextView>(R.id.edCategory)
                var spinner  =  a.findViewById<Spinner>(R.id.spinner)
                var name  =  a.findViewById<EditText>(R.id.edName)
                var desc  =  a.findViewById<EditText>(R.id.edDescription)
                var price  =  a.findViewById<EditText>(R.id.edPrice)
                var offerPerce  =  a.findViewById<EditText>(R.id.offerPercentage)
                var size  =  a.findViewById<EditText>(R.id.edSizes)
                var quantity  =  a.findViewById<EditText>(R.id.edQuantityInGrams)

             var images = mutableListOf<Uri>()
             for(i in i.images){
                 var s  = Uri.parse(i)
                 images.add(s)
             }

              imagerv.layoutManager =  LinearLayoutManager(c , LinearLayoutManager.HORIZONTAL , false)
              imgadapter = imageadapter(c , images){onclick ->
                  Toast.makeText(c, "hello", Toast.LENGTH_SHORT).show()
              }
            imagerv.adapter = imgadapter
               name.setText(i.name)
               desc.setText(i.description)
               price.setText(i.price)
               offerPerce.setText(i.offerPercentage.toString())
               size.setText(i.size)

               var ad =  ArrayList<String>()
               for(i in i.sizes!!){
                ad.add(i)
                }
                quantity.setText(ad.joinToString(","))
               getspinner(cate , spinner)

               cate.text = i.category

              // var getsizes = getSizeList(quantity.text.toString().trim())

               btn.setOnClickListener {
                   try {
                       val up = hashMapOf(
                           "name" to name.text.toString(),
                           "category" to cate.text.toString(),
                           "price" to price.text.toString(),
                           "size" to  size.text.toString(),
                           "offerPercentage" to if(offerPerce.text.isEmpty()) null else offerPerce.text.toString().toFloat(),
                           "description" to if(desc.text.isEmpty()) null else desc.text.toString(),
                           "sizes" to getSizeList(quantity.text.toString().trim())

                       )
                       var fr =  FirebaseFirestore.getInstance()
                       fr.collection("Products").document(i.name2).update(up).addOnSuccessListener {
                           notifyDataSetChanged()
                           dialog.dismiss()
                       }.addOnFailureListener {
                           Toast.makeText(c, "failed", Toast.LENGTH_SHORT).show()
                           dialog.dismiss()
                       }

                   }catch(e : Exception){
                       Toast.makeText(c, e.toString(), Toast.LENGTH_SHORT).show()
                       dialog.dismiss()
                   }



                   imgbtn.setOnClickListener {

                   }

                }
                var cross  =  a.findViewById<ImageView>(R.id.cross)

                cross.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
                dialog.window?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.window?.setGravity(Gravity.BOTTOM)

            }
    }
fun getspinner(text : TextView , spinner : Spinner){
    val options = arrayOf("Fruits", "Vegetables", "Ornamentals")
    val adapter = ArrayAdapter(c, android.R.layout.simple_spinner_item, options)
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    spinner.adapter = adapter
    spinner.setSelection(0)
    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            val selectedOption = options[position]
            text.text = selectedOption
        }

        override fun onNothingSelected(parent: AdapterView<*>) {

        }
    }
}
    private fun getSizeList(trim: String) : List<String>?{
        if(trim.isEmpty()){
            return null
        }
        val sizeList = trim.split(",")

        return sizeList
    }
}
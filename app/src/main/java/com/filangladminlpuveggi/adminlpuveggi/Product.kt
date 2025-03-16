package com.filangladminlpuveggi.adminlpuveggi

data class Product(
    val id: String,
    val name2 :  String,
    val name: String,
    val category: String,
    val price: String,
    val size: String,
    val offerPercentage: Float? = null,
    val description: String? = null,
    val sizes: List<String>? = null,
    val images: List<String>
){
    constructor(): this("0","","","","","", images = emptyList())
}
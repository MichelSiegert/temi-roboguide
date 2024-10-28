package de.fhkiel.temi.robogguide.media

fun getID(url: String): String{
    val urlFrags  = url.split("/")
    return urlFrags[urlFrags.size - 1].split("?")[0];
}
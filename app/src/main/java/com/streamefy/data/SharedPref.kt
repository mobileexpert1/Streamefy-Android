package com.streamefy.data

import android.content.Context
import android.content.SharedPreferences

object SharedPref {

    private const val PREF_NAME = "lumakin_preference"

    private lateinit var sharedPreferences: SharedPreferences

    // Initialize the SharedPreferences
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // Save String value
    fun setString(key: String, value: String) {
        with(sharedPreferences.edit()) {
            putString(key, value)
            apply()
        }
    }

    // Get String value
    fun getString(key: String): String? {
        return sharedPreferences.getString(key, "")
    }

    // Save Boolean value
    fun setBoolean(key: String, value: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(key, value)
            apply()
        }
    }

    // Get Boolean value
    fun getBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    // Clear all data
    fun clearData() {
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
    }

    // Uncomment and use the following methods if you need them
    /*
    fun saveArrayList(key: String, list: ArrayList<String>) {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(list)
        editor.putString(key, json)
        editor.apply()
    }

    fun getArrayList(key: String): ArrayList<String>? {
        val json = sharedPreferences.getString(key, null)
        val type = object : TypeToken<ArrayList<String>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun saveMyDataList(key: String, list: List<IntervalModel>) {
        val gson = Gson()
        val json = gson.toJson(list)
        with(sharedPreferences.edit()) {
            putString(key, json)
            apply()
        }
    }

    fun getMyDataList(key: String): List<IntervalModel>? {
        val json = sharedPreferences.getString(key, null)
        val type = object : TypeToken<List<IntervalModel>>() {}.type
        return Gson().fromJson(json, type)
    }
    */
}

package com.streamefy.data

import android.content.Context
import android.content.SharedPreferences

class SharedPref(private val context: Context) {

    companion object {
        private const val PREF_NAME = "lumakin_preference"
    }

    private val sharedPreferences: SharedPreferences
        get() = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // Save String value
    fun setString(varName: String, varValue: String) {
        with(sharedPreferences.edit()) {
            putString(varName, varValue)
            apply()
        }
    }

    // Get String value
    fun getString(varName: String): String? {
        return sharedPreferences.getString(varName, "")
    }

    // Save Boolean value
    fun setBoolean(varName: String, varValue: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(varName, varValue)
            apply()
        }
    }

    // Get Boolean value
    fun getBoolean(varName: String): Boolean {
        return sharedPreferences.getBoolean(varName, false)
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
    fun saveArrayList(key: String, customArrayList: ArrayList<String>) {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(customArrayList)
        editor.putString(key, json)
        editor.apply()
    }

    fun getArrayList(key: String): ArrayList<String>? {
        val json = sharedPreferences.getString(key, "")
        val type = object : TypeToken<ArrayList<String>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun saveMyDataList(myDataList: List<IntervalModel>) {
        val gson = Gson()
        val json = gson.toJson(myDataList)
        with(sharedPreferences.edit()) {
            putString(KEY_MY_DATA_LIST, json)
            apply()
        }
    }

    fun getMyDataList(): List<IntervalModel>? {
        val json = sharedPreferences.getString(KEY_MY_DATA_LIST, "")
        val type = object : TypeToken<List<IntervalModel>>() {}.type
        return Gson().fromJson(json, type)
    }
    */

}

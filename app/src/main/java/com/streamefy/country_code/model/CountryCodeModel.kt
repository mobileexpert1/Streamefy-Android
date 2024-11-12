package com.streamefy.country_code.model
import android.content.Context
import android.util.Log
import android.util.Xml
import java.io.IOException
import java.io.InputStream
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.util.*
data class CountryCodeModel(
    var nameCode:String="",
    var phoneCode:String="",
    var name:String="",
    var englishName:String="",
    var flagResID:String="",
    var dialingLength:String="",
    var isSelected:Boolean=false
)


enum class Language {
    ENGLISH, SPANISH, FRENCH, // Add other languages here
}

object CountryPicker {

    var dialogTitle: String = "Select a country"
    var searchHintMessage: String = "Search..."
    var noResultFoundAckMessage: String = "Results not found"
    var loadedLibraryMaterList: List<CountryCodeModel> = emptyList()
    var loadedLibraryMasterListLanguage: Language = Language.ENGLISH

    fun loadDataFromXML(context: Context) {
        val countries = mutableListOf<CountryCodeModel>()
        var tempDialogTitle = ""
        var tempSearchHint = ""
        var tempNoResultAck = ""

        try {
            // Set up the XML parser
            val xmlFactoryObject = Xml.newPullParser()
//            a_new_lang_template
            // Load the XML resource from the raw folder
            val resourceId = context.resources.getIdentifier("ccp_danish", "raw", context.packageName)
            val inputStream: InputStream = context.resources.openRawResource(resourceId)

            // Set the input for the XML parser
            xmlFactoryObject.setInput(inputStream, "UTF-8")

            // Initialize the first event type to move the parser to the first event
            var eventType = xmlFactoryObject.next() // Move to next event
            Log.e("XMLParser", "Initial event type: $eventType")

            // Check for empty or malformed XML
            if (eventType == XmlPullParser.END_DOCUMENT) {
                Log.e("XMLParser", "Empty or invalid XML document.")
                throw XmlPullParserException("XML document is empty or invalid.")
            }

            var currentCountry: CountryCodeModel? = null

            // Parse the document
            while (eventType != XmlPullParser.END_DOCUMENT) {
                val name = xmlFactoryObject.name
                Log.e("XMLParser", "Current event: $eventType, Current tag: $name  gdgd $xmlFactoryObject")

                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (name) {
                            "country" -> {
                                // Process <country> tag
                                currentCountry = CountryCodeModel(
                                    name = xmlFactoryObject.getAttributeValue(null, "name"),
                                    englishName = xmlFactoryObject.getAttributeValue(null, "english_name"),
                                    nameCode = xmlFactoryObject.getAttributeValue(null, "name_code").toUpperCase(),
                                    phoneCode = xmlFactoryObject.getAttributeValue(null, "phone_code"),
                                   // dialingLength = xmlFactoryObject.getAttributeValue(null, "dialing_length"),
                                   // flagResID = xmlFactoryObject.getAttributeValue(null, "flag") ?: ""
                                )
                                Log.d("XMLParser", "Country found: ${currentCountry?.name}")
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        // End of a <country> tag
                        if (name == "country" && currentCountry != null) {
                            countries.add(currentCountry)
                            currentCountry = null
                            Log.d("XMLParser", "Country added to list.")
                        }
                    }
                }
                eventType = xmlFactoryObject.next() // Move to the next event
            }

        } catch (e: XmlPullParserException) {
            e.printStackTrace()
            Log.e("XMLParser", "XML parsing error: ${e.message}")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("XMLParser", "I/O error: ${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("XMLParser", "Unexpected error: ${e.message}")
        }

        // Handle the fallback logic when no countries were loaded
        if (countries.isEmpty()) {
            loadedLibraryMasterListLanguage = Language.ENGLISH
            countries.addAll(getLibraryMasterCountriesEnglish()) // Fallback to English countries
        }

        // Set the final translations for dialogTitle, searchHint, etc.
        dialogTitle = if (tempDialogTitle.isNotEmpty()) tempDialogTitle else "Select a country"
        searchHintMessage = if (tempSearchHint.isNotEmpty()) tempSearchHint else "Search..."
        noResultFoundAckMessage = if (tempNoResultAck.isNotEmpty()) tempNoResultAck else "Results not found"

        // Sort the countries alphabetically by their names or another attribute as needed
       // loadedLibraryMaterList = countries.sortedBy { it.name }
    }

    // Placeholder for method to load English country list, you may need to implement it
    private fun getLibraryMasterCountriesEnglish(): List<CountryCodeModel> {
        // Return a default list of countries (this is just an example)
        return listOf(
            CountryCodeModel("Andorra", "Andorra", "AD", "376", "6", "@drawable/flag_andorra"),
            CountryCodeModel("United Arab Emirates", "UAE", "AE", "971", "9", "@drawable/flag_united_arab_emirates")
        )
    }
}


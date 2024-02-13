package com.example.listofcountries

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import coil.compose.rememberImagePainter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable
import org.json.JSONArray
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color


// A data class to represent a country with its name, flag, and currency information.
@Serializable
data class Country(
    val name: Name, // Nested data class for the country's name details.
    val flags: Flags, // Nested data class for the country's flag image URL.
    val currencies: Map<String, Currency> // A map of currency codes to currency details.
)

// Data class for holding the common and official names of a country.
@Serializable
data class Name(
    val common: String, // The common name of the country, used informally.
    val official: String // The official name of the country, used formally.
)

// Data class for holding the URL to the country's flag image.
@Serializable
data class Flags(
    val png: String // URL to the PNG image of the country's flag.
)

// Data class for representing a currency, including its name and symbol.
@Serializable
data class Currency(
    val name: String, // Name of the currency.
    val symbol: String // Symbol of the currency.
)

// A composable function to display a list of countries with a search bar.
@Composable
fun CountryListScreen(navController: NavController) {
    // State for managing the search text input by the user. Initially, it's empty.
    var searchText by remember { mutableStateOf("") }

    // State for holding the list of countries. Initially, it's set to null to indicate that data is being loaded.
    val countries = remember { mutableStateOf<List<Country>?>(null) }

    // Remember a coroutine scope tied to the CountryListScreen's lifecycle.
    val coroutineScope = rememberCoroutineScope()

    // Fetch the list of countries when the composable is first launched.
    LaunchedEffect(true) {
        coroutineScope.launch {
            // Asynchronously fetch the countries and store them in the state.
            val countryData = fetchCountries()
            countries.value = countryData
        }
    }

    // Arrange the search bar and the list of countries vertically.
    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar allowing the user to filter countries by name.
        SearchBar(searchText = searchText, onSearchTextChanged = { searchText = it })

        // Check if the countries data is still loading.
        if (countries.value == null) {
            // Display a circular progress indicator while the countries data is being loaded.
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Filter countries based on the current search text.
            val filteredCountries = countries.value!!.filter {
                it.name.common.contains(searchText, ignoreCase = true)
            }

            // Display the filtered list of countries.
            LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                items(filteredCountries) { country ->
                    // Display each country as a list item that can be clicked.
                    CountryListItem(country = country) {
                        // Navigate to the country's detail screen when a list item is clicked.
                        navController.navigate("countryInfo/${Uri.encode(country.name.common)}")
                    }
                }
            }
        }
    }
}

// Composable function for the search bar UI.
@Composable
fun SearchBar(searchText: String, onSearchTextChanged: (String) -> Unit) {
    TextField(
        value = searchText, // Current search text.
        onValueChange = onSearchTextChanged, // Update search text state on change.
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        placeholder = { Text("Search") }, // Placeholder text.
        singleLine = true // Limit to a single line input.
        // Default TextField colors are used.
    )
}

// Composable function to display a country item in the list.
@Composable
fun CountryListItem(country: Country, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        shape = MaterialTheme.shapes.medium, // Medium rounded corners.
        color = MaterialTheme.colorScheme.surfaceVariant, // Surface color variant for background.
        shadowElevation = 4.dp // Elevation for shadow.
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Display the country's flag.
            Image(
                painter = rememberImagePainter(country.flags.png),
                contentDescription = "Flag of ${country.name.common}",
                modifier = Modifier
                    .size(80.dp)
                    .padding(end = 10.dp)
            )
            Column {
                // Display the common and official names of the country.
                Text(
                    text = country.name.common,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = country.name.official,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

// Suspending function to fetch the list of countries from an API.
suspend fun fetchCountries(): List<Country> = withContext(Dispatchers.IO) {
    val url = "https://restcountries.com/v3.1/all?fields=name,flags,currencies"
    val response = try {
        URL(url).readText() // Attempt to fetch data from the API.
    } catch (e: Exception) {
        e.printStackTrace()
        return@withContext emptyList<Country>() // Return an empty list on failure.
    }

    val countries = mutableListOf<Country>()
    val jsonArray = JSONArray(response) // Parse the JSON response.
    for (i in 0 until jsonArray.length()) {
        val jsonObject = jsonArray.getJSONObject(i)
        // Extract country name, flag, and currencies information.
        val nameObject = jsonObject.getJSONObject("name")
        val commonName = nameObject.getString("common")
        val officialName = nameObject.getString("official")
        val flagsObject = jsonObject.getJSONObject("flags")
        val pngUrl = flagsObject.getString("png")
        val currenciesObject = jsonObject.getJSONObject("currencies")
        val currencies = currenciesObject.keys().asSequence().map { key ->
            val currencyObject = currenciesObject.getJSONObject(key)
            val currencyName = currencyObject.getString("name")
            val currencySymbol = currencyObject.optString("symbol", "")
            key to Currency(currencyName, currencySymbol)
        }.toMap()

        countries.add(Country(
            name = Name(commonName, officialName),
            flags = Flags(pngUrl),
            currencies = currencies
        ))
    }

    // Sort the list of countries by their common name for display.
    return@withContext countries.sortedBy { it.name.common }
}

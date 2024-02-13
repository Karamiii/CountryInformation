package com.example.listofcountries

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL

// Displays detailed information about a country.
@Composable
fun CountryInfoScreen(countryName: String) {
    val countryDetails = remember { mutableStateOf<CountryDetail?>(null) } // State to hold country details
    val uriHandler = LocalUriHandler.current // To handle external URIs, like opening a link in a browser

    // Fetch detailed country information when countryName changes or on initial composition
    LaunchedEffect(countryName) {
        countryDetails.value = fetchCountryDetail(countryName)
    }

    countryDetails.value?.let { detail ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                // Display country flag with a border
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .border(2.dp, Color.Gray, MaterialTheme.shapes.medium) // Add a border around the Box
                ) {
                    Image(
                        painter = rememberImagePainter(detail.flagUrl),
                        contentDescription = "Flag of ${detail.name}",
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                // Display various details about the country
                DetailItem(title = "Common Name:", detail = detail.name)
                DetailItem(title = "Official Name:", detail = detail.officialName)
                DetailItem(title = "Region:", detail = detail.region)
                DetailItem(title = "Subregion:", detail = detail.subregion)
                DetailItem(title = "Languages:", detail = detail.languages.values.joinToString())
                DetailItem(title = "Currencies:", detail = detail.currencies.values.joinToString { "${it.name} (${it.symbol})" })
                DetailItem(title = "Timezones:", detail = detail.timezones.joinToString())
                if (detail.borders.isNotEmpty()) {
                    DetailItem(title = "Bordering countries:", detail = detail.borders.joinToString())
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Link to view the country on Google Maps
                Text(
                    "View on Google Maps",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    ),
                    modifier = Modifier
                        .clickable { uriHandler.openUri(detail.googleMapsLink) }
                        .padding(8.dp)
                )
            }
        }
    } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator() // Show loading indicator if country details are null
    }
}

// A helper Composable to display a detail item with a title and detail text
@Composable
fun DetailItem(title: String, detail: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = "$title ",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

// Data class representing detailed information about a country
data class CountryDetail(
    val name: String,
    val officialName: String,
    val region: String,
    val subregion: String,
    val languages: Map<String, String>,
    val currencies: Map<String, Currency>,
    val timezones: List<String>,
    val googleMapsLink: String,
    val flagUrl: String,
    val borders: List<String>
)

// Fetches detailed information about a country asynchronously
suspend fun fetchCountryDetail(countryName: String): CountryDetail? = withContext(Dispatchers.IO) {
    val url = "https://restcountries.com/v3.1/name/$countryName?fullText=true"
    val response = try {
        URL(url).readText()
    } catch (e: Exception) {
        e.printStackTrace()
        return@withContext null
    }

    val jsonArray = JSONArray(response)
    if (jsonArray.length() == 0) return@withContext null

    val jsonObject = jsonArray.getJSONObject(0)

    // Parse JSON to extract country details
    val name = jsonObject.getJSONObject("name").getString("common")
    val officialName = jsonObject.getJSONObject("name").getString("official")
    val region = jsonObject.getString("region")
    val subregion = jsonObject.getString("subregion")
    val languages = jsonObject.getJSONObject("languages").let { langObj ->
        langObj.keys().asSequence().associateWith { langObj.getString(it) }
    }
    val currencies = jsonObject.getJSONObject("currencies").let { currObj ->
        currObj.keys().asSequence().associate { key ->
            key to Currency(currObj.getJSONObject(key).getString("name"), currObj.getJSONObject(key).getString("symbol"))
        }
    }
    val timezones = jsonObject.getJSONArray("timezones").let { tzArray ->
        List(tzArray.length()) { tzArray.getString(it) }
    }
    val googleMapsLink = jsonObject.getJSONObject("maps").getString("googleMaps")
    val flagUrl = jsonObject.getJSONObject("flags").getString("png")
    val borders = jsonObject.optJSONArray("borders")?.let { bordersArray ->
        List(bordersArray.length()) { bordersArray.getString(it) }
    } ?: emptyList() // Handle optional borders array

    // Return a CountryDetail object with fetched data
    CountryDetail(name, officialName, region, subregion, languages, currencies, timezones, googleMapsLink, flagUrl, borders)
}
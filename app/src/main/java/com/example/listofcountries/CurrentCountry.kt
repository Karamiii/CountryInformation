package com.example.listofcountries

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

data class CountryDetails(
    val name: String,
    val currency: String,
    val currencySymbol: String,
    val borders: List<String>,
    val flagUrl: String,
    val languages: String
)

// A composable function that displays information about the current country based on the user's location.
@SuppressLint("MissingPermission")
@Composable
fun CurrentCountryScreen(navController: NavController) {
    // Obtain the current context and the FusedLocationProviderClient for accessing location services.
    val context = LocalContext.current
    val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    // Remember a mutable state to store the details of the current country. It starts as null indicating loading or no data.
    var countryDetails by remember { mutableStateOf<CountryDetails?>(null) }

    // Remember a coroutine scope that is tied to the Composable's lifecycle.
    val coroutineScope = rememberCoroutineScope()

    // Automatically fetch the country information when the screen is launched.
    LaunchedEffect(key1 = true) {
        fetchCountryInfo(context, fusedLocationProviderClient, coroutineScope) { details ->
            // Update the state with the fetched country details.
            countryDetails = details
        }
    }

    // Layout the screen's UI elements.
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // Arrange content vertically.
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Display the country details if available; otherwise, show a loading indicator.
            countryDetails?.let {
                DisplayCountryInfo(it)
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            // Row of buttons for user actions.
            ButtonsRow(navController) {
                // Refresh the country information when the button is clicked.
                fetchCountryInfo(context, fusedLocationProviderClient, coroutineScope) { details ->
                    countryDetails = details
                }
            }
        }
    }
}

// A helper composable that displays detailed information about a country.
@Composable
fun DisplayCountryInfo(countryDetails: CountryDetails) {
    // Check if the details are incomplete and show a loading indicator.
    if (countryDetails.borders == null ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    // Display the country's name.
    Text(
        "${countryDetails.name}",
        style = MaterialTheme.typography.headlineLarge.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    )
    Spacer(modifier = Modifier.height(8.dp))

    // Display the languages spoken in the country.
    Text(
        "Languages: ${countryDetails.languages}",
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary
        )
    )

    // Display the country's currency information.
    Text(
        "Currency: ${countryDetails.currency} (${countryDetails.currencySymbol})",
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary
        )
    )

    // Display bordering countries if available; otherwise, indicate there are none.
    if (countryDetails.borders.isNotEmpty()) {
        Text(
            "Bordering countries: ${countryDetails.borders.joinToString()}",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary
            )
        )
    } else {
        Text(
            "Borders: None",
            style = MaterialTheme.typography.bodySmall.copy(
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.secondary
            )
        )
    }
    Spacer(modifier = Modifier.height(16.dp))

    // Display the country's flag.
    Image(
        painter = rememberImagePainter(countryDetails.flagUrl),
        contentDescription = "Flag of ${countryDetails.name}",
        modifier = Modifier
            .height(200.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(2.dp, Color.Gray, RoundedCornerShape(10.dp))
    )
}


// A composable function that displays a row of buttons for user actions.
@Composable
fun ButtonsRow(navController: NavController, onRefresh: () -> Unit) {
    // Arrange buttons vertically and align them in the center of the width, placing them at the bottom of the container.
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Refresh button to re-fetch the current country's information.
        Button(
            onClick = onRefresh, // Trigger the passed refresh action when clicked.
            modifier = Modifier
                .height(56.dp) // Set the button's height.
                .fillMaxWidth(), // Expand the button to fill the available width.
            shape = RoundedCornerShape(12.dp), // Rounded corners for the button.
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary) // Use the primary color scheme for the button.
        ) {
            Text("Refresh", fontSize = 18.sp, color = MaterialTheme.colorScheme.onPrimary) // Button label with customized font size and color.
        }
        Spacer(modifier = Modifier.height(8.dp)) // Space between the buttons.
        // Button to navigate to the list of countries.
        Button(
            onClick = { navController.navigate("countryList") }, // Navigate to the country list screen when clicked.
            modifier = Modifier
                .height(56.dp) // Set the button's height.
                .fillMaxWidth(), // Expand the button to fill the available width.
            shape = RoundedCornerShape(12.dp), // Rounded corners for the button.
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary) // Use the secondary color scheme for the button.
        ) {
            Text("List of Countries", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSecondary) // Button label with customized font size and color.
        }
    }
}

// Fetches information about the country based on the current location.
fun fetchCountryInfo(context: Context, fusedLocationProviderClient: FusedLocationProviderClient, coroutineScope: CoroutineScope, onResult: (CountryDetails?) -> Unit) {
    // Check for location permissions before proceeding.
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        Log.d("CountryFetch", "Permissions not granted")
        return // Exit the function if permissions are not granted.
    }

    // Define a callback to receive location updates.
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation // Obtain the most recent location.
            if (location != null) {
                // Log the fetched location and proceed to fetch country details.
                Log.d("CountryFetch", "Location fetched: Lat=${location.latitude}, Long=${location.longitude}")
                fetchDetailsAndRespond(location, coroutineScope, onResult) // Fetch country details based on the location.
            } else {
                Log.d("CountryFetch", "Location is null, unable to fetch country details")
            }
        }
    }

    // Attempt to get the last known location.
    fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            // If a last known location is available, use it to fetch country details.
            Log.d("CountryFetch", "Location fetched: Lat=${location.latitude}, Long=${location.longitude}")
            fetchDetailsAndRespond(location, coroutineScope, onResult)
        } else {
            // If no last known location is available, request location updates.
            Log.d("CountryFetch", "Last known location is null, requesting updates")
            val locationRequest = LocationRequest.create().apply {
                interval = 10000 // Set the interval for location updates.
                fastestInterval = 5000 // Set the fastest interval for location updates.
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY // Request high accuracy location data.
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Request location updates with the defined request and callback.
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            }
        }
    }
}

// Helper function to fetch country details based on the location and respond via the provided callback.
private fun fetchDetailsAndRespond(location: Location, coroutineScope: CoroutineScope, onResult: (CountryDetails?) -> Unit) {
    coroutineScope.launch(Dispatchers.IO) {
        try {
            // Obtain the country name based on the geographical coordinates.
            val countryName = getCountryNameFromLocation(location.latitude, location.longitude)
            Log.d("CountryFetch", "Country name fetched: $countryName")
            // Fetch additional details about the country and pass them to the callback.
            val details = fetchCountryDetails(countryName)
            withContext(Dispatchers.Main) {
                onResult(details)
            }
        } catch (e: Exception) {
            // Log any errors encountered during the fetch operation.
            Log.e("CountryFetch", "Error fetching country information", e)
            withContext(Dispatchers.Main) {
                onResult(null) // Respond with null in case of errors.
            }
        }
    }
}




// Function to get the name of a country based on latitude and longitude using the BigDataCloud API.
suspend fun getCountryNameFromLocation(latitude: Double, longitude: Double): String {
    // API endpoint with query parameters for latitude, longitude, and language for locality information.
    val baseUrl = "https://api.bigdatacloud.net/data/reverse-geocode-client"
    val locationURL = "$baseUrl?latitude=$latitude&longitude=$longitude&localityLanguage=en"
    return try {
        // Sending a network request to the URL and getting the response as a String.
        val response = URL(locationURL).readText()
        // Parsing the response to a JSONObject to extract data.
        val countryJSON = JSONObject(response)
        // Extracting the country name from the JSON object.
        val countryName = countryJSON.getString("countryName")
        Log.d("CountryFetch", "Country name fetched: $countryName")
        countryName
    } catch (e: Exception) {
        // Logging and handling exceptions if the network request fails.
        e.printStackTrace()
        Log.d("CountryFetch", "Failed to get country name")
        "Failed to get country name"
    }
}

// Function to fetch details of a country by its name using the RestCountries API.
suspend fun fetchCountryDetails(countryName: String): CountryDetails? {
    // API endpoint for fetching country details by country name.
    val baseUrl = "https://restcountries.com/v3.1/name/$countryName"
    return try {
        // Sending a network request to the URL and getting the response as a String.
        val response = URL(baseUrl).readText()
        // Parsing the response to a JSONArray and extracting the first JSONObject.
        val jsonArray = JSONArray(response)
        if (jsonArray.length() > 0) {
            val jsonObject = jsonArray.getJSONObject(0)
            // Extracting various details like name, currency, borders, and flag URL from the JSON object.
            val name = jsonObject.getJSONObject("name").getString("common")
            val currencies = jsonObject.getJSONObject("currencies")
            val currencyKey = currencies.keys().next()
            val currencyObject = currencies.getJSONObject(currencyKey)
            val currency = currencyObject.getString("name")
            val currencySymbol = currencyObject.getString("symbol")
            val bordersArray = jsonObject.optJSONArray("borders") ?: JSONArray()
            val borders = List(bordersArray.length()) { index -> bordersArray.getString(index) }
            val flagUrl = jsonObject.getJSONObject("flags").getString("png")
            // Dynamically extracting languages from the JSON object.
            val languagesJsonObject = jsonObject.getJSONObject("languages")
            val languagesMap = mutableMapOf<String, String>()
            languagesJsonObject.keys().forEach { key ->
                val languageName = languagesJsonObject.getString(key)
                languagesMap[key] = languageName
            }
            val languages = languagesMap.values.joinToString(", ")
            // Logging the fetched details and returning a CountryDetails object with extracted data.
            Log.d("CountryDetailsFetch", "Details fetched for: $name")
            CountryDetails(name, currency, currencySymbol, borders, flagUrl, languages)
        } else {
            // Handling cases where no country details are found.
            Log.d("CountryDetailsFetch", "No details found for country: $countryName")
            null
        }
    } catch (e: Exception) {
        // Logging and handling exceptions if the network request fails.
        e.printStackTrace()
        Log.d("CountryDetailsFetch", "Failed to fetch country details")
        null
    }
}

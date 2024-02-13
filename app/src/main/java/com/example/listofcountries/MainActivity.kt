package com.example.listofcountries

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.listofcountries.ui.theme.ListOfCountriesTheme

// MainActivity inherits from ComponentActivity to utilize Android Jetpack Compose UI components.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Define a permission request launcher to handle user response to the location permission request.
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // If location permission is granted, set the app's content with Compose UI elements.
                setContent {
                    ListOfCountriesTheme {
                        AppContent()
                    }
                }
            } else {
                // If location permission is denied, display a Toast message to inform the user.
                runOnUiThread {
                    Toast.makeText(this, "Location permission is needed for the app to function correctly.", Toast.LENGTH_LONG).show()
                }
                // Additional handling for permission denial can be done here, such as closing the app.
            }
        }

        // Initiates the permission request for ACCESS_FINE_LOCATION at runtime.
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}

// Composable function to manage the app's content based on the internet connection availability.
@Composable
fun AppContent() {
    val context = LocalContext.current
    // Check for internet connectivity; if not available, show no internet connection UI.
    if (!checkForInternet(context)) {
        NoInternetConnectionUI()
    } else {
        // If internet is available, proceed with app navigation setup.
        AppNavigation()
    }
}

// Composable function to display a UI message when there is no internet connection.
@Composable
fun NoInternetConnectionUI() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No internet connection. Check your connection.")
    }
}

// Sets up navigation for the app using Compose Navigation Component.
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "currentCountry") {
        // Define navigation routes for different screens within the app.
        composable("currentCountry") {
            CurrentCountryScreen(navController)
        }
        composable("countryList") {
            CountryListScreen(navController)
        }
        composable("countryInfo/{countryName}") { backStackEntry ->
            // Retrieve and pass the country name to the CountryInfoScreen.
            val countryName = backStackEntry.arguments?.getString("countryName") ?: ""
            CountryInfoScreen(countryName)
        }
    }
}

// Utility function to check for an active internet connection.
fun checkForInternet(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork) ?: return false
    // Returns true if there's an active internet connection through WIFI, CELLULAR, or ETHERNET.
    return when {
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        else -> false
    }
}
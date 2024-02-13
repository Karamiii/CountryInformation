# Country Information App



## Overview
The Country Information App is a simple Android application developed using Jetpack Compose. It allows users to view detailed information about countries, including common and official names, region, subregion, languages, currencies, timezones, and even a view of the country on Google Maps.

## Features
- **Current Country Information**: Automatically fetches and displays information about the user's current country based on their location.
- **Country List**: Displays a list of all countries. Users can tap on a country to view more detailed information.
- **Search Functionality**: Includes a search bar to filter countries by their common or official names.
- **Google Maps Integration**: Offers a link to view the selected country on Google Maps.

## Technical Details
- **Architecture**: Utilizes the MVVM (Model-View-ViewModel) architecture for a clean separation of concerns.
- **Location Services**: Uses the FusedLocationProviderClient for acquiring the user's current location.
- **Permissions**: Handles runtime permissions for accessing the device's location.
- **External APIs**: Fetches country data from the Rest Countries API and uses the BigDataCloud API for reverse geocoding based on location.

## Dependencies
- Jetpack Compose for UI development.
- Coil for image loading.
- Google Play Services Location for accessing location features.

## Setup
To run this project, clone the repo and open it in Android Studio. Ensure you have the latest Android SDK installed and configured. Before running, add your Google Maps API key to the project to enable map functionalities.

## Created by

- Karam Chafqane

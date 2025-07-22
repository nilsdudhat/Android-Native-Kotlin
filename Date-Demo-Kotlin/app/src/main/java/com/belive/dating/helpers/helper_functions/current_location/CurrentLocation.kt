package com.belive.dating.helpers.helper_functions.current_location

import android.Manifest
import android.annotation.SuppressLint
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.belive.dating.constants.PrefConst
import com.belive.dating.extensions.gsonString
import com.belive.dating.extensions.logger
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import java.util.Locale

const val LOCATION_PERMISSION_REQUEST_CODE = 1001

class CurrentLocation(
    private val activity: AppCompatActivity,
    private val callback: () -> Unit,
) {

    companion object {
        var currentLocation: Location? = null
    }

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
        .setWaitForAccurateLocation(true).setMinUpdateIntervalMillis(5000).build()

    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            logger("--location--", "onLocationResult: ${gsonString(locationResult)}")

            if (locationResult.locations.isNotEmpty()) {
                currentLocation = locationResult.locations[0]
            }
            callback.invoke()

            fusedLocationClient.removeLocationUpdates(this)
        }

        override fun onLocationAvailability(locationAvailability: LocationAvailability) {
            super.onLocationAvailability(locationAvailability)

            logger("--location--", "onLocationAvailability: ${gsonString(locationAvailability)}")
        }
    }

    fun init(enableGPSLauncher: ActivityResultLauncher<IntentSenderRequest>) {
        if (hasLocationPermissions()) {
            // Permissions already granted, enable location services
            enableLocationServices(enableGPSLauncher)
        } else {
            // Request location permissions
            askLocationPermission()
        }
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener {
            if (it != null) {
                Toast.makeText(activity, "Last location available", Toast.LENGTH_SHORT).show()
                callback.invoke()
            } else {
                Toast.makeText(activity, "Last location not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun askLocationPermission() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
            LOCATION_PERMISSION_REQUEST_CODE,
        )
    }

    private fun hasLocationPermissions() = (ActivityCompat.checkSelfPermission(
        activity, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(
        activity, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED)

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (!hasLocationPermissions()) {
            // Permissions are not granted
            Toast.makeText(activity, "Location permission not available", Toast.LENGTH_SHORT).show()
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper()
        ).addOnFailureListener {
            logger("--location--", "addOnFailureListener: ${gsonString(it)}")
        }.addOnSuccessListener {
            logger("--location--", "addOnSuccessListener: ${gsonString(it)}")
        }
    }

    fun isGPSEnabled() {
        // Ensure that the device's location settings are enabled
        val locationSettingsRequest =
            LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
                .setAlwaysShow(true) // Always show the dialog when GPS is off
                .build()
        val client: SettingsClient = LocationServices.getSettingsClient(activity)
        val task: Task<LocationSettingsResponse> =
            client.checkLocationSettings(locationSettingsRequest)

        task.addOnSuccessListener {
            // All location settings are satisfied. The client can initialize location requests here.
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {

            }
        }
    }

    private fun enableLocationServices(enableGPSLauncher: ActivityResultLauncher<IntentSenderRequest>) {
        // Ensure that the device's location settings are enabled
        val locationSettingsRequest =
            LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
                .setAlwaysShow(true) // Always show the dialog when GPS is off
                .build()
        val client: SettingsClient = LocationServices.getSettingsClient(activity)
        val task: Task<LocationSettingsResponse> =
            client.checkLocationSettings(locationSettingsRequest)

        task.addOnSuccessListener {
            // All location settings are satisfied. The client can initialize location requests here.
            startLocationUpdates()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed by showing the user a dialog.
                try {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(exception.resolution).build()
                    enableGPSLauncher.launch(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error
                }
            }
        }
    }

    fun getCurrentLocationData(onLocationDataRetrieved: (locationMap: MutableMap<String, String>?) -> Unit) {

        if (!hasLocationPermissions()) {
            onLocationDataRetrieved.invoke(null)
            return
        }

        if (currentLocation == null) {
            onLocationDataRetrieved.invoke(null)
            return
        } else {
            val locationMap = mutableMapOf<String, String>()

            val currentLatitude = currentLocation!!.latitude
            val currentLongitude = currentLocation!!.longitude

            getGeoCoderAddress(
                currentLatitude,
                currentLongitude,
                onAddressListReceived = { address ->
                    locationMap[PrefConst.CURRENT_LATITUDE] = currentLatitude.toString()
                    locationMap[PrefConst.CURRENT_LONGITUDE] = currentLongitude.toString()
                    locationMap[PrefConst.CURRENT_CITY] = address?.locality ?: ""
                    locationMap[PrefConst.CURRENT_STATE] = address?.adminArea ?: ""
                    locationMap[PrefConst.CURRENT_COUNTRY] = address?.countryName ?: ""
                    locationMap[PrefConst.COUNTRY_CODE] = address?.countryCode ?: ""

                    onLocationDataRetrieved.invoke(locationMap)
                },
            )
        }
    }

    private fun getGeoCoderAddress(
        currentLatitude: Double,
        currentLongitude: Double,
        onAddressListReceived: (address: Address?) -> Unit,
    ) {
        getAddress(activity, currentLatitude, currentLongitude, onAddressListReceived)
    }
}

fun getAddress(
    activity: AppCompatActivity,
    currentLatitude: Double,
    currentLongitude: Double,
    onAddressListReceived: (address: Address?) -> Unit,
) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Geocoder(activity, Locale.getDefault()).getFromLocation(
                currentLatitude,
                currentLongitude,
                1,
            ) {
                if (it.isEmpty()) {
                    onAddressListReceived.invoke(null)
                } else {
                    onAddressListReceived.invoke(it[0])
                }
            }
        } else {
            val addressList: List<Address>? =
                Geocoder(activity, Locale.getDefault()).getFromLocation(
                    currentLatitude,
                    currentLongitude,
                    1,
                )
            if (addressList?.isEmpty() == true) {
                onAddressListReceived.invoke(null)
            } else {
                onAddressListReceived.invoke(addressList?.get(0))
            }
        }
    } catch (e: Exception) {
        onAddressListReceived.invoke(null)
    }
}
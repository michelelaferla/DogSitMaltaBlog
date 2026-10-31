package com.example.dogsitmalta;

import android.Manifest;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Picks a location for a blog post. For NEW posts, after foreground location permission
 * is granted, initially selects the device's current position. Existing post coordinates
 * are preserved on edit until the user explicitly changes them.
 *
 * The user can always tap the map instead, including when permission is denied or
 * location services are unavailable. No background location permission is requested.
 */
public class LocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_LAT = "selected_latitude";
    public static final String EXTRA_LNG = "selected_longitude";
    public static final String EXTRA_LOCALITY = "selected_locality";

    private static final String STATE_HAS_POINT = "state_has_point";
    private static final String STATE_LAT = "state_lat";
    private static final String STATE_LNG = "state_lng";
    private static final String STATE_LOCALITY = "state_locality";
    private static final long GEOCODE_TIMEOUT_MS = 12_000L;
    private static final LatLng MALTA_FALLBACK = new LatLng(35.90, 14.45);

    private Double latitude;
    private Double longitude;
    private String selectedLocality;
    private boolean resolvingLocality;
    private int localityRevision = 0;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService geocoderWorker = Executors.newSingleThreadExecutor();
    private Runnable lookupTimeout;
    private GoogleMap googleMap;
    private Marker marker;
    private TextView currentSelection;
    private Button confirmButton;
    private FusedLocationProviderClient locationClient;
    private CancellationTokenSource pendingLocationRequest;
    private int selectionRevision = 0;
    private boolean hasInitialPoint = false;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    permissions -> {
                        if (hasLocationPermission()) {
                            enableMyLocationLayer();
                            locateAndSelectCurrentPosition();
                        } else {
                            Toast.makeText(this, R.string.map_permission_denied,
                                    Toast.LENGTH_LONG).show();
                            showSelectedCoordinates();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);

        locationClient = LocationServices.getFusedLocationProviderClient(this);
        currentSelection = findViewById(R.id.pickedCoordinates);
        confirmButton = findViewById(R.id.buttonConfirmLocation);

        if (savedInstanceState != null &&
                savedInstanceState.getBoolean(STATE_HAS_POINT, false)) {
            latitude = savedInstanceState.getDouble(STATE_LAT);
            longitude = savedInstanceState.getDouble(STATE_LNG);
            selectedLocality = savedInstanceState.getString(STATE_LOCALITY);
            hasInitialPoint = true;
        } else if (getIntent().hasExtra(EXTRA_LAT) && getIntent().hasExtra(EXTRA_LNG)) {
            // Editing a post: keep its original point, rather than replacing it with GPS.
            latitude = getIntent().getDoubleExtra(EXTRA_LAT, 0);
            longitude = getIntent().getDoubleExtra(EXTRA_LNG, 0);
            selectedLocality = getIntent().getStringExtra(EXTRA_LOCALITY);
            hasInitialPoint = true;
        }

        confirmButton.setEnabled(latitude != null && longitude != null);
        showSelectedCoordinates();

        confirmButton.setOnClickListener(view -> {
            if (latitude == null || longitude == null) return;
            Intent result = new Intent();
            result.putExtra(EXTRA_LAT, latitude);
            result.putExtra(EXTRA_LNG, longitude);
            result.putExtra(EXTRA_LOCALITY, selectedLocality == null ? "" : selectedLocality);
            setResult(RESULT_OK, result);
            finish();
        });

        // The user can retry location detection even after initially denying permission.
        findViewById(R.id.buttonUseMyLocation).setOnClickListener(
                view -> requestAndUseMyLocation());

        SupportMapFragment fragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapPicker);
        if (fragment != null) fragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        LatLng start = latitude == null ? MALTA_FALLBACK : new LatLng(latitude, longitude);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                start, latitude == null ? 11f : 15f));

        if (latitude != null && longitude != null) {
            marker = googleMap.addMarker(new MarkerOptions()
                    .position(start).title(selectedLocality == null
                            ? getString(R.string.map_selected_point) : selectedLocality));
        }

        googleMap.setOnMapClickListener(point -> {
            // A user's manual choice always wins over any GPS request still running.
            selectionRevision++;
            cancelPendingLocation();
            selectPoint(point, false);
        });

        // Make the standard blue-dot location button also SELECT the device location,
        // not merely pan the camera without updating the stored post coordinates.
        googleMap.setOnMyLocationButtonClickListener(() -> {
            requestAndUseMyLocation();
            return true;
        });

        if (hasLocationPermission()) enableMyLocationLayer();

        // Saved points retain their stored locality until the user moves the marker.
        // If an older post has coordinates but no locality, attempt to fill the name.
        if (!hasInitialPoint) requestAndUseMyLocation();
        else if ((selectedLocality == null || selectedLocality.trim().isEmpty())
                && latitude != null && longitude != null) {
            resolveLocality(new LatLng(latitude, longitude));
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAndUseMyLocation() {
        if (!hasLocationPermission()) {
            // Request FINE and COARSE together so Android 12+ can offer approximate access.
            permissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
            return;
        }
        enableMyLocationLayer();
        locateAndSelectCurrentPosition();
    }

    private void enableMyLocationLayer() {
        if (googleMap == null || !hasLocationPermission()) return;
        try {
            googleMap.setMyLocationEnabled(true); // Blue dot and standard location button.
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        } catch (SecurityException ignored) {
            // Permission might have been revoked between checking and using it.
        }
    }

    private void locateAndSelectCurrentPosition() {
        if (!hasLocationPermission()) return;

        cancelPendingLocation();
        final int requestedAtRevision = selectionRevision;
        final CancellationTokenSource request = new CancellationTokenSource();
        pendingLocationRequest = request;

        if (latitude == null) currentSelection.setText(R.string.map_finding_location);

        boolean precise = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        CurrentLocationRequest options = new CurrentLocationRequest.Builder()
                .setPriority(precise ? Priority.PRIORITY_HIGH_ACCURACY
                        : Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                .setMaxUpdateAgeMillis(10_000L)
                .setDurationMillis(15_000L)
                .build();

        try {
            locationClient.getCurrentLocation(options, request.getToken())
                    .addOnSuccessListener(this, location -> {
                        if (pendingLocationRequest != request
                                || selectionRevision != requestedAtRevision) return;
                        pendingLocationRequest = null;
                        if (location == null) {
                            showLocationUnavailable();
                            return;
                        }
                        selectPoint(new LatLng(location.getLatitude(),
                                location.getLongitude()), true);
                    })
                    .addOnFailureListener(this, exception -> {
                        if (pendingLocationRequest != request
                                || selectionRevision != requestedAtRevision) return;
                        pendingLocationRequest = null;
                        showLocationUnavailable();
                    });
        } catch (SecurityException exception) {
            if (pendingLocationRequest == request) pendingLocationRequest = null;
            showLocationUnavailable();
        }
    }

    private void selectPoint(@NonNull LatLng point, boolean moveCamera) {
        latitude = point.latitude;
        longitude = point.longitude;
        selectedLocality = null;  // Never keep an old town name with new coordinates.
        if (googleMap != null) {
            if (marker != null) marker.remove();
            marker = googleMap.addMarker(new MarkerOptions().position(point)
                    .title(getString(R.string.map_selected_point)));
            if (moveCamera) googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(point, 16f));
        }
        resolveLocality(point);
    }

    /**
     * Reverse geocode the selected coordinates into a locality/town name.
     * On API 33+, use Android's asynchronous listener; on API 24-32, use a
     * worker thread so no network-backed geocoding blocks the UI.
     * Callbacks are guarded by a revision number to reject stale map taps.
     */
    private void resolveLocality(@NonNull LatLng point) {
        final int revision = ++localityRevision;
        resolvingLocality = true;
        selectedLocality = null;
        confirmButton.setEnabled(false);
        showSelectedCoordinates();

        if (lookupTimeout != null) mainHandler.removeCallbacks(lookupTimeout);
        lookupTimeout = () -> finishLocalityLookup(revision, null);
        mainHandler.postDelayed(lookupTimeout, GEOCODE_TIMEOUT_MS);

        if (!Geocoder.isPresent()) {
            finishLocalityLookup(revision, null);
            return;
        }

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                geocoder.getFromLocation(point.latitude, point.longitude, 3,
                        new Geocoder.GeocodeListener() {
                            @Override
                            public void onGeocode(@NonNull List<Address> addresses) {
                                String name = extractLocality(addresses);
                                runOnUiThread(() -> finishLocalityLookup(revision, name));
                            }

                            @Override
                            public void onError(@Nullable String errorMessage) {
                                runOnUiThread(() -> finishLocalityLookup(revision, null));
                            }
                        });
            } catch (RuntimeException ex) {
                finishLocalityLookup(revision, null);
            }
        } else {
            geocoderWorker.execute(() -> {
                List<Address> addresses = null;
                try {
                    // Legacy call is deliberately off the main/UI thread.
                    addresses = geocoder.getFromLocation(point.latitude, point.longitude, 3);
                } catch (IOException | RuntimeException ignored) {
                    // Service can be offline, rate-limited or unavailable.
                }
                String name = extractLocality(addresses);
                runOnUiThread(() -> finishLocalityLookup(revision, name));
            });
        }
    }

    /** Use city/locality data, not a street address or business name. */
    @Nullable
    private static String extractLocality(@Nullable List<Address> addresses) {
        if (addresses == null || addresses.isEmpty()) return null;
        for (Address address : addresses) {
            String locality = firstNonBlank(address.getLocality(), address.getSubLocality());
            if (locality != null) return locality;
        }
        for (Address address : addresses) {
            String area = firstNonBlank(address.getSubAdminArea(), address.getAdminArea());
            if (area != null) return area;
        }
        return null;
    }

    @Nullable
    private static String firstNonBlank(String... candidates) {
        for (String value : candidates) {
            if (value != null && !value.trim().isEmpty()) return value.trim();
        }
        return null;
    }

    private void finishLocalityLookup(int revision, @Nullable String locality) {
        if (revision != localityRevision || isFinishing() || isDestroyed()) return;
        if (lookupTimeout != null) mainHandler.removeCallbacks(lookupTimeout);
        lookupTimeout = null;
        resolvingLocality = false;
        selectedLocality = locality;
        confirmButton.setEnabled(latitude != null && longitude != null);
        if (marker != null && locality != null) marker.setTitle(locality);
        showSelectedCoordinates();
    }

    private void showLocationUnavailable() {
        Toast.makeText(this, R.string.map_location_unavailable, Toast.LENGTH_LONG).show();
        showSelectedCoordinates();
    }

    private void showSelectedCoordinates() {
        if (currentSelection == null) return;
        if (latitude == null || longitude == null) {
            currentSelection.setText(R.string.map_picker_none);
        } else if (resolvingLocality) {
            currentSelection.setText(getString(R.string.map_resolving_locality,
                    latitude, longitude));
        } else if (selectedLocality != null && !selectedLocality.isEmpty()) {
            currentSelection.setText(getString(R.string.map_locality_and_coordinates,
                    selectedLocality, latitude, longitude));
        } else {
            currentSelection.setText(getString(R.string.map_locality_not_found,
                    latitude, longitude));
        }
    }

    private void cancelPendingLocation() {
        if (pendingLocationRequest != null) {
            pendingLocationRequest.cancel();
            pendingLocationRequest = null;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(STATE_HAS_POINT, latitude != null && longitude != null);
        if (latitude != null && longitude != null) {
            outState.putDouble(STATE_LAT, latitude);
            outState.putDouble(STATE_LNG, longitude);
            outState.putString(STATE_LOCALITY, selectedLocality);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        cancelPendingLocation();
        ++localityRevision; // Invalidate any pending geocoder callbacks.
        if (lookupTimeout != null) mainHandler.removeCallbacks(lookupTimeout);
        geocoderWorker.shutdownNow();
        super.onDestroy();
    }
}

package edu.temple.convoy;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

public class ConvoyActivity extends FragmentActivity implements OnMapReadyCallback {

    LocationManager locationManager;
    LocationListener locationListener;
    Location prevLocation;
    TextView textView;
    RequestQueue requestQueue;
    FusedLocationProviderClient fusedLocationClient;
    Location lastKnown;

    GoogleMap mapAPI;
    SupportMapFragment mapFragment;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convoy);

        requestQueue = Volley.newRequestQueue(this);
        textView = findViewById(R.id.txtId);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SharedPreferences settings = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
        String check = settings.getString("convoyID", null);
        if (check != null) {
            textView.setText("Convoy ID:" + check);

        }
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapAPI);
        mapFragment.getMapAsync(this);

        locationManager = getSystemService(LocationManager.class);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (prevLocation != null) {
                    if (ActivityCompat.checkSelfPermission(ConvoyActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ConvoyActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    lastKnown = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));
                        double lat = lastKnown.getLatitude();
                        double longi = lastKnown.getLongitude();
                        LatLng latLng = new LatLng(lat,longi);
                        mapAPI.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14.f));


                }
                prevLocation = location;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (!haveGPSPermission()) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        } else {
            doGPSStuff();
        }



    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean haveGPSPermission() {

        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint({"MissingPermission", "NewApi"})
    private void doGPSStuff() {
        if (haveGPSPermission())
            //location updates only for 10 meters-
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, locationListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        doGPSStuff();
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            doGPSStuff();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mapAPI = googleMap;
        mapAPI.setMyLocationEnabled(true);

        lastKnown = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));
        if(lastKnown != null){
            double lat = lastKnown.getLatitude();
            double longi = lastKnown.getLongitude();
            LatLng latLng = new LatLng(lat,longi);
            mapAPI.addMarker(new MarkerOptions().position(latLng).title("mm"));
            mapAPI.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14.f));

        }

        //LatLng mm = new LatLng(19.389137, 76.031094);
       // mapAPI.moveCamera(CameraUpdateFactory.newLatLng(mm));
       // mapAPI.moveCamera(CameraUpdateFactory.newLatLngZoom(mm, 16));
    }

    public void startConvoy(View view) {
        if(!textView.getText().toString().contains("Convoy")) {
            Intent launchIntent = new Intent(ConvoyActivity.this, StartConvoyActivity.class);
            startActivity(launchIntent);
        }
    }

    public void endConvoy(View view) {
        if(textView.getText().toString().contains("Convoy")) {
            Intent launchIntent = new Intent(ConvoyActivity.this, EndConvoyActivity.class);
            startActivity(launchIntent);
        }
    }

    public void leaveConvoy(){
        String convoy = "https://kamorris.com/lab/convoy/convoy.php";
        StringRequest strRequest = new StringRequest(Request.Method.POST, convoy,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        if(response.contains("SUCCESS")){

                            SharedPreferences settings = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString("convoyID",null);
                            editor.apply();
                            textView.setText(" ");
                            Toast.makeText(ConvoyActivity.this, "You Hava Left This Convoy", Toast.LENGTH_SHORT).show();

                        }else{
                            Toast.makeText(ConvoyActivity.this, "You Have Not Joined A Convoy", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Toast.makeText(ConvoyActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {

                SharedPreferences settings = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
                String username = settings.getString("username", "N/A");
                String session = settings.getString("session", "N/A");
                String convoyIdentity = settings.getString("convoyID", "N/A");

                Map<String, String> params = new HashMap<String, String>();
                params.put("action", "END");
                params.put("username", username);
                params.put("session_key",session);
                params.put("convoy_id",convoyIdentity);
                return params;
            }
        };

        requestQueue.add(strRequest);
    }

    public void logoutClick(View view) {

        leaveConvoy();
        String convoy = "https://kamorris.com/lab/convoy/account.php";
        StringRequest strRequest = new StringRequest(Request.Method.POST, convoy,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        if(response.contains("SUCCESS")){


                            SharedPreferences settings = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.clear();
                            editor.apply();

                            Intent launchIntent = new Intent(ConvoyActivity.this, MainActivity.class);
                            startActivity(launchIntent);
                            finito();

                        }else{
                            Toast.makeText(ConvoyActivity.this, "Logout Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Toast.makeText(ConvoyActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {

                SharedPreferences settings = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
                String username = settings.getString("username", "N/A");
                String session = settings.getString("session", "N/A");

                Map<String, String> params = new HashMap<String, String>();
                params.put("action", "LOGOUT");
                params.put("username", username);
                params.put("session_key",session);
                return params;
            }
        };

        requestQueue.add(strRequest);
    }

    public void joinClick(View view) {

        Intent launchIntent = new Intent(ConvoyActivity.this, JoinConvoyActivity.class);
        startActivity(launchIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences settings = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
        String check = settings.getString("convoyID", "N/A");
        if(!check.equals("N/A")){
            textView.setText("Convoy ID:" + check);
        }

        String leave = settings.getString("end", null);

        if(leave!=null){
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("end",null);
            editor.apply();
           leaveConvoy();
        }
    }


    public void finito(){
        finish();
    }


}
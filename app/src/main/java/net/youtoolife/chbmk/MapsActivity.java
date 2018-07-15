package net.youtoolife.chbmk;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private int loginID = 0;
    private double lat = 0;
    private double longt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        loginID = getIntent().getIntExtra("loginID", 0);
        lat = getIntent().getDoubleExtra("lat", 0);
        longt = getIntent().getDoubleExtra("long", 0);

            Log.d("MAP", "login: "+loginID+"; lat: "+lat+"; long: "+longt);



        Button mapBtn = (Button) findViewById(R.id.gpsBtn);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String login = SharedPrefManager.getInstance(getApplicationContext()).getLogin();
                String invite = SharedPrefManager.getInstance(getApplicationContext()).getInvite();

                if (login == null || invite == null)
                    return;

                Map<String, String> params0 = new HashMap<>();
                params0.put("login", login);
                params0.put("invite", RSAIsa.rsaEncrypt(invite));
                params0.put("pwd",RSAIsa.rsaEncrypt(XA.b(XA.B)));

                JSONObject jsonObject = new JSONObject(params0);
                Map<String, String> params = new HashMap<>();
                String json = jsonObject.toString();
                System.out.print("json "+json);
                //params.put("d", RSAIsa.rsaEncrypt(jsonObject.toString()));
                try {
                    params.put("d", Base64.encodeToString(jsonObject.toString().getBytes("UTF-8"), Base64.DEFAULT));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                String URL_SERVER = "http://chbmk.000webhostapp.com/chbmk/get_location.php";//XA.b(XA.A);


                RequestHandler requestHandler = new RequestHandler(URL_SERVER, params, getApplicationContext());
                requestHandler.request(new CallBack() {
                    @Override
                    public void callBackFunc(String response) {
                        responseGps(response);
                    }
                });
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    private void responseGps(String response) {

        if (response == null || response.isEmpty())
            return;

        try {
            JSONObject obj = new JSONObject(new String(Base64.decode(response, Base64.DEFAULT), "UTF-8"));

            if (obj.getInt("id") != 1)
                return;

            JSONArray arr = obj.getJSONArray("msg");

            lat = arr.getJSONObject(loginID).getDouble("lat");
            longt = arr.getJSONObject(loginID).getDouble("long");

            if (mMap != null) {
                LatLng sydney = new LatLng(lat, longt);
                mMap.addMarker(new MarkerOptions().position(sydney).title("Последнее местоположение ребенка"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(lat, longt);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.addMarker(new MarkerOptions().position(sydney).title("Последнее местоположение ребенка"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16));
    }
}

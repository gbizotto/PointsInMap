package br.gbizotto.saitama;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import br.gbizotto.saitama.data.SaitamaContract;
import br.gbizotto.saitama.integration.JitenshaApi;
import br.gbizotto.saitama.integration.JitenshaParameters;
import br.gbizotto.saitama.pojo.Place;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String LOG_TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private List<Place> mPlaces;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mContext = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarSelection);
        toolbar.setTitle(R.string.bicycle_rental_places);

        SharedPreferences sharedPreferences = getApplication().getSharedPreferences(SaitamaContract.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String accessToken = sharedPreferences.getString(SaitamaContract.SHARED_ACCESS_TOKEN,null);

        SearchPlacesTask searchPlacesTask = new SearchPlacesTask(accessToken);
        searchPlacesTask.execute((Void) null);
    }

    public class SearchPlacesTask extends AsyncTask<Void, Void, Boolean>{

        String mAccessToken;
        List<Place> mPlaces = new ArrayList<>();

        public SearchPlacesTask(String accessToken){
            mAccessToken = accessToken;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            StringBuffer uri = new StringBuffer()
                    .append(mContext.getString(R.string.jitensha_api_base_url))
                    .append(mContext.getString(R.string.jitensha_api_places));

            String result = JitenshaApi.connectByGet(Uri.parse(uri.toString()).buildUpon().build(),mAccessToken);

            JSONObject resultsJson = null;
            try {
                resultsJson = new JSONObject(result);
                JSONArray placesArray = resultsJson.getJSONArray(JitenshaParameters.RESPONSE_RESULTS);

                for (int i = 0; i < placesArray.length(); i++) {
                    JSONObject placeObject =  placesArray.getJSONObject(i);
                    JSONObject locationObject = placeObject.getJSONObject(JitenshaParameters.RESPONSE_LOCATION);
                    Double latitude = locationObject.getDouble(JitenshaParameters.RESPONSE_LATITUDE);
                    Double longitude = locationObject.getDouble(JitenshaParameters.RESPONSE_LONGITUDE);
                    String id = placeObject.getString(JitenshaParameters.RESPONSE_ID);
                    String name = placeObject.getString(JitenshaParameters.RESPONSE_NAME);
                    mPlaces.add(new Place(id, latitude,longitude,name));
                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(),e);
            }


            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            showMap(mPlaces);
        }
    }

    public void showMap(List<Place> places){
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mPlaces = places;

    }


    /**
     * Manipulates the map once available.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Intent intent = new Intent();
                intent.setClass(mContext, PaymentActivity.class);
                mContext.startActivity(intent);
                return false;
            }
        });

        //Parameters that will be used to determine the bounds for zooming in;
        Double smallestLatitude = null;
        Double smallestLongitude = null;
        Double biggestLatitude = null;
        Double biggestLongitude = null;

        for (Place place:mPlaces) {
            LatLng latLng = new LatLng(place.getLatitude(), place.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title(place.getName()));

            if (smallestLatitude == null || smallestLatitude > place.getLatitude()){
                smallestLatitude = place.getLatitude();
            }else if(biggestLatitude == null || biggestLatitude < place.getLatitude()){
                biggestLatitude = place.getLatitude();
            }

            if (smallestLongitude == null || smallestLongitude > place.getLongitude()){
                smallestLongitude = place.getLongitude();
            }else if(biggestLongitude == null || biggestLongitude < place.getLongitude()){
                biggestLongitude = place.getLongitude();
            }
        }

        // Shows only map centered inside these coordinates
        LatLngBounds latLngBounds = new LatLngBounds(new LatLng(smallestLatitude, smallestLongitude), new LatLng(biggestLatitude, biggestLongitude));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngBounds.getCenter(), 10));
    }
}

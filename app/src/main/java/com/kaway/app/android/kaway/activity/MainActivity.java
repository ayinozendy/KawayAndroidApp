package com.kaway.app.android.kaway.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.kaway.app.android.kaway.R;
import com.kaway.app.android.kaway.data.MockData;
import com.kaway.app.android.kaway.helper.RouteProcessor;
import com.kaway.app.android.kaway.model.Jeep;
import com.kaway.app.android.kaway.model.Route;
import com.kaway.app.android.kaway.model.RouteStop;
import com.kaway.app.android.kaway.model.User;
import com.kaway.app.android.kaway.service.RestService;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    SupportMapFragment mapFragment;
    GoogleMap map;
    OnMapReadyCallback mapReadyCallback;
    RecyclerView routeList;
    Button pickRouteButton;

    RestService mockData = new MockData();
    List<Route> routes;
    List<RouteStop> routeStops;
    List<Jeep> jeeps;
    User user;

    LatLng initialLocation; //Arbitrary for now
    float initialZoom = 18f;

    boolean routeListIsShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    @Override
    public void onBackPressed() {
        if (routeListIsShowing) {
            routeListIsShowing = false;
            final Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down_exit);
            routeList.setAnimation(slideDown);
            routeList.setVisibility(View.GONE);
            resetMapGestures();
        } else {
            super.onBackPressed();
        }
    }

    private void init() {
        routeList = (RecyclerView) findViewById(R.id.routeList);
        pickRouteButton = (Button) findViewById(R.id.pickRouteButton);
        if (pickRouteButton != null) {
            pickRouteButton.setOnClickListener(v -> {
                routeListIsShowing = true;
                final Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up_enter);
                routeList.setVisibility(View.VISIBLE);
                routeList.setAnimation(slideUp);
                if (map != null) {
                    map.getUiSettings().setAllGesturesEnabled(false);
                }
            });
        }

        mapFragment = SupportMapFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.mapPlaceHolder, mapFragment);
        ft.commit();
        mapReadyCallback = new MapReadyCallback();
        mapFragment.getMapAsync(mapReadyCallback);

        initializeMapItems();
    }

    private void initializeMapItems() {
        routes = mockData.getRoutes();
        routeStops = mockData.getStops();
        jeeps = mockData.getJeeps();
        user = mockData.getUser(0);
        initialLocation = new LatLng(user.getLocation().getLat(), user.getLocation().getLng());
    }

    private void drawLines() {
        for (Route _route : routes) {
            map.addPolyline(RouteProcessor.processRoute(_route));
        }
    }

    private void drawStops() {
        for (RouteStop routeStop : routeStops) {
            map.addMarker(RouteProcessor.processRouteStop(routeStop));
        }
    }

    private void drawJeeps() {
        for (Jeep jeep : jeeps) {
            map.addMarker(RouteProcessor.processJeep(jeep));
        }
    }

    private void drawUser() {
        map.addMarker(RouteProcessor.processUser(user));
    }

    class MapReadyCallback implements OnMapReadyCallback {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;
            map.getUiSettings().setMapToolbarEnabled(false);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, initialZoom));
            resetMapGestures();

            drawLines();
            drawStops();
            drawJeeps();
            drawUser();
        }
    }

    private void resetMapGestures() {
        if (map == null)
            return;
        map.getUiSettings().setAllGesturesEnabled(false);
        map.getUiSettings().setScrollGesturesEnabled(true);
        map.getUiSettings().setZoomGesturesEnabled(true);
        map.getUiSettings().setRotateGesturesEnabled(true);
    }
}

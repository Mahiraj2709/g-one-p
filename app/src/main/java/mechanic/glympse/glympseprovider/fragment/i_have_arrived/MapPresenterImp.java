package mechanic.glympse.glympseprovider.fragment.i_have_arrived;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import mechanic.glympse.glympseprovider.app.BaseActivity;
import mechanic.glympse.glympseprovider.app.MainActivity;
import mechanic.glympse.glympseprovider.data.DataManager;
import mechanic.glympse.glympseprovider.data.local.PrefsHelper;
import mechanic.glympse.glympseprovider.data.model.CustomerLocation;
import mechanic.glympse.glympseprovider.data.model.ResponseData;
import mechanic.glympse.glympseprovider.fragment.HomeFragment;
import mechanic.glympse.glympseprovider.fragment.RateYourCustomerFragment;
import mechanic.glympse.glympseprovider.utils.ApplicationMetadata;
import mechanic.glympse.glympseprovider.utils.DialogFactory;


/**
 * Created by admin on 12/27/2016.
 */

public class MapPresenterImp implements MapPresenter, com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = MapPresenterImp.class.getSimpleName();
    private static final long INTERVAL = 1000 * 60 * 1; //1 minute
    private static final long FASTEST_INTERVAL = 1000 * 60 * 1; // 1 minute
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLocation;
    private String mLastUpdateTime;
    private static final int CALL_PERMISSIONS_REQUEST = 231;
    private MechOnWayMapView view;
    private ResponseData responseData = null;
    private FragmentActivity activity = null;
    private Context context = null;
    private String[] permissions = {Manifest.permission.CALL_PHONE};
    private PrefsHelper prefsHelper = null;
    private DataManager dataManager = null;
    private String requestId = null;
    private Timer timer = new Timer();
    Map<String,String> requestMap = new HashMap<>();
    private LatLng myLatLng = null;
    private LatLng customerLatLng = null;

    public MapPresenterImp(MechOnWayMapView view, FragmentActivity fragmentActivity, Context context) {
        if (view == null) throw new NullPointerException("view can not be NULL");
        if (fragmentActivity == null)
            throw new NullPointerException("AppCompactActivity can not be NULL");
        if (context == null) throw new NullPointerException("context can not be NULL");

        this.view = view;
        activity = fragmentActivity;
        this.context = context;
        prefsHelper = new PrefsHelper(context);
        dataManager = new DataManager(context);
        buildGoogleApiClient();
        this.view.generateMap();
    }




    @Override
    public void onMapReady() {

    }

    @Override
    public void callCustomer() {
        if (((BaseActivity)activity).hasPermission(permissions[0])) {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + responseData.getCustomerDetail().getPhoneNo()));
            activity.startActivity(intent);
        } else {
            ((BaseActivity)activity).requestPermissionsSafely(permissions,CALL_PERMISSIONS_REQUEST);
        }
   }

    @Override
    public void finishTask() {
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put(ApplicationMetadata.SESSION_TOKEN, prefsHelper.getPref(ApplicationMetadata.SESSION_TOKEN, ""));
        requestParams.put(ApplicationMetadata.REQUEST_ID, requestId);

        dataManager.setCallback(new DataManager.RequestCallback() {
            @Override
            public void Data(Object data) {
                Fragment fragment = RateYourCustomerFragment.newInstance(requestId);
                ((MainActivity)activity).addFragmentToStack(fragment,"home_fragment");
            }
        });

        dataManager.finished(requestParams);
    }

    @Override
    public void setRequestId(String requestId) {
//request mechaniic by id
        this.requestId = requestId;
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put(ApplicationMetadata.SESSION_TOKEN, prefsHelper.getPref(ApplicationMetadata.SESSION_TOKEN, ""));
        requestParams.put(ApplicationMetadata.REQUEST_ID, requestId); // CUSTOMER ID
        dataManager.setCallback(new DataManager.RequestCallback() {


            @Override
            public void Data(Object data) {
                responseData = (ResponseData) data;
                view.setCustomerDetail(responseData);

                customerLatLng = new LatLng(Double.parseDouble(responseData.getMechAppointment().getAddressLat()),Double.parseDouble(responseData.getMechAppointment().getAddressLong()));

                if (myLatLng != null && customerLatLng != null) {
                    view.setMap(myLatLng,customerLatLng);
                }
                //upldateLocationEveryFiveMinute(responseData.getId());
            }
        });
        dataManager.getCustomerDetail(requestParams);
    }

    @Override
    public void arrived() {
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put(ApplicationMetadata.SESSION_TOKEN, prefsHelper.getPref(ApplicationMetadata.SESSION_TOKEN, ""));
        requestParams.put(ApplicationMetadata.REQUEST_ID, requestId); // CUSTOMER ID
        dataManager.setmArrivedCallback(new DataManager.ArrivedCallback() {


            @Override
            public void arrived() {
//                /*responseData = (CustomerDetail) data;
                view.iHaveArrived();
            }
        });
        dataManager.arrived(requestParams);
    }

    protected synchronized void buildGoogleApiClient() {
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();
    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates() {

        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: ");
        //got the the current location for the first time
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }

    @Override
    public void connectToGoogleApiClient() {
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onPause() {
        stopLocationUpdates();
    }
    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, location.getLatitude() + " longitude " + location.getLongitude());
        Log.d(TAG, "Firing onLocationChanged..............................................");
        mLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        Log.i(TAG, "last update time for location is" + mLastUpdateTime);

        //move map to the current location
        //moveToLatLng();
        LatLng mechCurrentLoc = new LatLng(mLocation.getLatitude(),mLocation.getLongitude());
        myLatLng = mechCurrentLoc;

        if(customerLatLng != null)
            view.setMap(mechCurrentLoc,customerLatLng);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Connected");
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void upldateLocationEveryFiveMinute(String serviceProviderId) {
        requestMap.put(ApplicationMetadata.SERVICE_PROVIDER_ID,serviceProviderId);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //call update location every 5 minutes
                    dataManager.getLatLng(requestMap);
                dataManager.setmLocationUpdateCallback(new DataManager.LocationUpdateCallback() {
                    @Override
                    public void locationReceived(CustomerLocation location) {

                    }
                });

            }
        },0,5 * 60 * 1000);
    }

    @Override
    public void reqeustCancelled(String message) {
        Fragment fragment = HomeFragment.newInstance(0);
        ((MainActivity)activity).addFragmentToStack(fragment,"home_fragment");

        DialogFactory.createSimpleOkErrorDialog(context,"Your request has been cancelled").show();
    }
}

package mechanic.glympse.glympseprovider.fragment;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.kyleduo.switchbutton.SwitchButton;
import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mechanic.glympse.glympseprovider.GlympseApplication;
import mechanic.glympse.glympseprovider.R;
import mechanic.glympse.glympseprovider.app.MainActivity;
import mechanic.glympse.glympseprovider.data.DataManager;
import mechanic.glympse.glympseprovider.data.local.PrefsHelper;
import mechanic.glympse.glympseprovider.data.model.ResponseData2;
import mechanic.glympse.glympseprovider.fragment.i_have_arrived.IHaveArrivedFragment;
import mechanic.glympse.glympseprovider.interfaces.AvailabilityCallback;
import mechanic.glympse.glympseprovider.model.Customer;
import mechanic.glympse.glympseprovider.utils.ApplicationMetadata;
import mechanic.glympse.glympseprovider.utils.DialogFactory;
import mechanic.glympse.glympseprovider.utils.LocationUtils;

/**
 * Created by admin on 11/22/2016.
 */

public class HomeFragment extends Fragment implements AvailabilityCallback {
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final int REQUEST_CHECK_SETTINGS = 11;
    private static final int REQUEST_PERMISSION_ACCESS_LOCATION = 12;
    private static final String TAG = HomeFragment.class.getSimpleName();
    @BindView(R.id.sb_availability)
    SwitchButton sb_availability;
    @BindView(R.id.fl_availability)
    FrameLayout fl_availability;
    private static View view;
    private boolean setChecked = false;
    private GoogleMap mMap;
    private MapFragment mapFragment = null;
    @BindView(R.id.iv_currentLocation)
    ImageView iv_currentLocation;
    private MainActivity activity;
    private Customer customer;

    public static HomeFragment newInstance(int args) {
        HomeFragment fragment = new HomeFragment();
        Bundle data = new Bundle();
        data.putInt("args", args);
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) (getActivity());
        String notificationData = getActivity().getIntent().getStringExtra(ApplicationMetadata.NOTIFICATION_DATA);
        int notificationType = getActivity().getIntent().getIntExtra(ApplicationMetadata.NOTIFICATION_TYPE,-1);
        if (notificationData != null && notificationType == ApplicationMetadata.NOTIFICATION_NEW_OFFER) { // NEW OFFER FROM CUSTOMER
            customer = new Gson().fromJson(notificationData, Customer.class);
//            customer = getTestCustomer();
            newRequestFromCustomer(customer);
        } else if(notificationData != null && notificationType == ApplicationMetadata.NOTIFICATION_REQ_CANCELLED){// OFFER ACCEPTED BY CUSTOMER
            Customer tempCustomer = new Gson().fromJson(notificationData, Customer.class);
            DialogFactory.createSimpleOkErrorDialog(getContext(),tempCustomer.message);
            Fragment fragment = HomeFragment.newInstance(0);
            ((MainActivity)context).addFragmentToStack(fragment,"home_fragment");
        }else if(notificationData != null && notificationType == ApplicationMetadata.NOTIFICATION_TASK_FINISH){// OFFER ACCEPTED BY CUSTOMER
            Customer tempCustomer = new Gson().fromJson(notificationData, Customer.class);
            //DialogFactory.createSimpleOkErrorDialog(getContext(),tempCustomer.message);
            Fragment fragment = HomeFragment.newInstance(0);
            ((MainActivity)context).addFragmentToStack(fragment,"home_fragment");
        }
        //set availability listener
        ((MainActivity)context).setOnAvailabilityChangeListener(this);
    }

    /*private Customer getTestCustomer() {
        Customer customer = new Customer();
        customer.name = "Mahiraj";
        customer.customer_id = "1";
        customer.location = "Noida";
        customer.request_id = "44";
        return customer;
    }*/

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.content_main, container, false);
        } catch (InflateException e) {
    /* map is already there, just return view as it is */
        }
        ButterKnife.bind(this, view);
        if (GlympseApplication.isAvailable) {
            sb_availability.setChecked(true);
        }
        initGoogleMap();
        fl_availability.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrefsHelper prefsHelper = new PrefsHelper(getContext());
                Map<String, String> requestParams = new HashMap<>();
                requestParams.put(ApplicationMetadata.SESSION_TOKEN, prefsHelper.getPref(ApplicationMetadata.SESSION_TOKEN, ""));

                if (sb_availability.isChecked()) {
                    requestParams.put(ApplicationMetadata.APP_STATUS, ApplicationMetadata.NOT_AVAILABLE);
                    setChecked = false;
                } else {
                    requestParams.put(ApplicationMetadata.APP_STATUS, ApplicationMetadata.AVAILABLE);
                    setChecked = true;
                }
                DataManager dataManager = new DataManager(getContext());
                dataManager.setCallback(new DataManager.RequestCallback() {
                    @Override
                    public void Data(Object data) {
                        sb_availability.setChecked(setChecked);
                        GlympseApplication.isAvailable = setChecked;
                    }
                });
                dataManager.changeAvailability(requestParams);
            }
        });
        GlympseApplication.getBus().register(this);
        return view;
    }

    private void initGoogleMap() {
        //permission for accessing the location
        LocationUtils locationUtils = new LocationUtils(getActivity());
        locationUtils.showSettingDialog();
        mapFragment = (MapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        //show current location on the map
    }

    @OnClick(R.id.iv_currentLocation)
    public void moveToMyLocation() {
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude())));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_ACCESS_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    //hide the default location button on the map
                    //show myLocation Button here
                    iv_currentLocation.setVisibility(View.VISIBLE);
                } else {
                    DialogFactory.createSimpleOkErrorDialog(getActivity(),
                            R.string.title_permissions,
                            R.string.permission_not_accepted_access_location).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void changeAvailability() {
        sb_availability.setChecked(GlympseApplication.isAvailable);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Subscribe
    public void newRequestFromCustomer(final Customer customer) {
        //new request from the customer load customer data

        PrefsHelper prefsHelper = new PrefsHelper(getContext());
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put(ApplicationMetadata.SESSION_TOKEN, prefsHelper.getPref(ApplicationMetadata.SESSION_TOKEN, ""));
        requestParams.put(ApplicationMetadata.APP_REQUEST_ID, customer.request_id);

        DataManager dataManager = new DataManager(getContext());
        dataManager.setCallback(new DataManager.RequestCallback() {
            @Override
            public void Data(Object data) {
                ResponseData2 responseData = (ResponseData2) data;
                Bundle bundle = new Bundle();
                bundle.putString(ApplicationMetadata.REQUEST_ID,customer.request_id);
                bundle.putString(ApplicationMetadata.USER_NAME,responseData.getCustomerDetail().getName());
                bundle.putString(ApplicationMetadata.ADDRESS,responseData.getAppointment().getAddress());
                bundle.putString(ApplicationMetadata.USER_IMAGE,responseData.getCustomerDetail().getProfilePic());
                bundle.putString(ApplicationMetadata.AVG_RATING,responseData.getCustomerDetail().getRating());
                bundle.putString(ApplicationMetadata.USER_NEED,responseData.getAppointment().getMsg());
                CustomerDetailFragment customerDetailFragment = CustomerDetailFragment.newInstance(bundle);
                customerDetailFragment.show(getActivity().getSupportFragmentManager(), "customer_detail");

            }
        });
        dataManager.getAppointment(requestParams);
    }
}

package mechanic.glympse.glympseprovider.fragment.i_have_arrived;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import mechanic.glympse.glympseprovider.GlympseApplication;
import mechanic.glympse.glympseprovider.R;
import mechanic.glympse.glympseprovider.app.MainActivity;
import mechanic.glympse.glympseprovider.data.model.ResponseData;
import mechanic.glympse.glympseprovider.model.Customer;
import mechanic.glympse.glympseprovider.model.OfferAccepted;
import mechanic.glympse.glympseprovider.utils.ApplicationMetadata;
import mechanic.glympse.glympseprovider.utils.LocationUtils;

/**
 * Created by admin on 12/27/2016.
 */

public class IHaveArrivedFragment extends Fragment implements OnMapReadyCallback, MechOnWayMapView {
    private static final String TAG = IHaveArrivedFragment.class.getSimpleName();
    private static final String MECH_DETAIL = "mech_details";
    private MainActivity activity;
    @BindView(R.id.image_profile) CircleImageView image_profile;
    @BindView(R.id.ll_uperLayout) LinearLayout ll_uperLayout;
    @BindView(R.id.ll_bottomLayout) LinearLayout ll_bottomLayout;
    @BindView(R.id.btn_arrived)
    TextView btn_arrived;
    @BindView(R.id.btn_callCustomer)
    LinearLayout btn_callCustomer;
    @BindView(R.id.tv_requestId)
    TextView tv_requestId;
    private GoogleMap map;
    private MapPresenter presenter;
    private boolean arrived;

    public static IHaveArrivedFragment newInstance(String requestId) {
        IHaveArrivedFragment fragment = new IHaveArrivedFragment();
        Bundle data = new Bundle();
        data.putString(ApplicationMetadata.REQUEST_ID, requestId);
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) (getActivity());
        ((TextView) ((MainActivity) getActivity()).findViewById(R.id.tv_toolbarHeader)).setText(getString(R.string.title_i_have_arrived));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.i_have_arrived_fragment, container, false);
        ButterKnife.bind(this, view);
        GlympseApplication.getBus().register(this);
        tv_requestId.setText(getString(R.string.request_id)+"-"+getArguments().getString(ApplicationMetadata.REQUEST_ID));
        presenter = new MapPresenterImp(this, getActivity(), getContext());
        presenter.setRequestId(getArguments().getString(ApplicationMetadata.REQUEST_ID));
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        presenter.onMapReady();
    }

    @Override
    public void generateMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void setView(OfferAccepted offer) {

    }


    @Override
    public void setMap(LatLng mechLatLng, LatLng customerLatLng) {
        if (map != null) {
            map.clear();

            map.addMarker(new MarkerOptions().position(mechLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_com_repair)));
            map.addMarker(new MarkerOptions().position(customerLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_pin)));
            /*map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    DialogFragment customerDetailFragment = CustomerDetailFragment.newInstance(1);
                    customerDetailFragment.show(getActivity().getSupportFragmentManager(), "customer_detail");
                    return false;
                }
            });*/

            //get current lat lng of the mechanic

            List<LatLng> allLatLng = new ArrayList<>();
            allLatLng.add(customerLatLng);
            allLatLng.add(mechLatLng);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(LocationUtils.computeCentroid(allLatLng), ApplicationMetadata.MAP_ZOOM_VALUE));
        }
    }

    @Override
    public void iHaveArrived() {
        //set uper layout bottom padding to 100dp and bottom layout height to 100 dp
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)getResources().getDimension(R.dimen.layout_height));
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        ll_bottomLayout.setLayoutParams(params);

        ll_uperLayout.setPadding(0,0,0,(int)getResources().getDimension(R.dimen.layout_height));
        btn_arrived.setText("FINISHED");
        image_profile.setVisibility(View.GONE);
        btn_callCustomer.setVisibility(View.GONE);

        arrived = true;
    }

    @Override
    public void setCustomerDetail(ResponseData responseData) {
        Glide.with(this)
                .load(ApplicationMetadata.IMAGE_BASE_URL + responseData.getCustomerDetail().getProfilePic())
                .thumbnail(0.2f)
                .centerCrop()
                .error(R.drawable.ic_profile_photo)
                .into(image_profile);

        //set customer position on map

    }

    @OnClick(R.id.btn_callCustomer)
    public void callCustomer() {
        presenter.callCustomer();
    }



    @OnClick(R.id.btn_arrived)
    public void arrived() {
        presenter.arrived();

        if (arrived) {
            //call finish on the
            presenter.finishTask();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.onStop();
    }


    /*// Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            try{
                URL finalUrl = new URL(url[0]);

                urlConnection = (HttpURLConnection) finalUrl.openConnection();
                // Connecting to url
                urlConnection.connect();

                // Reading data from url
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuffer sb  = new StringBuffer();

                String line = "";
                while( ( line = br.readLine())  != null){
                    sb.append(line);
                }

                data = sb.toString();

                br.close();

            }catch(Exception e){
                Log.d("Exception while downloading url", e.toString());
            }finally{
                try {
                    iStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                urlConnection.disconnect();
            }
            Log.i(TAG, url[0]+"*****" +data);
            return data;
            // For storing data from web service
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(5);
                lineOptions.color(getResources().getColor(R.color.mapLineColor));
            }

            // Drawing polyline in the Google Map for the i-th route
            map.addPolyline(lineOptions);
        }
    }*/

    @Subscribe
    public void getNotification(Customer data) {
        //Toast.makeText(getActivity(), data.message, Toast.LENGTH_LONG).show();

        if (Integer.parseInt(data.status) == ApplicationMetadata.NOTIFICATION_REQ_CANCELLED) {
            //requst cacelled by the user
            presenter.reqeustCancelled(data.message);
        }
    }

}

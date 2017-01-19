package mechanic.glympse.glympseprovider.fragment;


import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import mechanic.glympse.glympseprovider.R;
import mechanic.glympse.glympseprovider.app.MainActivity;
import mechanic.glympse.glympseprovider.data.DataManager;
import mechanic.glympse.glympseprovider.data.local.PrefsHelper;
import mechanic.glympse.glympseprovider.data.model.ResponseData2;
import mechanic.glympse.glympseprovider.fragment.i_have_arrived.IHaveArrivedFragment;
import mechanic.glympse.glympseprovider.model.Customer;
import mechanic.glympse.glympseprovider.utils.ApplicationMetadata;

/**
 * Created by admin on 11/29/2016.
 */

public class CustomerDetailFragment extends DialogFragment {
    private static final String TAG = CustomerDetailFragment.class.getSimpleName();
    @BindView(R.id.tv_address)
    TextView tv_address;
    @BindView(R.id.tv_userName)
    TextView tv_userName;
    @BindView(R.id.tv_needDesc)
    TextView tv_needDesc;
    @BindView(R.id.image_profile)
    CircleImageView image_profile;

    @BindView(R.id.rating_company)
    RatingBar rating_company;
    private Bundle bundle;

    private Customer customer;
    private RequestCompletedCallback requestCallback;
    private PrefsHelper prefsHelper = null;
    private DataManager dataManager = null;
    private ResponseData2 responseData = null;

    public interface RequestCompletedCallback {
        void reqeustCompleted();
    }

    public static CustomerDetailFragment newInstance(Bundle bundle) {
        CustomerDetailFragment f = new CustomerDetailFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putBundle("args", bundle);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefsHelper = new PrefsHelper(getContext());
        dataManager = new DataManager(getContext());
        bundle = getArguments().getBundle("args");
        // Pick a style based on the num.
        int style = DialogFragment.STYLE_NORMAL, theme = 0;
        setStyle(style, R.style.DialogStyle);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.customer_detial_fragment, container, false);
        ButterKnife.bind(this, v);
        setCustomer();
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return v;
    }

    @OnClick(R.id.ll_acceptRequest)
    public void acceptRequest() {

        Map<String, String> requestParams = new HashMap<>();
        requestParams.put(ApplicationMetadata.SESSION_TOKEN, prefsHelper.getPref(ApplicationMetadata.SESSION_TOKEN, ""));
        requestParams.put(ApplicationMetadata.REQUEST_ID, (bundle.getString(ApplicationMetadata.REQUEST_ID)));

        dataManager.setCallback(new DataManager.RequestCallback() {
            @Override
            public void Data(Object data) {
                if ((int) data == ApplicationMetadata.SUCCESS_RESPONSE_STATUS) {

                    //dismiss d
                    CustomerDetailFragment.this.dismiss();
                    Fragment fragment = IHaveArrivedFragment.newInstance((bundle.getString(ApplicationMetadata.REQUEST_ID)));
                    ((MainActivity) getActivity()).addFragmentToStack(fragment, "i_have_arrived");
                } else if ((int) data == ApplicationMetadata.FAILURE_RESPONSE_STATUS) {
                    CustomerDetailFragment.this.dismiss();

                }
                if (requestCallback != null) {
                    requestCallback.reqeustCompleted();
                }
            }
        });

        dataManager.acceptRequest(requestParams);
    }

    @OnClick(R.id.ll_cancelRequest)
    public void cancelRequest() {
        CustomerDetailFragment.this.dismiss();
        /*PrefsHelper prefsHelper = new PrefsHelper(getContext());
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put(ApplicationMetadata.SESSION_TOKEN, prefsHelper.getPref(ApplicationMetadata.SESSION_TOKEN, ""));
        requestParams.put(ApplicationMetadata.REQUEST_ID, customer.request_id);

        *//*requestParams.put(ApplicationMetadata.LATITUDE, (((BaseActivity) getActivity()).currentLocatoin != null) ? ((BaseActivity) getActivity()).currentLocatoin.getLatitude() + "" : "0.0");
        requestParams.put(ApplicationMetadata.LONGITUDE, (((BaseActivity) getActivity()).currentLocatoin != null) ? ((BaseActivity) getActivity()).currentLocatoin.getLongitude() + "" : "0.0");*//*
        DataManager dataManager = new DataManager(getContext());
        dataManager.setCallback(new DataManager.RequestCallback() {
            @Override
            public void Data(Object data) {

                if ((int) data == ApplicationMetadata.SUCCESS_RESPONSE_STATUS) {
                    //dismiss d
                    CustomerDetailFragment.this.dismiss();
                } else if ((int) data == ApplicationMetadata.FAILURE_RESPONSE_STATUS) {
                    CustomerDetailFragment.this.dismiss();

                }
                if (requestCallback != null) {
                    requestCallback.reqeustCompleted();
                }
            }
        });
        dataManager.acceptRequest(requestParams);*/
    }

    @OnClick(R.id.iv_closeDialog)
    public void closeDialog() {
        this.dismiss();
        if (requestCallback != null) {
            requestCallback.reqeustCompleted();
        }
    }

    public void setRequestCompletedCallback(RequestCompletedCallback callback) {
        requestCallback = callback;
    }

    private void setCustomer() {
        tv_address.setText(bundle.getString(ApplicationMetadata.ADDRESS));
        tv_userName.setText(bundle.getString(ApplicationMetadata.USER_NAME));
        tv_needDesc.setText(bundle.getString(ApplicationMetadata.USER_NEED));
        if ((bundle.getString(ApplicationMetadata.AVG_RATING)) != null) {
            rating_company.setRating(Integer.parseInt((bundle.getString(ApplicationMetadata.AVG_RATING))));
        }
        Glide.with(this)
                .load(ApplicationMetadata.IMAGE_BASE_URL + bundle.getString(ApplicationMetadata.USER_IMAGE))
                .thumbnail(0.2f)
                .error(R.drawable.ic_profile_photo)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(image_profile);
    }
}

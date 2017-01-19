package mechanic.glympse.glympseprovider.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import mechanic.glympse.glympseprovider.utils.ApplicationMetadata;
import mechanic.glympse.glympseprovider.utils.DialogFactory;

/**
 * Created by admin on 12/27/2016.
 */

public class RateYourCustomerFragment extends Fragment {
    private static final String TAG  = RateYourCustomerFragment.class.getSimpleName();
    @BindView(R.id.et_experience) EditText et_experience;
    @BindView(R.id.rating_customer) RatingBar rating_customer;
    @BindView(R.id.image_profile) CircleImageView image_profile;
    private PrefsHelper prefsHelper = null;
    private DataManager dataManager = null;
    private ResponseData2 responseData = null;
    public static RateYourCustomerFragment newInstance(String args) {
        RateYourCustomerFragment fragment = new RateYourCustomerFragment();
        Bundle data = new Bundle();
        data.putString("content",args);
        fragment.setArguments(data);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((TextView)((MainActivity) getActivity()).findViewById(R.id.tv_toolbarHeader)).setText(getString(R.string.title_rate_your_customer));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.rate_customer_fragment,container,false);
        ButterKnife.bind(this,view);
        prefsHelper = new PrefsHelper(getContext());
        dataManager = new DataManager(getContext());

        sendCustomerData();
        return view;
    }

    @OnClick(R.id.btn_rateCustomer)
    public void rateCustomer() {
        if (validateField()) {
            Map<String, String> requestParams = new HashMap<>();
            requestParams.put(ApplicationMetadata.SESSION_TOKEN, prefsHelper.getPref(ApplicationMetadata.SESSION_TOKEN, ""));
            requestParams.put(ApplicationMetadata.RATING, rating_customer.getNumStars()+"");
            requestParams.put(ApplicationMetadata.REQUEST_ID, getArguments().getString("content"));
            requestParams.put(ApplicationMetadata.REVIEW, et_experience.getText().toString());

            dataManager.setCallback(new DataManager.RequestCallback() {
                @Override
                public void Data(Object data) {
                    Fragment fragment = HomeFragment.newInstance(0);
                    ((MainActivity)getActivity()).addFragmentToStack(fragment,"home_fragment");
                }
            });

            dataManager.rateCustomer(requestParams);
        }
    }

    public boolean validateField() {
        if (et_experience.getText().toString().isEmpty()) {
            DialogFactory.createSimpleOkErrorDialog(getContext(),"Please share your experience!").show();
            return false;
        }
        return true;
    }

    private void sendCustomerData() {
        PrefsHelper prefsHelper = new PrefsHelper(getContext());
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put(ApplicationMetadata.SESSION_TOKEN, prefsHelper.getPref(ApplicationMetadata.SESSION_TOKEN, ""));
        requestParams.put(ApplicationMetadata.APP_REQUEST_ID, getArguments().getString("content"));

        DataManager dataManager = new DataManager(getContext());
        dataManager.setCallback(new DataManager.RequestCallback() {
            @Override
            public void Data(Object data) {
                responseData = (ResponseData2) data;
                setCustomer();

            }
        });
        dataManager.getAppointment(requestParams);
    }

    private void setCustomer() {

        Glide.with(this)
                .load(ApplicationMetadata.IMAGE_BASE_URL + responseData.getCustomerDetail().getProfilePic())
                .thumbnail(0.2f)
                .error(R.drawable.ic_profile_photo)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(image_profile);
    }
}

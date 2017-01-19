package mechanic.glympse.glympseprovider.data;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.util.Map;

import mechanic.glympse.glympseprovider.GlympseApplication;
import mechanic.glympse.glympseprovider.R;
import mechanic.glympse.glympseprovider.app.LoginActivity;
import mechanic.glympse.glympseprovider.app.MainActivity;
import mechanic.glympse.glympseprovider.app.RegisterActivity;
import mechanic.glympse.glympseprovider.data.local.PrefsHelper;
import mechanic.glympse.glympseprovider.data.model.ApiResponse;
import mechanic.glympse.glympseprovider.data.model.ApiResponse2;
import mechanic.glympse.glympseprovider.data.model.CustomerLocation;
import mechanic.glympse.glympseprovider.data.model.ResponseData;
import mechanic.glympse.glympseprovider.data.model.UserInfo;
import mechanic.glympse.glympseprovider.data.remote.GlympseService;
import mechanic.glympse.glympseprovider.fragment.AboutFragment;
import mechanic.glympse.glympseprovider.fragment.MyProfileFragment;
import mechanic.glympse.glympseprovider.fragment.ServiceHistoryFragment;
import mechanic.glympse.glympseprovider.fragment.TermsNConditionDialogFragment;
import mechanic.glympse.glympseprovider.utils.ApplicationMetadata;
import mechanic.glympse.glympseprovider.utils.DialogFactory;
import mechanic.glympse.glympseprovider.utils.NetworkUtil;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by admin on 11/25/2016.
 */

public class DataManager {
    private static final String TAG = DataManager.class.getSimpleName();
    private GlympseService mApiService;
    private PrefsHelper prefsHelper;
    private Context mContext;
    private RequestCallback mCallback = null;
    private ArrivedCallback mArrivedCallback = null;
    private LocationUpdateCallback mLocationUpdateCallback = null;
    public DataManager(Context context) {
        mContext = context;
        mApiService = GlympseService.Factory.makeFairRepairService(context);
        prefsHelper = new PrefsHelper(context);
    }

    public interface RequestCallback{
        void Data(Object data);
    }
    public void setCallback(RequestCallback mCallback){
        this.mCallback = mCallback;
    }

    public interface ArrivedCallback{
        void arrived();
    }
public void setmArrivedCallback(ArrivedCallback arrivedCallback) {
    this.mArrivedCallback = arrivedCallback;
}
    public interface LocationUpdateCallback{
        void locationReceived(CustomerLocation location);
    }

    public void setmLocationUpdateCallback(LocationUpdateCallback callback) {
        this.mLocationUpdateCallback = callback;
    }

    public void signUp(Map<String, RequestBody> requestMap) {
        if (!NetworkUtil.isNetworkConnected(mContext)) {
            DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.no_connectin).show();
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(mContext.getString(R.string.msg_loading));
        progressDialog.show();
        Call<ApiResponse> call = mApiService.signUp(requestMap);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.body() == null) {
                    progressDialog.dismiss();
                    return;
                }
                int status = response.body().getResponseStatus();
                if (status == ApplicationMetadata.SUCCESS_RESPONSE_STATUS) {
                    String sessionToken = response.body().getResponseData().getSessTok();
                    prefsHelper.savePref(ApplicationMetadata.SESSION_TOKEN, sessionToken);
                    UserInfo userInfo = response.body().getResponseData().getUserInfo();
                    prefsHelper.savePref(ApplicationMetadata.USER_ID, userInfo.getId());
                    prefsHelper.savePref(ApplicationMetadata.USER_NAME, userInfo.getName());
                    prefsHelper.savePref(ApplicationMetadata.USER_EMAIL, userInfo.getEmail());
                    prefsHelper.savePref(ApplicationMetadata.USER_MOBILE, userInfo.getPhoneNo());
                    prefsHelper.savePref(ApplicationMetadata.ADDRESS, userInfo.getAddress());
                    prefsHelper.savePref(ApplicationMetadata.PASSWORD, userInfo.getPassword());
                    prefsHelper.savePref(ApplicationMetadata.USER_IMAGE, userInfo.getProfilePic());
                    prefsHelper.savePref(ApplicationMetadata.USER_ADD_DATE,userInfo.getAddedDate());
                    prefsHelper.savePref(ApplicationMetadata.AVG_RATING,userInfo.getAvgRating());
                    prefsHelper.savePref(ApplicationMetadata.LOGIN, true);
                    //launch home screen activity
                    //DialogFactory.createRegisterSuccessDialog(mContext,R.string.title_success, "You have registered successfully. Please Login").show();
                    Intent intent = new Intent(mContext, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    ((RegisterActivity) mContext).finish();

                } else {
                    progressDialog.dismiss();
                    DialogFactory.createSimpleOkErrorDialog(mContext, response.body().getResponseMsg()).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Log error here since request failed
                progressDialog.dismiss();
                Log.e(TAG, t.toString());
                DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.msg_server_error).show();
            }
        });
    }

    //login
    public void login(final Map<String, String> loginRequest) {
        if (!NetworkUtil.isNetworkConnected(mContext)) {
            DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.no_connectin).show();
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(mContext.getString(R.string.msg_loading));
        progressDialog.show();
        Call<ApiResponse> call = mApiService.login(loginRequest);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.body() == null) {
                    progressDialog.dismiss();
                    return;
                }
                int status = response.body().getResponseStatus();
                if (status == ApplicationMetadata.SUCCESS_RESPONSE_STATUS) {
                    String sessionToken = response.body().getResponseData().getSessTok();
                    prefsHelper.savePref(ApplicationMetadata.SESSION_TOKEN, sessionToken);
                    UserInfo userInfo = response.body().getResponseData().getUserInfo();
                    prefsHelper.savePref(ApplicationMetadata.USER_ID, userInfo.getId());
                    prefsHelper.savePref(ApplicationMetadata.USER_NAME, userInfo.getName());
                    prefsHelper.savePref(ApplicationMetadata.USER_EMAIL, userInfo.getEmail());
                    prefsHelper.savePref(ApplicationMetadata.USER_MOBILE, userInfo.getPhoneNo());
                    prefsHelper.savePref(ApplicationMetadata.ADDRESS, userInfo.getAddress());
                    prefsHelper.savePref(ApplicationMetadata.PASSWORD, userInfo.getPassword());
                    prefsHelper.savePref(ApplicationMetadata.USER_IMAGE, userInfo.getProfilePic());
                    prefsHelper.savePref(ApplicationMetadata.USER_ADD_DATE,userInfo.getAddedDate());
                    prefsHelper.savePref(ApplicationMetadata.AVG_RATING,userInfo.getAvgRating());
                    prefsHelper.savePref(ApplicationMetadata.LOGIN, true);

                    //launch home screen activity
                    Intent intent = new Intent(mContext, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    ((LoginActivity) mContext).finish();
                } else {
                    progressDialog.dismiss();
                    DialogFactory.createSimpleOkErrorDialog(mContext, response.body().getResponseMsg()).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Log error here since request failed
                Log.e(TAG, t.toString());
                progressDialog.dismiss();
                DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.msg_server_error);
            }
        });
    }

    //forgot password
    public void forgotPassword(final Map<String, String> forgotPasswordRequest) {
        if (!NetworkUtil.isNetworkConnected(mContext)) {
            DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.no_connectin).show();
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(mContext.getString(R.string.msg_loading));
        progressDialog.show();
        Call<ApiResponse> call = mApiService.forgotPassword(forgotPasswordRequest);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.body() == null) {
                    progressDialog.dismiss();
                    return;
                }
                int status = response.body().getResponseStatus();
                if (status == ApplicationMetadata.SUCCESS_RESPONSE_STATUS) {
                    DialogFactory.createSimpleOkSuccessDialog(mContext,R.string.title_password_changed, response.body().getResponseMsg()).show();
                } else {
                    DialogFactory.createSimpleOkErrorDialog(mContext, response.body().getResponseMsg()).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Log error here since request failed
                Log.e(TAG, t.toString());
                progressDialog.dismiss();
                DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.msg_server_error);
            }
        });
    }

    //update location of the provider every 5 seconds
    public void updateLatLong(Map<String, String> requestMap) {
        if (!NetworkUtil.isNetworkConnected(mContext)) {
            DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.no_connectin).show();
            return;
        }
        Call<ApiResponse> call = mApiService.updateLatLong(requestMap);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Log error here since request failed
                Log.e(TAG, t.toString());
            }
        });
    }
    //Logout user
    public void logout(final Map<String, String> logoutRequest) {
        if (!NetworkUtil.isNetworkConnected(mContext)) {
            DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.no_connectin).show();
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(mContext.getString(R.string.msg_loading));
        progressDialog.show();
        Call<ApiResponse> call = mApiService.logout(logoutRequest);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.body() == null) {
                    progressDialog.dismiss();
                    return;
                }
                int status = response.body().getResponseStatus();
                if (status == ApplicationMetadata.SUCCESS_RESPONSE_STATUS) {
                    PrefsHelper prefsHelper = new PrefsHelper(mContext);
                    String deviveToken = prefsHelper.getPref(ApplicationMetadata.DEVICE_TOKEN);
                    prefsHelper.clearAllPref();
                    prefsHelper.savePref(ApplicationMetadata.DEVICE_TOKEN, deviveToken);
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    ((Activity)mContext).finish();
                } else {
                    DialogFactory.createSimpleOkErrorDialog(mContext, response.body().getResponseMsg()).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Log error here since request failed
                Log.e(TAG, t.toString());
                progressDialog.dismiss();
                DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.msg_server_error);
            }
        });
    }

    //get profile of the user
    public void getProfile(Map<String, String> requestMap) {
        if (!NetworkUtil.isNetworkConnected(mContext)) {
            DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.no_connectin).show();
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(mContext.getString(R.string.msg_loading));
        progressDialog.show();
        Call<ApiResponse> call = mApiService.getProfile(requestMap);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.body() == null) {
                    progressDialog.dismiss();
                    return;
                }
                int status = response.body().getResponseStatus();
                if (status == ApplicationMetadata.SUCCESS_RESPONSE_STATUS) {
                    UserInfo userInfo = response.body().getResponseData().getUserInfo();
                    prefsHelper.savePref(ApplicationMetadata.USER_ID, userInfo.getId());
                    prefsHelper.savePref(ApplicationMetadata.USER_NAME, userInfo.getName());
                    prefsHelper.savePref(ApplicationMetadata.USER_EMAIL, userInfo.getEmail());
                    prefsHelper.savePref(ApplicationMetadata.USER_MOBILE, userInfo.getPhoneNo());
                    prefsHelper.savePref(ApplicationMetadata.ADDRESS, userInfo.getAddress());
                    prefsHelper.savePref(ApplicationMetadata.PASSWORD, userInfo.getPassword());
                    prefsHelper.savePref(ApplicationMetadata.USER_IMAGE, userInfo.getProfilePic());
                    prefsHelper.savePref(ApplicationMetadata.USER_ADD_DATE,userInfo.getAddedDate());
                    prefsHelper.savePref(ApplicationMetadata.AVG_RATING,userInfo.getAvgRating());

                    Fragment newFragment = MyProfileFragment.newInstance(2);
                    ((MainActivity)mContext).addFragmentToStack(newFragment, "my_profile");
                } else {
                    progressDialog.dismiss();
                    DialogFactory.createSimpleOkErrorDialog(mContext, response.body().getResponseMsg()).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Log error here since request failed
                progressDialog.dismiss();
                Log.e(TAG, t.toString());
                DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.msg_server_error).show();
            }
        });
    }

    //get profile of the user
    public void editProfile(Map<String, RequestBody> requestMap) {
        if (!NetworkUtil.isNetworkConnected(mContext)) {
            DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.no_connectin).show();
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(mContext.getString(R.string.msg_loading));
        progressDialog.show();
        Call<ApiResponse> call = mApiService.editProfile(requestMap);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.body() == null) {
                    progressDialog.dismiss();
                    return;
                }
                int status = response.body().getResponseStatus();
                if (status == ApplicationMetadata.SUCCESS_RESPONSE_STATUS) {
                    UserInfo userInfo = response.body().getResponseData().getUserInfo();
                    prefsHelper.savePref(ApplicationMetadata.USER_ID, userInfo.getId());
                    prefsHelper.savePref(ApplicationMetadata.USER_NAME, userInfo.getName());
                    prefsHelper.savePref(ApplicationMetadata.USER_EMAIL, userInfo.getEmail());
                    prefsHelper.savePref(ApplicationMetadata.USER_MOBILE, userInfo.getPhoneNo());
                    prefsHelper.savePref(ApplicationMetadata.ADDRESS, userInfo.getAddress());
                    prefsHelper.savePref(ApplicationMetadata.PASSWORD, userInfo.getPassword());
                    prefsHelper.savePref(ApplicationMetadata.USER_IMAGE, userInfo.getProfilePic());
                    prefsHelper.savePref(ApplicationMetadata.USER_ADD_DATE,userInfo.getAddedDate());
                    prefsHelper.savePref(ApplicationMetadata.AVG_RATING,userInfo.getAvgRating());


                } else {
                    progressDialog.dismiss();
                    DialogFactory.createSimpleOkErrorDialog(mContext, response.body().getResponseMsg()).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Log error here since request failed
                progressDialog.dismiss();
                Log.e(TAG, t.toString());
                DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.msg_server_error).show();
            }
        });
    }

    //reset password
    public void resetPassword(Map<String, String> requestMap) {
        if (!NetworkUtil.isNetworkConnected(mContext)) {
            DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.no_connectin).show();
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(mContext.getString(R.string.msg_loading));
        progressDialog.show();
        Call<ApiResponse> call = mApiService.resetPassword(requestMap);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.body() == null) {
                    return;
                }
                int status = response.body().getResponseStatus();
                if (status == ApplicationMetadata.SUCCESS_RESPONSE_STATUS) {
                    DialogFactory.createSimpleOkErrorDialog(mContext, response.body().getResponseMsg()).show();

                    Intent intent = new Intent(mContext,LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    ((Activity)mContext).finish();
                } else {
                    progressDialog.dismiss();
                    DialogFactory.createSimpleOkErrorDialog(mContext, response.body().getResponseMsg()).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Log error here since request failed
                progressDialog.dismiss();
                Log.e(TAG, t.toString());
                DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.msg_server_error).show();
            }
        });
    }

    //get static content
    public void getStaticPages(Map<String, String> requestMap, final String type) {
        if (!NetworkUtil.isNetworkConnected(mContext)) {
            DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.no_connectin).show();
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(mContext.getString(R.string.msg_loading));
        progressDialog.show();
        Call<ApiResponse> call = mApiService.getStaticPages(requestMap);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.body() == null) {
                    progressDialog.dismiss();
                    return;
                }
                int status = response.body().getResponseStatus();
                if (status == ApplicationMetadata.SUCCESS_RESPONSE_STATUS) {
                    String content = response.body().getResponseData().getPage().getPagesDesc();

                    if (type.equals(ApplicationMetadata.ABOUT_MECH)) {
                        //launch about us fragment
                        Fragment newFragment = AboutFragment.newInstance(content);
                        ((MainActivity)mContext).addFragmentToStack(newFragment, "about");
                    } else if (type.equals(ApplicationMetadata.TNC_MECH)) {
                        //show tnc dialog
                        DialogFragment customerDetailFragment = TermsNConditionDialogFragment.newInstance(content);
                        customerDetailFragment.show(((RegisterActivity)mContext).getSupportFragmentManager(), "terms_n_condition");
                    }
                } else {
                    progressDialog.dismiss();
                    DialogFactory.createSimpleOkErrorDialog(mContext, response.body().getResponseMsg()).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Log error here since request failed
                progressDialog.dismiss();
                Log.e(TAG, t.toString());
                DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.msg_server_error).show();
            }
        });
    }

    //finish
    public void finished(Map<String, String> requestParams) {
        if (!NetworkUtil.isNetworkConnected(mContext)) {
            DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.no_connectin).show();
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(mContext.getString(R.string.msg_loading));
        progressDialog.show();
        Call<ApiResponse> call = mApiService.finish(requestParams);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.body() == null) {
                    progressDialog.dismiss();
                    return;
                }
                int status = response.body().getResponseStatus();
                if (status == ApplicationMetadata.SUCCESS_RESPONSE_STATUS) {
                    progressDialog.dismiss();
                    mCallback.Data(new Object());
                } else {
                    progressDialog.dismiss();
                    DialogFactory.createSimpleOkErrorDialog(mContext, response.body().getResponseMsg()).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Log error here since request failed
                progressDialog.dismiss();
                Log.e(TAG, t.toString());
                DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.msg_server_error).show();
            }
        });
    }

    //change availability
    public void changeAvailability(final Map<String, String> requestMap) {
        if (!NetworkUtil.isNetworkConnected(mContext)) {
            DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.no_connectin).show();
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(mContext.getString(R.string.msg_loading));
        if(GlympseApplication.isVisible)
            progressDialog.show();
        Call<ApiResponse> call = mApiService.changeAvailability(requestMap);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.body() == null) {
                    progressDialog.dismiss();
                    return;
                }
                int status = response.body().getResponseStatus();
                if (status == ApplicationMetadata.SUCCESS_RESPONSE_STATUS) {
                    //DialogFactory.createSimpleOkSuccessDialog(mContext,R.string.status, response.body().getResponseMsg()).show();
                    mCallback.Data(new Object());
                } else {
                    if(GlympseApplication.isVisible)
                        DialogFactory.createSimpleOkErrorDialog(mContext, response.body().getResponseMsg()).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Log error here since request failed
                progressDialog.dismiss();
                Log.e(TAG, t.toString());
                if(GlympseApplication.isVisible)
                    DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.msg_server_error).show();
            }
        });
    }



    //accept request
    public void acceptRequest(final Map<String, String> requestMap) {
        if (!NetworkUtil.isNetworkConnected(mContext)) {
            DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.no_connectin).show();
            return;
        }
        Log.i("sdfsf","1");
        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(mContext.getString(R.string.msg_loading));
        progressDialog.show();
        Call<ApiResponse> call = mApiService.acceptRequest(requestMap);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.body() == null) {
                    progressDialog.dismiss();
                    return;
                }
                int status = response.body().getResponseStatus();
                if (status == ApplicationMetadata.SUCCESS_RESPONSE_STATUS) {
                    mCallback.Data(ApplicationMetadata.SUCCESS_RESPONSE_STATUS);
                } else {
                    DialogFactory.createSimpleOkErrorDialog(mContext, response.body().getResponseMsg()).show();
                    mCallback.Data(ApplicationMetadata.FAILURE_RESPONSE_STATUS);
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Log error here since request failed
                progressDialog.dismiss();
                Log.e(TAG, t.toString());
                DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.msg_server_error).show();
            }
        });
    }

    //cancel request
    public void rejectRequest(final Map<String, String> requestMap) {
        if (!NetworkUtil.isNetworkConnected(mContext)) {
            DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.no_connectin).show();
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(mContext.getString(R.string.msg_loading));
        progressDialog.show();
        Call<ApiResponse> call = mApiService.rejectRequest(requestMap);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.body() == null) {
                    progressDialog.dismiss();
                    return;
                }
                int status = response.body().getResponseStatus();
                if (status == ApplicationMetadata.SUCCESS_RESPONSE_STATUS) {
                    DialogFactory.createSimpleOkSuccessDialog(mContext,R.string.status, response.body().getResponseMsg()).show();
                    //mCallback.Data(new Object());
                } else {
                    DialogFactory.createSimpleOkErrorDialog(mContext, response.body().getResponseMsg()).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Log error here since request failed
                progressDialog.dismiss();
                Log.e(TAG, t.toString());
                DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.msg_server_error).show();
            }
        });
    }

    //cancel request
    public void rateCustomer(final Map<String, String> requestMap) {
        if (!NetworkUtil.isNetworkConnected(mContext)) {
            DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.no_connectin).show();
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(mContext.getString(R.string.msg_loading));
        progressDialog.show();
        Call<ApiResponse> call = mApiService.rateCustomer(requestMap);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.body() == null) {
                    progressDialog.dismiss();
                    return;
                }
                int status = response.body().getResponseStatus();
                if (status == ApplicationMetadata.SUCCESS_RESPONSE_STATUS) {
                    DialogFactory.createSimpleOkSuccessDialog(mContext,R.string.status, response.body().getResponseMsg()).show();
                    //mCallback.Data(new Object());
                } else {
                    DialogFactory.createSimpleOkErrorDialog(mContext, response.body().getResponseMsg()).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Log error here since request failed
                progressDialog.dismiss();
                Log.e(TAG, t.toString());
                DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.msg_server_error).show();
            }
        });
    }

    //Complete request
    public void completeRequest(final Map<String, String> requestMap) {
        if (!NetworkUtil.isNetworkConnected(mContext)) {
            DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.no_connectin).show();
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(mContext.getString(R.string.msg_loading));
        progressDialog.show();
        Call<ApiResponse> call = mApiService.completeRequest(requestMap);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.body() == null) {
                    progressDialog.dismiss();
                    return;
                }
                int status = response.body().getResponseStatus();
                if (status == ApplicationMetadata.SUCCESS_RESPONSE_STATUS) {
                    DialogFactory.createSimpleOkSuccessDialog(mContext,R.string.status, response.body().getResponseMsg()).show();
                    //mCallback.Data(new Object());
                } else {
                    DialogFactory.createSimpleOkErrorDialog(mContext, response.body().getResponseMsg()).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Log error here since request failed
                progressDialog.dismiss();
                Log.e(TAG, t.toString());
                DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.msg_server_error).show();
            }
        });
    }

    //get customer
    public void getCustomerDetail(final Map<String, String> requestMap) {
        if (!NetworkUtil.isNetworkConnected(mContext)) {
            DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.no_connectin).show();
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(mContext.getString(R.string.msg_loading));
        progressDialog.show();
        Call<ApiResponse> call = mApiService.getCustomerDetail(requestMap);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.body() == null) {
                    progressDialog.dismiss();
                    return;
                }
                int status = response.body().getResponseStatus();
                if (status == ApplicationMetadata.SUCCESS_RESPONSE_STATUS) {
                    ResponseData responseData = response.body().getResponseData();
                    mCallback.Data(responseData);
                } else {
                    DialogFactory.createSimpleOkErrorDialog(mContext, response.body().getResponseMsg()).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Log error here since request failed
                progressDialog.dismiss();
                Log.e(TAG, t.toString());
                DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.msg_server_error).show();
            }
        });
    }

    //get customer
    public void getAppointment(final Map<String, String> requestMap) {
        if (!NetworkUtil.isNetworkConnected(mContext)) {
            DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.no_connectin).show();
            return;
        }
        Call<ApiResponse2> call = mApiService.getCustomerAppointment(requestMap);
        call.enqueue(new Callback<ApiResponse2>() {
            @Override
            public void onResponse(Call<ApiResponse2> call, Response<ApiResponse2> response) {
                if (response.body() == null) {

                    return;
                }
                int status = response.body().getResponseStatus();
                if (status == ApplicationMetadata.SUCCESS_RESPONSE_STATUS) {
                    mCallback.Data(response.body().getResponseData());
                } else {
                    DialogFactory.createSimpleOkErrorDialog(mContext, response.body().getResponseMsg()).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse2> call, Throwable t) {
                // Log error here since request failedF
                Log.e(TAG, t.toString());
                DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.msg_server_error).show();
            }
        });
    }

    //update lat and lng
    //get customer
    public void getLatLng(final Map<String, String> requestMap) {
        if (!NetworkUtil.isNetworkConnected(mContext)) {
            DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.no_connectin).show();
            return;
        }
        /*final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(mContext.getString(R.string.msg_loading));
        progressDialog.show();
        */Call<ApiResponse> call = mApiService.getLatLng(requestMap);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.body() == null) {
                    //progressDialog.dismiss();
                    return;
                }
                int status = response.body().getResponseStatus();
                if (status == ApplicationMetadata.SUCCESS_RESPONSE_STATUS) {
                    mLocationUpdateCallback.locationReceived(response.body().getCustomerLocation());
                } else {
                    DialogFactory.createSimpleOkErrorDialog(mContext, response.body().getResponseMsg()).show();
                }
                //progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Log error here since request failed
//                progressDialog.dismiss();
                Log.e(TAG, t.toString());
                DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.msg_server_error).show();
            }
        });
    }

    //arrived
    public void arrived(final Map<String, String> requestMap) {
        if (!NetworkUtil.isNetworkConnected(mContext)) {
            DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.no_connectin).show();
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(mContext.getString(R.string.msg_loading));
        progressDialog.show();
        Call<ApiResponse> call = mApiService.arrived(requestMap);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.body() == null) {
                    progressDialog.dismiss();
                    return;
                }
                int status = response.body().getResponseStatus();
                if (status == ApplicationMetadata.SUCCESS_RESPONSE_STATUS) {
                    mArrivedCallback.arrived();
                } else {
                    DialogFactory.createSimpleOkErrorDialog(mContext, response.body().getResponseMsg()).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Log error here since request failed
//                progressDialog.dismiss();
                Log.e(TAG, t.toString());
                DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.msg_server_error).show();
            }
        });
    }

    public void getHistory(Map<String, String> requestParams) {
        if (!NetworkUtil.isNetworkConnected(mContext)) {
            DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.no_connectin).show();
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(mContext.getString(R.string.msg_loading));
        progressDialog.show();
        Call<ApiResponse> call = mApiService.getHistory(requestParams);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                int status = response.body().getResponseStatus();
                if (status == ApplicationMetadata.SUCCESS_RESPONSE_STATUS) {

                    /*List<Provider> providerList = response.body().getResponseData().getAllProviders();
                    nearByProvidersCallback.allProviders(providerList);*/
                    mCallback.Data(response.body().getResponseData().getServiceHistory());

                } else {
                    progressDialog.dismiss();
                    DialogFactory.createSimpleOkErrorDialog(mContext, response.body().getResponseMsg()).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Log error here since request failed
                progressDialog.dismiss();
                Log.e(TAG, t.toString());
                DialogFactory.createSimpleOkErrorDialog(mContext, R.string.title_attention, R.string.msg_server_error).show();
            }
        });
    }

}

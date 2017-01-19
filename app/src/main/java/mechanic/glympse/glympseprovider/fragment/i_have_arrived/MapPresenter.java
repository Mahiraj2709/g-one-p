package mechanic.glympse.glympseprovider.fragment.i_have_arrived;


/**
 * Created by admin on 12/27/2016.
 */

public interface MapPresenter {

    void onMapReady();
    void callCustomer();

    void connectToGoogleApiClient();
    void onResume();
    void onStop();
    void onPause();

    void finishTask();

    void setRequestId(String string);

    void arrived();

    void reqeustCancelled(String message);
}

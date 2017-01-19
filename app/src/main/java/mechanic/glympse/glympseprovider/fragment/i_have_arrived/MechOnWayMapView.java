package mechanic.glympse.glympseprovider.fragment.i_have_arrived;

import com.google.android.gms.maps.model.LatLng;

import mechanic.glympse.glympseprovider.data.model.ResponseData;
import mechanic.glympse.glympseprovider.model.OfferAccepted;

/**
 * Created by admin on 12/27/2016.
 */

public interface MechOnWayMapView {
    void generateMap();

    void setView(OfferAccepted offer);

    void setMap(LatLng mechLagLng, LatLng customerLatLng);
    void iHaveArrived();

    void setCustomerDetail(ResponseData responseData);

}

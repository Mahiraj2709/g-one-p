
package mechanic.glympse.glympseprovider.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class ResponseData2 {

    @SerializedName("profile")
    @Expose
    private UserInfo userInfo;
    @SerializedName("session_token")
    @Expose
    private String sessTok;

    @SerializedName("user_type")
    @Expose
    private String userType;
    @SerializedName("error")
    @Expose
    private Object error;

    @SerializedName("content")
    private String staticContent;

    @SerializedName("appointment")
    private Appointment appointment;

    @SerializedName("customer")
    private CustomerDetail customerDetail;


    /**
     * 
     * @return
     *     The userInfo
     */
    public UserInfo getUserInfo() {
        return userInfo;
    }

    /**
     * 
     * @param userInfo
     *     The user_info
     */
    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    /**
     * 
     * @return
     *     The sessTok
     */
    public String getSessTok() {
        return sessTok;
    }

    /**
     * 
     * @param sessTok
     *     The sess_tok
     */
    public void setSessTok(String sessTok) {
        this.sessTok = sessTok;
    }

    /**
     * 
     * @return
     *     The error
     */
    public Object getError() {
        return error;
    }

    /**
     * 
     * @param error
     *     The error
     */
    public void setError(Object error) {
        this.error = error;
    }


    public String getStaticContent() {
        return staticContent;
    }

    public void setStaticContent(String staticContent) {
        this.staticContent = staticContent;
    }


    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public CustomerDetail getCustomerDetail() {
        return customerDetail;
    }

    public void setCustomerDetail(CustomerDetail customerDetail) {
        this.customerDetail = customerDetail;
    }
}

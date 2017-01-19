
package mechanic.glympse.glympseprovider.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


public class ResponseData {

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

    @SerializedName("service_provider")
    private CustomerDetail customerDetail;

    @SerializedName("customer")
    private CustomerDetail appointment;

    @SerializedName("appointment")
    private Appointment mechAppointment;

    @SerializedName("history")
    private List<ServiceHistory> serviceHistory;

    @SerializedName("page")
    private Page page;


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

    public CustomerDetail getCustomerDetail() {
        return customerDetail;
    }

    public void setCustomerDetail(CustomerDetail customerDetail) {
        this.customerDetail = customerDetail;
    }

    public CustomerDetail getAppointment() {
        return appointment;
    }

    public void setAppointment(CustomerDetail appointment) {
        this.appointment = appointment;
    }

    public Appointment getMechAppointment() {
        return mechAppointment;
    }

    public void setMechAppointment(Appointment mechAppointment) {
        this.mechAppointment = mechAppointment;
    }

    public List<ServiceHistory> getServiceHistory() {
        return serviceHistory;
    }

    public void setServiceHistory(List<ServiceHistory> serviceHistory) {
        this.serviceHistory = serviceHistory;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }
}

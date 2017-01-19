
package mechanic.glympse.glympseprovider.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ApiResponse2 {

    @SerializedName("response_msg")
    @Expose
    private String responseMsg;
    @SerializedName("response_key")
    @Expose
    private String responseKey;
    @SerializedName("response_status")
    @Expose
    private Integer responseStatus;
    @SerializedName("response_data")
    @Expose
    private ResponseData2 responseData;

    /**
     * 
     * @return
     *     The responseMsg
     */
    public String getResponseMsg() {
        return responseMsg;
    }

    /**
     * 
     * @param responseMsg
     *     The response_msg
     */
    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }

    /**
     * 
     * @return
     *     The responseStatus
     */
    public Integer getResponseStatus() {
        return responseStatus;
    }

    /**
     * 
     * @param responseStatus
     *     The response_status
     */
    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }

    /**
     * 
     * @return
     *     The responseData
     */
    public ResponseData2 getResponseData() {
        return responseData;
    }

    /**
     * 
     * @param responseData
     *     The response_data
     */
    public void setResponseData(ResponseData2 responseData) {
        this.responseData = responseData;
    }

}

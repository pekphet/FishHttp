package cc.fish.fishhttp.net;

/**
 * Created by fish on 16-4-28.
 */
public class NetException extends Exception {
    private int responseCode;

    public NetException(int responseCode) {
        this.responseCode = responseCode;
        new NetException(getErrorCodeReason(responseCode) + " code : " + responseCode);

    }

    public NetException(String detailMessage) {
        super(detailMessage);
    }



    private String getErrorCodeReason(int code) {
        switch(code) {
            case 300:
            case 301:
            case 302:
                return "Redirect ERROR";
            case 400:
                return "BAD REQUEST!!!";
            case 401:
                return "Unauthorized!!!";
            case 403:
                return "Forbidden Resource";
            case 404:
                return "Page Not Found!";
            case 405:
                return "Request Method Not Allowed!";
            case 408:
                return "Request Timeout!";
            case 500:
                return "Internal Server Error!";
            case 501:
                return "Not Implemented";
            case 502:
                return "Bad Gateway";
            case 503:
                return "Service Unavailable";
            case 504:
                return "Gateway Timeout";
            case 505:
                return "HTTP Version Not Supported";
            default:
                return "Unknown Reason >3<";
        }
    }

    public String getCodeText() {
        return getErrorCodeReason(responseCode) + " code is : " + responseCode;
    }
}

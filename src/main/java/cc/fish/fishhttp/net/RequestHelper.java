package cc.fish.fishhttp.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import cc.fish.fishhttp.thread.Done;
import cc.fish.fishhttp.thread.ThreadPoolManager;
import cc.fish.fishhttp.util.Bean2Map;
import cc.fish.fishhttp.util.ZLog;


/**
 * Created by fish on 16-4-27.
 */
public class RequestHelper<T> {
    private static final int TIME_OUT = 8 * 1000;
    private static final String UTF8 = "UTF-8";
    private static final int DATA_CORRECTLY = 0;

    public Method mMethod;
    public StringBuilder mUrlParam = new StringBuilder();
    private StringBuilder mPostParam = new StringBuilder();
    private Done<T> mSuccess = null;
    private Done<String> mFailed = null;

    private Map<String, String> headerProps = new HashMap<>();


    final static private String NO_NETWORK = "Have No Network!";
    final static private String NO_IO = "I/O Exception Occurred";
    private Class<T> mResultClz ;


    public RequestHelper() {
        super();
    }

    public RequestHelper(String mUrl, Method mMethod) {
        this.mUrlParam = new StringBuilder().append(mUrl);
        this.mMethod = mMethod;
    }

    public RequestHelper Url(String url) {
        this.mUrlParam = new StringBuilder().append(url);
        return this;
    }

    public RequestHelper Method(Method method) {
        mMethod = method;
        return this;
    }

    public RequestHelper Result(Class<T> clz) {
        mResultClz = clz;
        return this;
    }

    public RequestHelper UrlParam(Serializable object, boolean isFirstPara) {
        Map<String, Object> data = Bean2Map.trans(object);
        boolean isFirstParam = isFirstPara;
        for (String key : data.keySet()) {
            mUrlParam.append(isFirstParam ? "?" : "&");
            mUrlParam.append(key).append("=").append(data.get(key));
            isFirstParam = false;
        }
        return this;
    }

    public RequestHelper UrlParam(Serializable object) {
        return UrlParam(object, true);
    }

    public RequestHelper UrlParam(String key, String value, boolean isFirstParam) {
        mUrlParam.append(isFirstParam ? "?" : "&").append(key).append("=").append(value);
        return this;
    }

    public RequestHelper UrlParam(String key, String value) {
        return UrlParam(key, value, false);
    }

    public RequestHelper PostParam(Serializable object) {
        Map<String, Object> data = Bean2Map.trans(object);
        boolean isFirstParam = false;
        for (String key : data.keySet()) {
            if (!isFirstParam) {
                mPostParam.append("&");
            }
            mPostParam.append(key).append("=").append(data.get(key));
        }
        return this;
    }

    public RequestHelper HeaderParam(String key, String value) {
        if (headerProps != null) {
            headerProps.put(key, value);
        }
        return this;
    }

    public void post(Context context, Handler h) {
        if (!hasNet(context)) {
            mFailed.run(NO_NETWORK);
            return;
        }
        ThreadPoolManager.getInstance().addTask(() -> {
            try {
                doPost(h);
            } catch (IOException e) {
                e.printStackTrace();
                doFailed(h, NO_IO);
            } catch (NetException e) {
                e.printStackTrace();
                doFailed(h, e.getCodeText());
            }
        });
    }

    public void get(Context context, Handler h) {
        if (!hasNet(context)) {
            doFailed(h, NO_NETWORK);
            return;
        }
        ThreadPoolManager.getInstance().addTask(() -> {
            try {
                doGet(h);
            } catch (IOException e) {
                e.printStackTrace();
                doFailed(h, NO_IO);
            } catch (NetException e) {
                e.printStackTrace();
                doFailed(h, e.getCodeText());
            }
        });
    }

    public RequestHelper Success(Done<T> successRun){
        mSuccess = successRun;
        return this;
    }

    public RequestHelper Failed(Done<String> failedRun) {
        mFailed = failedRun;
        return this;
    }




    private void doPost(Handler h) throws IOException, NetException {
        HttpURLConnection connection = getConnection(mUrlParam.toString());
        connection.connect();
        OutputStream os = connection.getOutputStream();
        os.write(mPostParam.toString().getBytes(UTF8));
        os.flush();
        os.close();
        doNet(connection, h);
    }

    private void doGet(Handler h) throws IOException, NetException {
        HttpURLConnection connection = getConnection(mUrlParam.toString());
        connection.setDoOutput(false);
        connection.connect();
        doNet(connection, h);
    }

    private void doNet(HttpURLConnection connection, Handler h) throws IOException, NetException {
        int code = connection.getResponseCode();
        if (code != 200) {
            throw new NetException(code);
        }
        InputStream is = connection.getInputStream();
        String responseContent = getStrByInputStream(is);
        ZLog.e("JSON GOT", responseContent);
        T responseObj = new Gson().fromJson(responseContent, mResultClz);
        doSuccess(h, responseObj);
        is.close();
        connection.disconnect();
    }

    private String getStrByInputStream(InputStream is) throws IOException {
        StringBuilder buf = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(is, UTF8);
        char[] charBuf = new char[1024];
        int count = reader.read(charBuf);
        while (count > -1) {
            buf.append(charBuf, 0, count);
            count = reader.read(charBuf);
        }
        reader.close();
        return buf.toString();
    }





    private void doSuccess(Handler h, final T result) {
        if (mSuccess != null && h != null) {
            h.post(()->mSuccess.run(result));
        }
    }

    private void doFailed(Handler h, String msg) {
        if (mFailed != null && h != null) {
            h.post(()->mFailed.run(msg));
        }
    }

    private HttpURLConnection getConnection (String url) {
        HttpURLConnection connection = null;
        try {
            URL u = new URL(url);
            ZLog.e("url", url);
            connection = (HttpURLConnection) u.openConnection();
            connection.setConnectTimeout(TIME_OUT);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Charset", UTF8);
            connection.setRequestProperty("Connection","Keep-Alive");
            // HAS NO SAFETY PARAMS
            connection.setRequestProperty("Content-Length", String.valueOf(mPostParam.length()));
            connection.setRequestProperty("Content-Type", "application/json");
            //set header properties.
            for (String key : headerProps.keySet()) {
                connection.addRequestProperty(key, headerProps.get(key));
            }

            if (mMethod == null) {
                throw new IllegalStateException("please set Method first!!");
            }
            switch (mMethod) {
                case POST:
                    connection.setRequestMethod("POST");
                    break;
                case GET:
                    connection.setRequestMethod("GET");
                    break;
                default:
                    throw new IllegalStateException("please set Method first!!");
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return connection;
    }

    private boolean hasNet(Context context) {
        if (context != null) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                return false;
            }
            NetworkInfo mNetworkInfo = cm.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public enum Method {
        POST,
        GET,
    }
}
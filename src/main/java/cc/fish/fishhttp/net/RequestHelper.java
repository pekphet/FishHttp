package cc.fish.fishhttp.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import cc.fish.fishhttp.thread.Done;
import cc.fish.fishhttp.thread.ThreadPoolManager;
import cc.fish.fishhttp.util.Bean2Map;
import cc.fish.fishhttp.util.MD5Utils;
import cc.fish.fishhttp.util.ZLog;


/**
 * Created by fish on 16-4-27.
 */
public class RequestHelper<T> {
    private static final int TIME_OUT = 8 * 1000;
    private static final String UTF8 = "UTF-8";
    private static final int DATA_CORRECTLY = 0;

    final static String MUL_UPLOAD_BOUNDARY = "Boundary+AAABBB2212341234";
    final static String U_MUL_UPLOAD_BOUNDARY = "--" + MUL_UPLOAD_BOUNDARY + "\r\n";

    public Method mMethod;
    private String mUrl = null;
    public StringBuilder mUrlParam = new StringBuilder();
    private StringBuilder mPostParam = new StringBuilder();
    private Done<T> mSuccess = null;
    private Done<String> mFailed = null;

    private Map<String, String> headerProps = new HashMap<>();
    private boolean isContentTypeJson = false;


    final static private String NO_NETWORK = "Have No Network!";
    final static private String NO_IO = "I/O Exception Occurred";
    private Class<T> mResultClz;
    private TypeToken mTypeToken;
    private SSLContext mSSLContext = null;


    public RequestHelper() {
        super();
    }

    public RequestHelper(String mUrl, Method mMethod) {
        this.mUrlParam = new StringBuilder().append(mUrl);
        this.mMethod = mMethod;
    }

    public RequestHelper Url(String url) {
        mUrl = url;
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

    public RequestHelper ResultType(TypeToken token) {
        mResultClz = null;
        mTypeToken = token;
        return this;
    }

    public RequestHelper UrlParam(Serializable object, boolean isFirstPara) {
        Map<String, Object> data = Bean2Map.trans(object);
        boolean isFirstParam = isFirstPara;
        for (String key : data.keySet()) {
            mUrlParam.append(isFirstParam ? "?" : "&");
            try {
                mUrlParam.append(key).append("=").append(URLEncoder.encode(data.get(key).toString(), "utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            isFirstParam = false;
        }
        return this;
    }

    public RequestHelper UrlParam(Serializable object) {
        return UrlParam(object, true);
    }

    public RequestHelper UrlParam(String key, String value, boolean isFirstParam) {
        if (isFirstParam) {
            mUrlParam = new StringBuilder();
            mUrlParam.append(mUrl);
        }
        try {
            mUrlParam.append(isFirstParam ? "?" : "&").append(key).append("=").append(URLEncoder.encode(value != null ? value : "null", "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return this;
    }

    public RequestHelper UrlParam(String key, String value) {
        return UrlParam(key, value, false);
    }

    public RequestHelper PostParam(Serializable object) {
        if (isContentTypeJson) {
            isContentTypeJson = false;
            mPostParam = new StringBuilder();
        }
        Map<String, Object> data = Bean2Map.trans(object);
        boolean isFirstParam = true;
        for (String key : data.keySet()) {
            if (key.contains("$")) {
                continue;
            }
            if (!isFirstParam) {
                mPostParam.append("&");
            }
            mPostParam.append(key).append("=").append(data.get(key));
            isFirstParam = false;
        }
        return this;
    }

    public RequestHelper PostParam(String key, String value, boolean isFirst) {
        isContentTypeJson = false;
        if (isFirst) {
            mPostParam = new StringBuilder();
        } else {
            mPostParam.append("&");
        }
        mPostParam.append(key).append("=").append(value);
        return this;
    }

    public RequestHelper PostParam(String key, String value) {
        return PostParam(key, value, false);
    }

    public RequestHelper PostJson(Object obj) {
        isContentTypeJson = true;
        mPostParam = new StringBuilder();
        mPostParam.append(new Gson().toJson(obj));
        return this;
    }


    public RequestHelper HeaderParam(String key, String value) {
        if (headerProps != null) {
            headerProps.put(key, value);
        }
        return this;
    }

    public void post(Context context, final Handler h) {
        if (!hasNet(context)) {
            doFailed(h, NO_NETWORK);
            return;
        }
        ThreadPoolManager.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                try {
                    doPost(h);
                } catch (IOException e) {
                    e.printStackTrace();
                    doFailed(h, NO_IO);
                } catch (NetException e) {
                    e.printStackTrace();
                    doFailed(h, e.getCodeText());
                }
            }
        });
    }

    public void syncPost(Context context, final Handler h) {
        if (!hasNet(context)) {
            doFailed(h, NO_NETWORK);
            return;
        }
        ThreadPoolManager.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        doPost(h);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    doFailed(h, NO_IO);
                } catch (NetException e) {
                    e.printStackTrace();
                    doFailed(h, e.getCodeText());
                }
            }
        });
    }

    public void get(Context context, final Handler h) {
        if (!hasNet(context)) {
            doFailed(h, NO_NETWORK);
            return;
        }
        ThreadPoolManager.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                try {
                    doGet(h);
                } catch (IOException e) {
                    e.printStackTrace();
                    doFailed(h, NO_IO);
                } catch (NetException e) {
                    e.printStackTrace();
                    doFailed(h, e.getCodeText());
                }
            }
        });
    }

    public void syncGet(Context context, final Handler h) {
        if (!hasNet(context)) {
            doFailed(h, NO_NETWORK);
            return;
        }
        ThreadPoolManager.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        doGet(h);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    doFailed(h, NO_IO);
                } catch (NetException e) {
                    e.printStackTrace();
                    doFailed(h, e.getCodeText());
                }
            }
        });
    }

    public RequestHelper Success(Done<T> successRun) {
        mSuccess = successRun;
        return this;
    }

    public RequestHelper Failed(Done<String> failedRun) {
        mFailed = failedRun;
        return this;
    }


    private void doPost(Handler h) throws IOException, NetException {
        HttpURLConnection connection = getConnection(mUrlParam.toString());
        connection.setDoOutput(true);
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
            Log.e("NET","code == " + code);
            throw new NetException(code);
        }
        InputStream is = connection.getInputStream();
        String responseContent = getStrByInputStream(is);
//        ZLog.e("JSON GOT", responseContent);
        T responseObj = null;
        try {
            if (mResultClz != null) {
                responseObj = new Gson().fromJson(responseContent, mResultClz);
            } else {
                responseObj = new Gson().fromJson(responseContent, mTypeToken.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (responseObj == null) {
            doFailed(h, "PLZ make sure using correct result type/class");
        } else {
            doSuccess(h, responseObj);
        }

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
            h.post(new Runnable() {
                @Override
                public void run() {
                    mSuccess.run(result);
                }
            });
        }
    }

    private void doFailed(Handler h, final String msg) {
        if (mFailed != null && h != null) {
            h.post(new Runnable() {
                @Override
                public void run() {
                    mFailed.run(msg);
                }
            });
        }
    }

    private HttpURLConnection getConnection(String url) {
        HttpURLConnection connection = null;
        try {
            URL u = new URL(url);
            ZLog.e("url", url);
            connection = (HttpURLConnection) u.openConnection();
            if (mSSLContext != null && connection instanceof HttpsURLConnection) {
                ((HttpsURLConnection)connection).setSSLSocketFactory(mSSLContext.getSocketFactory());
            }
            connection.setConnectTimeout(TIME_OUT);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Charset", UTF8);
            connection.setRequestProperty("Connection", "Keep-Alive");
            // HAS NO SAFETY PARAMS
            //connection.setRequestProperty("Content-Length", String.valueOf(mPostParam.length()));
            connection.setRequestProperty("Content-Type", isContentTypeJson ? "application/json" : "application/x-www-form-urlencoded");
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

//    @Deprecated
//    private void doPostSingleImage(Handler handler, InputStream imgInputStream) throws IOException, NetException {
//        HttpURLConnection connection = getConnection(mUrlParam.toString());
//        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + MUL_UPLOAD_BOUNDARY);
//        connection.setDoOutput(true);
//        connection.connect();
//        OutputStream os = connection.getOutputStream();
//        //APPEND PARAM
//        os.write(getUploadParam("token", "33d1818d3128981b1d6aada15ed3257a"));
//
//        //APPEND IMAGE BOUNDARY & CONTENT
//        os.write(U_MUL_UPLOAD_BOUNDARY.getBytes());
//        os.write("Content-Disposition: form-data; name=\"avatar\"; filename=\"avatar\"\r\n".getBytes());
//        os.write("Content-Type: image/*\r\n\r\n".getBytes());
//
//        byte[] imgBuf = new byte[1024 * 100];
//        while (imgInputStream.read(imgBuf) > 0) {
//            os.write(imgBuf);
//        }
//        //APPEND END BOUNDARY
//        os.write(("\r\n--" + MUL_UPLOAD_BOUNDARY + "--\r\n").getBytes());
//        os.flush();
//        os.close();
//        doNet(connection, handler);
//    }

    private void doPostSingleFile(Handler handler, Map<String, String> postParams, String fileParam, String fileName, String fileType, InputStream imgInputStream) throws IOException, NetException {
        HttpURLConnection connection = getConnection(mUrlParam.toString());
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + MUL_UPLOAD_BOUNDARY);
        connection.setDoOutput(true);
        connection.connect();
        OutputStream os = connection.getOutputStream();
        //APPEND PARAM
        if (postParams != null && postParams.size() > 0) {
            for (String key : postParams.keySet()) {
                os.write(getUploadParam(key, postParams.get(key)));
            }
        }
        //APPEND IMAGE BOUNDARY & CONTENT
        os.write(U_MUL_UPLOAD_BOUNDARY.getBytes());
        os.write(String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"\r\n", fileParam, fileName).getBytes());
        os.write(String.format("Content-Type: %s/*\r\n\r\n", fileType).getBytes());

        byte[] fileBuf = new byte[1024 * 100];
        while (imgInputStream.read(fileBuf) > 0) {
            os.write(fileBuf);
        }
        //APPEND END BOUNDARY
        os.write(("\r\n--" + MUL_UPLOAD_BOUNDARY + "--\r\n").getBytes());
        os.flush();
        os.close();
        doNet(connection, handler);
    }


    private byte[] getUploadParam(String key, String value) {
        String partParam = String.format("%sContent-Disposition: form-data; name=\"%s\"\r\n\r\n%s\r\n",
                U_MUL_UPLOAD_BOUNDARY, key, value);
        return partParam.getBytes();
    }


    /**
     * upload file with post params
     *
     * @param context     Context
     * @param h           Handler
     * @param postParams  post params (key,value) nullable
     * @param fileParam   file's key in post params
     * @param fileName    name of uploading file
     * @param fileType    file's type, eg: image, etc.
     * @param inputStream file's inputStream
     */
    public void postSingleFile(Context context, final Handler h, final Map<String, String> postParams, final String fileParam, final String fileName, final String fileType, final InputStream inputStream) {
        if (!hasNet(context)) {
            doFailed(h, NO_NETWORK);
            return;
        }
        ThreadPoolManager.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                try {
                    doPostSingleFile(h, postParams, fileParam, fileName, fileType, inputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                    doFailed(h, NO_IO);
                } catch (NetException e) {
                    e.printStackTrace();
                    doFailed(h, e.getCodeText());
                }
            }
        });
    }

    /**
     * upload file without post params
     *
     * @param context     Context
     * @param h           Handler
     * @param fileParam   file's key in post params
     * @param fileName    name of uploading file
     * @param fileType    file's type, eg: image, etc.
     * @param inputStream file's inputStream
     */
    public void postSingleFile(Context context, Handler h, String fileParam, String fileName, String fileType, InputStream inputStream) {
        postSingleFile(context, h, null, fileParam, fileName, fileType, inputStream);
    }

    /**
     * upload image file with post params
     *
     * @param context        Context
     * @param h              Handler
     * @param postParams     post params (key,value) nullable
     * @param fileParam      img file's key in post params
     * @param fileName       name of uploading image file
     * @param imgInputStream image's inputStream
     */
    public void postSingleImage(Context context, Handler h, Map<String, String> postParams, String fileParam, String fileName, InputStream imgInputStream) {
        postSingleFile(context, h, postParams, fileParam, fileName, "image", imgInputStream);
    }

    /**
     * upload image file without post params
     *
     * @param context        Context
     * @param h              Handler
     * @param fileParam      img file's key in post params
     * @param fileName       name of uploading image file
     * @param imgInputStream image's inputStream
     */
    public void postSingleImage(Context context, Handler h, String fileParam, String fileName, InputStream imgInputStream) {
        postSingleImage(context, h, null, fileParam, fileName, imgInputStream);
    }

    /****add encrypt************/
    public RequestHelper EncryptSortedParam() {
        Map<String, String> params = new HashMap<>();
        String getPs = mUrlParam.toString();
        getPs = getPs.split("[?]")[1];
        for (String getP : getPs.split("&")) {
            try {
                params.put(getP.split("=")[0], getP.split("=")[1]);
            }catch (Exception ex){
                params.put(getP.split("=")[0], "");

            }
        }
        if (mPostParam != null && mPostParam.length() > 0) {
            String postPs = mPostParam.toString();
            for (String postP : postPs.split("&")) {
                try {
                    params.put(postP.split("=")[0], postP.split("=")[1]);
                }catch (ArrayIndexOutOfBoundsException obEx){
                    obEx.printStackTrace();
                    params.put(postP.split("=")[0], "");
                }
            }
        }
        this.UrlParam("sign", MD5Utils.md5Encrypt(MD5Utils.sortParams(params)));
        return this;
    }

    /****ADD HTTPS SUPPORT****/
    public RequestHelper HTTPS(Context context, String pemFile, String keyFile) {
        InputStream keyIS = null;
        InputStream pemIS = null;
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyIS =context.getAssets().open(keyFile);
            keyStore.load(keyIS, "".toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
            kmf.init(keyStore, "".toCharArray());
            if (pemFile == null) {
                mSSLContext = SSLContext.getInstance("TLS");
                mSSLContext.init(kmf.getKeyManagers(), null, null);
                return this;
            }
            pemIS = context.getAssets().open(pemFile);
            X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(pemIS);
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(trustStore);
            trustStore.load(null);
            trustStore.setCertificateEntry(cert.getSubjectX500Principal().getName(), cert);
            mSSLContext = SSLContext.getInstance("TLS");
            mSSLContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
            mSSLContext = null;
        } finally {
            try {
                keyIS.close();
                pemIS.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    public enum Method {
        POST,
        GET,
    }
}

# FishHttp
A SUPER-LIGHT HTTP REQUEST UTIL

This http request use Annotation & Builder mode to configure request params, so is more easily to use

For android studio developers
After:
1.Download or git clone this module in your project dir.
2.Open settings.gradle add ":fishhttp" behind include ':app' and etc. use comma to separate them
3.Open module setting -> your module which use http util ->Dependencies -> module dependency :fishhttp
4.add uses-permission :
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>

Start use it~~
1.Create a RequestHelper entity~   like this:
@NetMethod(GET or POST)
@NetUrl(your connect url)
@Result(will trans the response json to this class)
RequestHelper<RESULT> request
PS: the generate type will as callback param giving you~
2.if you need url params, you can use the method:
request.UrlParam(Serializable object) -> it returns request self so you can use "." continue~
3. if use post request and need add body params, use this method:
request.PostParam(Serializable object)  it seems like add url param~
4. you can add callback when the request is success or failed
AND THE CODES WILL RUN IN MAIN THREAD!
request.Success(Done<Result> done).Failed(Done<String> msg);
you can use lambda-exp. like this:
request.Success((result) -> {DO SUCCESS WITH result entity})
  .Failed((msg) -> {DO FAILED WITH msg entity})

finally, you only need call request.post/get(Context, Handler) to start http request~~

Happy to send email to me with suggestions or just chat with me~~~
email: pekphet@126.com








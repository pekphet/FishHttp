# FishHttp
A SUPER-LIGHT HTTP REQUEST UTIL

This http request use Annotation & Builder mode to configure request params, so is more easily to use

For android studio developers
After:
1.Download or git clone this module in your project dir.</br>
2.Open settings.gradle add ":fishhttp" behind include ':app' and etc. use comma to separate them</br>
3.Open module setting -> your module which use http util ->Dependencies -> module dependency :fishhttp</br>
4.add uses-permission :</br>
    android.permission.INTERNET</br>
    android.permission.READ_PHONE_STATE</br>
    android.permission.ACCESS_NETWORK_STATE</br>
    android.permission.ACCESS_WIFI_STATE</br>

Start use it</br>
1.Create a RequestHelper entity~   like this:</br>
@NetMethod(GET or POST)</br>
@NetUrl(your connect url)</br>
@Result(will trans the response json to this class)</br>
RequestHelper<RESULT> request</br>
And MUST CALL NetInject to inject annotations to this class  before use this entity~~~</br>
PS: the generate type will as callback param giving you~</br>
2.if you need url params, you can use the method:</br>
request.UrlParam(Serializable object) -> it returns request self so you can use "." continue~</br>
3. if use post request and need add body params, use this method:</br>
request.PostParam(Serializable object)  it seems like add url param~</br>
4. you can add callback when the request is success or failed</br>
AND THE CODES WILL RUN IN MAIN THREAD!</br>
request.Success(Done<Result> done).Failed(Done<String> msg);</br>
you can use lambda-exp. like this:</br>
request.Success((result) -> {DO SUCCESS WITH result entity})</br>
  .Failed((msg) -> {DO FAILED WITH msg entity})</br>

finally, you only need call request.post/get(Context, Handler) to start http request~~</br>

Happy to send email to me with suggestions or just chat with me~~~</br>
email: pekphet@126.com</br>

UPDATE 0.2  </br>
REMOVE BaseEntity Type limit of Request result~






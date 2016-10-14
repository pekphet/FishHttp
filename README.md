# FishHttp
A SUPER-LIGHT HTTP REQUEST UTIL<br/>
PS: Chinese-Readme in  ReadMe-CH.md file<br/>

YOU CAN SEE DEMO IN  <a href="https://github.com/pekphet/FishHttpDemo">FISHHTTP--DEMO--GET</a>

This http request use Annotation & Builder mode to configure request params, so is more easily to use

For android studio developers<br/>
After:<br/>
1.Download or git clone this module in your project dir.</br>
2.Open settings.gradle add ":fishhttp" behind include ':app' and etc. use comma to separate them</br>
3.Open module setting -> your module which use http util ->Dependencies -> module dependency :fishhttp</br>
4.add uses-permission :</br>
    android.permission.INTERNET</br>
    android.permission.READ_PHONE_STATE</br>
    android.permission.ACCESS_NETWORK_STATE</br>
    android.permission.ACCESS_WIFI_STATE</br>
5.In build.gradle of the Project: in node:buildscript -> dependencies<br/>
add classpath 'me.tatarka:gradle-retrolambda:3.2.5' to support lambda exp.<br/>

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
REMOVE BaseEntity Type limit of Request result~<br/>

UPDATE 0.3  <br/>
fix a bug : url params adder bug~ <br/>
support add header properties;<br/>
add url param appender:<br/>
1.UrlParam(REQEST_PARAMS, Boolean isFirst)<br/>
2.UrlParam(Key,Value,Boolean isFirst)<br/>
3.UrlParam(REQEST_PARAMS)  -> default: UrlParam(REQEST_PARAMS, true),used for single param bean;<br/>
4.UrlParam(Key,Value) -> default: UrlParam(Key,Value,false), used for param append quickly~<br/>

UPDATE 0.4<br/>
Change POST request and FIX BUGS in POST METHOD!<br/>
Support for 'application/x-www-form-urlencoded' and 'application/json' in 2 different API<br/>
The API of add body-parameters:<br/>
application/x-www-form-urlencoded use: PostParam(Serializable obj) returns RequestHelper <br/>
application/json use : PostJson(Object obj) returns RequestHelper<br/>
My POST METHOD server may be not open usually~ so you can use post server of your corp.<br/>
And Come in My QQ GROUP for discussion~~<br/>

UPDATE 0.5<BR/>
Fix BUGS in NetInject (NP Exception QwQ)<br/>
Post parameter adder can be used with key-value (default:NOT first)<br/>
Add TypeToken setter  while using List<T> Generic type, U can try TypeToken instead of using Result (class)<br/>
Add sync-post/get request method to keep sync~<br/>

UPDATE 0.51<BR/>
Fix bug:  A bug of OKHttp: post chinese word will be crashed.<br/>

UPDATE 1.0<BR/>
Add upload single file/image file support.<br/>
Using postSingle* Method, PostParam will be not enable.<br/>

UPDATE 1.01<BR/>
FIX ONE BUG, using UrlParam will add multiple when isFirst=true<br/>








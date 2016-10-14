<h1>FishHttp</h1>

是一个超级轻量级的网络HTTP请求框架<br/>

<a href="https://github.com/pekphet/FishHttpDemo">这里有DEMO哦>3<</a>


fishhttp网络框架使用注解和伪Builder方式来配置请求参数， 最大限度的增加便捷性<br/>
对于AndroidStudio的开发者<br/>
准备工作：<br/>
1.Download或者git clone 到本地工程目录中<br/>
2.打开settings.gradle文件添加":fishhttp"在 include ':app', 后面  中间用逗号分隔<br/>
3.打开 module setting （F12快捷键 / F4快捷键） 选择需要使用http框架的工程 然后右边Dependencies -> module dependency :fishhttp<br/>
4.app的AndroidManifest中添加权限 <br/>
android.permission.INTERNET<br/>
android.permission.READ_PHONE_STATE<br/>
android.permission.ACCESS_NETWORK_STATE<br/>
android.permission.ACCESS_WIFI_STATE<br/>
5.在工程的build.gradle的buildscript -> dependencies节点添加<br/>
classpath 'me.tatarka:gradle-retrolambda:3.2.5' 支持lambda表达式<br/>

开始使用：<br/>
1.创建一个RequestHelper的实例 就像这样<br/>
@NetMethod(GET或者POST)<br/>
@NetUrl(你的URL地址～)<br/>
@Result(想在请求中返回的类.class)<br/>
RequestHelper<RESULT> request;<br/>
然后要在调用request之前调用NetInject.inject(this)把注解注入进来<br/>
PS： RequestHelper可以加泛型的 这样直接在回调中会返回想要的类型 方便操作<br/>
2.如果需要拼接URL请求字段 可以调用这个方法：<br/>
request.UrlParam(Serializable obj) 他会返回request自己  所以还可以继续点点点～～<br/>
PS：这个类中 大写字母开头的方法都会返回self的<br/>
3.如果需要添加post请求体数据，可以这样:<br/>
request.PostParam(Serializable obj) 和UrlParam差不多～<br/>
4.你可以设置回调～  <br/>
request.Success(Done<RESULT> done).Failed(Done<String> error msg);<br/>
不去设置也不会出现空指针～<br/>
使用JAVA8的LAMBDA表达式可以这样～<br/>
request.Success((result) -> {//TODO WITH result})<br/>
    .Failed((msg) -> {//TODO with msg});<br/>
最后 调用 request.post或者get(Context, Handler)来发送请求<br/>
<br/>
欢迎拍砖到我的邮箱哦~~<br/>
<br/>
EMAIL: pekphet@126.com<br/>
QQ群: 537947025<br/>

更新0.2<br/>
移除BASEENTITY的限制  可以解析任何javabean<br/>
<br/>

更新0.3<br/>
修正一个添加URL参数方法的BUG (sorry)<br/>
支持HEADER请求参数<br/>
HeaderParam(String key, String value)<br/>
添加多个URL参数添加方法：<br/>
1.UrlParam(REQEST_PARAMS, Boolean isFirst)<br/>
2.UrlParam(Key,Value,Boolean isFirst)<br/>
3.UrlParam(REQEST_PARAMS) -> default: UrlParam(REQEST_PARAMS, true),方便添加单参数<br/>
4.UrlParam(Key,Value) -> default: UrlParam(Key,Value,false), 方便快速的去APPEND url参数。<br/>

更新0.4<br/>
对post请求进行调整， 支持FORM， JSON两种方式提交  <br/>
添加参数的API：  <br/>
FORM 使用PostParam(Serializable obj);方式添加请求体<br/>
JSON提交使用PostJson(Object obj);API 提交<br/>
POST测试服务器会不定期开放～<br/>
可以加入QQ群一起讨论<br/>

久违的更新～～

更新0.5

修正一个在NetInject中出现的空指针BUG QwQ<BR/>
PostParam添加两个方法  可以使用KEY-VALUE形式设置/添加BODY参数 默认isFirst=false<br/>
添加Typetoken设置方法:ResultType 这次就可以使用List<T> 泛型回调了，使用方法ResultType(new TypeToken<List<T>>(){})使用这个方法就会替代Result方法传入的class<br/>
添加同步POST/GET请求可以是请求保持同步调用:syncPost/syncGet。<br/>

更新0.51


修复参数带有中文时会crash的一个BUG, 这是HttpUrlConnection的一个BUG。


更新1.0<br/>
添加上传文件和图片的API,使用这个API的时候，PostParam的添加参数将会无效。


更新1.01<br/>
修复一个BUG：在使用UrlParam的时候，多次调用isFirst=true的参数时不会清空URL，现在可以清空了










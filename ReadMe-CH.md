<h1>FishHttp</h1>

是一个超级轻量级的网络HTTP请求框架

fishhttp网络框架使用注解和伪Builder方式来配置请求参数， 最大限度的增加便捷性
对于AndroidStudio的开发者
准备工作：
1.Download或者git clone 到本地工程目录中
2.打开settings.gradle文件添加":fishhttp"在 include ':app', 后面  中间用逗号分隔
3.打开 module setting （F12快捷键 / F4快捷键） 选择需要使用http框架的工程 然后右边Dependencies -> module dependency :fishhttp<br/>
4.app的AndroidManifest中添加权限 
android.permission.INTERNET
android.permission.READ_PHONE_STATE
android.permission.ACCESS_NETWORK_STATE
android.permission.ACCESS_WIFI_STATE
5.在工程的build.gradle的buildscript -> dependencies节点添加
classpath 'me.tatarka:gradle-retrolambda:3.2.5' 支持lambda表达式

开始使用：
1.创建一个RequestHelper的实例 就像这样
@NetMethod(GET或者POST)
@NetUrl(你的URL地址～)
@Result(想在请求中返回的类.class)
RequestHelper<RESULT> request;
然后要在调用request之前调用NetInject.inject(this)把注解注入进来
PS： RequestHelper可以加泛型的 这样直接在回调中会返回想要的类型 方便操作
2.如果需要拼接URL请求字段 可以调用这个方法：
request.UrlParam(Serializable obj) 他会返回request自己  所以还可以继续点点点～～
PS：这个类中 大写字母开头的方法都会返回self的
3.如果需要添加post请求体数据，可以这样:
request.PostParam(Serializable obj) 和UrlParam差不多～
4.你可以设置回调～  
request.Success(Done<RESULT> done).Failed(Done<String> error msg);
不去设置也不会出现空指针～
使用JAVA8的LAMBDA表达式可以这样～
request.Success((result) -> {//TODO WITH result})
    .Failed((msg) -> {//TODO with msg});
最后 调用 request.post或者get(Context, Handler)来发送请求

欢迎拍砖到我的邮箱哦~~

EMAIL: pekphet@126.com

更新0.2
移除BASEENTITY的限制  可以解析任何javabean












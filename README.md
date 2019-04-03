## 一个使用kotlin编写的Retrofit扩展库

### 特性
1. 支持使用QueryMap标注kotlin data class ；继承即可 BodyMap
2. 快速排除无法Gson序列化的kotlin data class中的属性
3. 支持Retrofit实现Activity的生命周期调用
  
------
### 警告
本库暂未释放，如果需要上述特性，可以下载源码进行参考
> RetrofitOnUI.kt 文件为支持Retrofit实现Activity的生命周期调用，原位工程特定文件，如需使用，请修改后编译
> 编译依赖请参考 library/build.gradle

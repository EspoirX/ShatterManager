# Shatter
复杂业务碎片化管理框架

Shatter 的思想是将复杂的业务和布局拆分成一个个的碎片，每个碎片又可以继续拆出多个子碎片。让复杂的业务变成
一个个相对独立简单的功能，达到解耦的效果（拆分粒度由开发者自己把握）。  
每个碎片的使用方式跟你使用 Activity 一样，简单方便。

Shatter 的意义在于：
1. 将一个复杂业务拆成多个简单业务，解耦，使你的代码易于维护。
2. 在多人协作的项目中，避免不同人员开发同一个业务模块时代码冲突的发生
3. 代码复用率可大大提高

使用方式：

[![](https://jitpack.io/v/EspoirX/ShatterManager.svg)](https://jitpack.io/#EspoirX/ShatterManager)
```groovy
dependencies {
   implementation 'com.github.EspoirX:ShatterManager:vTAG'
}
```

## 基本概念
如何让复杂的业务进行解耦，除了各种设计模式外，有一点就行将复杂的业务拆散，因为再复杂的功能也是由一个个简单的
功能组装起来的。  
每个简单功能，我称之为 “碎片” ，当你的粒度合理时，每个碎片之负责一到几个功能，最后再将这些 “碎片”
组合起来，就能达到解耦的效果了。  
而当两个页面之间用到相同或相似的功能模块时，就可以直接复用，都不用重新写。  

本框架中，每个碎片叫做 **Shatter** 它们统一由 **ShatterManager** 去管理。


## 使用
首先在 Application 中初始化一下：
```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ShatterManager.init(this)
    }
}
```

在你的 Activity 中，实现 IShatterActivity 接口：
```kotlin
class MainActivity : AppCompatActivity(), IShatterActivity {
    private val shatterManager = ShatterManager(this)

    override fun getShatterManager(): ShatterManager = shatterManager
}
```

在 Shatter 内部是通过 ActivityLifecycleCallbacks 监听生命周期的，有些生命周期回调在这个 callback 
里面没有，如果你想用的话，需要手动调用一下，比如：
```kotlin
override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    getShatterManager().onNewIntent(intent)
}

override fun onRestart() {
    super.onRestart()
    getShatterManager().onRestart()
}

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    getShatterManager().onActivityResult(requestCode, resultCode, data)
}

override fun onBackPressed() {
    if (getShatterManager().enableOnBackPressed()) {
        super.onBackPressed()
    }
}
```

然后就可以编写你的 Shatter 了。 Shatter 写完后通过 ShatterManager 去添加管理
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    getShatterManager()
        .addShatter(R.id.shatterALayout, ShatterA())
        .addShatter(ShatterAChild())
        .addShatter(ShatterB())
        .addShatter(ShatterC())
        .start()
}
```

## Shatter用法

Shatter 拥有和 Activity 相同的生命周期，你可以重现它们:onCreate() ,onStart() ... 等等。  
在 onCreate() 里面，还分出了两个方法：initView()，initData() 。合理的重写对应方法去完成逻辑。  

### 1. 当 Fragment 来用
Shatter 不止可以拆分代码逻辑，也可以拆分布局（和Fragment有点像）。  
通过重写 **getLayoutResId()** 方法传入布局。然后在 addShatter 的时候将 Shatter 添加到对应放这个布局的 Layout 中即可：
```kotlin
getShatterManager()
   .addShatter(R.id.shatterALayout, ShatterA())
   .start()

class ShatterA : Shatter() {
    override fun getLayoutResId(): Int = R.layout.layout_shatter_a

    override fun initView(view: View?, intent: Intent?) {
        //......
    }
}
```

在 initView() 中，你可以通过 view.findViewById 去得到相关的控件，同时如果你的项目支持 ViewBinding，也可以直接
通过 getBinding 方法获取到对应的 binding 去操作控件。  

### 2. 只当功能组件使用
如果我的 Shatter 只是有单纯的代码逻辑，不需要布局，比如我就请求个接口等，那么就不需要重写 getLayoutResId() 方法。  
addShatter 的时候直接添加即可：
```kotlin
 getShatterManager()
    .addShatter(ShatterA())
    .start()

class ShatterA : Shatter() {
    
    fun requestUserInfo(){
        //......
    }
}
```

### 3. Shatter 间的通讯
在 ShatterA 中如何获取到 ShatterB 的实例，然后调用 ShatterB 里面的方法？  
可以通过 **findShatter(ShatterB::class.java)** 去获取。  

同样的，ShatterA 中如果要使用 ShatterB 中的布局控件也是可以通过先获取到实例，然后再获取 ViewBinding 得到：  

val binding = findShatter(ShatterB::class.java).binding

**事件通讯：**
除了主动获取对象，Shatter 还支持相互间的事件通讯（类似EventBus）。  
可以通过 **fun sendShatterEvent(key: String, data: Any? = null)** 方法发送事件。  
有两个参数， key 主要用于区分事件，data 就是事件带的数据，可为 null

接收事件需要重写 **fun onShatterEvent(key: String, data: Any?)** 方法。可以看到参数
是和发送方法对齐的。

### 4. 参数存储功能
有时候需要存储一些临时参数或者页面参数等，比如 intent 的数据，为了方便，可以使用 saveData 方法去存储  
通过 getSaveData 方法去获取，这两个方法由 ShatterManager 管理，所以可以全局使用。

### 5. 代码复用和 findShatter 支持接口
有时候我们可能遇到这种情况，两个 Shatter 直接大部分代码逻辑都相同，只是其中一小部分不一样。  
比如我们写了个评论的 Shatter，有两个页面都有评论，它们的逻辑都一样，区别只是调用的接口参数不一样等。  
这时候 Shatter 应该是可以直接复用的，只需要将不一样的抽出来即可。  

那么抽出来自然想到是接口。

下面举个例子看这种情况怎么做（比如点击按钮，toast不一样）：  
首先编写接口抽出功能：
```kotlin
interface IShowToast{
    fun show()
}
```

然后我们各自实现它，同时实现类本身也是一个 Shatter:
```kotlin
/** 页面 A 的 Show Toast*/
class ShowToastShatterAImpl : Shatter() , IShowToast{
    fun show(){
        Toast.show("fuck you")
    }
}

/** 页面 B 的 Show Toast */
class ShowToastShatterBImpl : Shatter() , IShowToast{
    fun show(){
        Toast.show("fuck you too")
    }
}
```

然我我们的业务模块这样写：
```kotlin
class BtnShatter : Shatter {
    val showToast get() = findShatter(IShowToast::class.java)
    
    //......
    showToast?.show()
}
```
在 findShatter 的时候只需要传入对应的接口，内部会自动找到对应的实现类，这样 BtnShatter 我们就可以完全复用，
不相同的地方只需要关注对应的实现类即可。  

添加的时候这样添加：

```kotlin
/** 页面 A 的 碎片管理*/
 shatterManager
     .addShatter(BtnShatter())
     .addShatter(ShowToastShatterAImpl())
     .start()

/** 页面 B 的 碎片管理*/
shatterManager
    .addShatter(BtnShatter())
    .addShatter(ShowToastShatterBImpl())
    .start()
```

更多详情请看 Demo 或者源码，或者通过文章 [《Android 复杂业务碎片化管理方案》](https://juejin.cn/spost/7336750909508206604)  了解更多背景。

有问题可随时交流。




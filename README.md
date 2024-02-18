# Shatter
复杂业务碎片化管理框架

Shatter 的思想是将复杂的业务和布局拆分成一个个的碎片，每个碎片又可以继续拆出多个子碎片。让复杂的业务变成
一个个相对独立简单的功能，达到解耦的效果（拆分粒度由开发者自己把握）。  
每个碎片的使用方式跟你使用 Activity 一样，简单方便。

Shatter 的意义在于：
1. 将一个复杂业务拆成多个简单业务，解耦，使你的代码易于维护。
2. 在多人协作的项目中，避免不同人员开发同一个业务模块时代码冲突的发生
3. 让你在领导面前装个逼，下次裁员就轮不到你了

使用方式：

[![](https://jitpack.io/v/EspoirX/ShatterManager.svg)](https://jitpack.io/#EspoirX/ShatterManager)
```groovy
dependencies {
   implementation 'com.github.EspoirX:ShatterManager:v1.0.0'
}
```

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
        .addShatter(
            R.id.shatterALayout, ShatterA()
                .addChildShatters(ShatterAChild())
        )
        .addShatter(ShatterB())
}
```

使用方式或者代码讲解主要可以通过文章 [《Android 复杂业务碎片化管理方案》]("https://juejin.cn/spost/7336750909508206604")  或者代码中的 demo。  
虽然写得有点随便，但是看完后用法应该能懂吧？有问题可随时交流。




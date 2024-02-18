package com.espoir.shattermanager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.espoir.shatter.IShatterActivity
import com.espoir.shatter.Shatter
import com.espoir.shatter.ShatterManager


class MainActivity : AppCompatActivity(), IShatterActivity {

    private val shatterManager = ShatterManager(this)

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

    override fun getShatterManager(): ShatterManager = shatterManager

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
}

class ShatterA : Shatter() {
    override fun getLayoutResId(): Int = R.layout.layout_shatter_a

    var textView: TextView? = null

    override fun initView(view: View?, intent: Intent?) {
        super.initView(view, intent)
        Log.i("MainActivity", "ShatterA initView")
        textView = view?.findViewById(R.id.textView)
    }
}

class ShatterAChild : Shatter() {

    override fun initView(view: View?, intent: Intent?) {
        super.initView(view, intent)
        findShatter(ShatterA::class.java)?.textView?.setOnClickListener {
            findShatter(ShatterB::class.java)?.showToast()
        }
    }
}

class ShatterB : Shatter() {
    fun showToast() {
        Toast.makeText(activity, "ShatterB showToast", Toast.LENGTH_SHORT).show()
    }
}
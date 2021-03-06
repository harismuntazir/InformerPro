package com.badtools.informer

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val ST_WRITE = 100
    private val util = UtilityMethods
    private lateinit var ctx: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //set lateinit values
        ctx = applicationContext

        //when the app first starts
        if (util.isFirstStart(ctx)) {
            util.setCache(ctx, "FirstStart", "1")
        } else {
            //when the app starts
            onAppStart()
        }


        //get permissions for storage access
        checkForPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,"Storage", ST_WRITE)
        findViewById<Button>(R.id.createList).setOnClickListener {
            saveFields()
            val intent = Intent(this, Downloader::class.java)
            startActivity(intent)
        }

    }

    //on app start
    private fun onAppStart() {
        //get cache values and set them to UI
        findViewById<TextView>(R.id.mainUrl).text = util.getCache(ctx, "mainUrl")
        findViewById<TextView>(R.id.batch).text = util.getCache(ctx, "batch")
        findViewById<TextView>(R.id.midUrl).text = util.getCache(ctx, "midUrl")
        findViewById<TextView>(R.id.pStart).text = util.getCache(ctx, "pStart")
        findViewById<TextView>(R.id.pEnd).text = util.getCache(ctx, "pEnd")
        findViewById<TextView>(R.id.urlEnd).text = util.getCache(ctx, "urlEnd")
    }

    //set cache values
    private fun saveFields() {
        val mainUrl = findViewById<TextView>(R.id.mainUrl).text.toString()
        val urlEnd = findViewById<TextView>(R.id.urlEnd).text.toString()
        val batch = findViewById<TextView>(R.id.batch).text.toString()
        val midUrl = findViewById<TextView>(R.id.midUrl).text.toString()
        val pStart = findViewById<TextView>(R.id.pStart).text.toString()
        val pEnd = findViewById<TextView>(R.id.pEnd).text.toString()
        util.setCache(ctx, "mainUrl", mainUrl)
        util.setCache(ctx, "batch", batch)
        util.setCache(ctx, "midUrl", midUrl)
        util.setCache(ctx, "pStart", pStart)
        util.setCache(ctx, "pEnd", pEnd)
        util.setCache(ctx, "urlEnd", urlEnd)
    }
    //permissions
    private fun checkForPermissions(permission: String, name: String, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(applicationContext, permission) == PackageManager.PERMISSION_GRANTED -> {
                    //Toast.makeText(applicationContext, "$name Permission Granted", Toast.LENGTH_SHORT).show()
                }
                shouldShowRequestPermissionRationale(permission) -> showDialog(permission, name, requestCode)

                else -> ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            }
        }
    }

    private fun permissionCheck(permission: String, name: String, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(applicationContext, permission) == PackageManager.PERMISSION_GRANTED -> toast("Permission Granted !")
                shouldShowRequestPermissionRationale(permission) -> showDialog(permission, name, requestCode)
                else -> ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            when {
                ContextCompat.checkSelfPermission(applicationContext, permission) == PackageManager.PERMISSION_GRANTED -> toast("Permission Granted !")
                shouldShowRequestPermissionRationale(permission) -> showDialog(permission, name, requestCode)
                else -> ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        fun innerCheck(name: String) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                toast("$name permission denied !")
            }
        }
        when (requestCode) {
            ST_WRITE -> innerCheck("Write Storage")
        }
    }
    private fun showDialog(permission: String, name: String, requestCode: Int) {
        val builder = AlertDialog.Builder(this)

        builder.apply {
            setMessage("Permission to access your $name is required to use this app")
            setTitle("$name Permission Required")
            setPositiveButton("OK") { _, _ ->
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
            }
            setNegativeButton("Cancel") { _, _ ->
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
            }
            val dialog = builder.create()
            dialog.show()
        }
    }
    private fun toast(msg: String) {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
    }

}
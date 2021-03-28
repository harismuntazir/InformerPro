package com.badtools.informer

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import kotlinx.coroutines.*
import java.io.*
import java.net.URL

class Downloader : AppCompatActivity() {
    private lateinit var mainUrl: String
    private lateinit var urlEnd: String
    private lateinit var batch: String
    private lateinit var midUrl: String
    private lateinit var pStart: String
    private lateinit var pEnd: String
    private var paramCtr = 0
    private val util = UtilityMethods
    private lateinit var ctx: Context
    var dlLimiter = 1


    @SuppressLint("ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloader)
        ctx = applicationContext

        //set lateinit values from cache
        mainUrl = util.getCache(ctx, "mainUrl")
        batch = util.getCache(ctx, "batch")
        midUrl = util.getCache(ctx, "midUrl")
        pStart = util.getCache(ctx, "pStart")
        pEnd = util.getCache(ctx, "pEnd")
        urlEnd = util.getCache(ctx, "urlEnd")
        //paramCtr = util.getCache(ctx, "paramCtr").toInt()

        val dlBtn = findViewById<Button>(R.id.start_dl)
        dlBtn.setOnClickListener {
            dlBtn.setBackgroundColor(R.color.light_red)
            var path = "${Environment.getStorageDirectory()}/self/primary/Informer/"
            //check if the download folder exists
            isDirExists(path)
            path = "${Environment.getStorageDirectory()}/self/primary/Informer/$batch"
            isDirExists(path)
            //the downloader is now in pENd function
            //so call in the pEnd function
            pEnd()
        }

    }
    //finds total number of profiles to be downloaded
    @RequiresApi(Build.VERSION_CODES.R)
    private fun pEnd() {
        var upperLimit = pEnd.toInt()
        var ctr = 0
        var isClose = 0
        var round = 0
        var lost = true
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                do {
                    val data = getData(createUrl(upperLimit.toString()))
                    ctr++   //keep track for number of turns it took
                    if (upperLimit < 0) {
                        break
                    }
                    if (data.size > 97510) {
                        //save this file to phone storage
                        saveFile(data, makeName(upperLimit.toString()))
                        when (round) {
                            0 -> {
                                upperLimit += 10000
                            }
                            1 -> {
                                isClose = 1
                                upperLimit += 1000
                            }
                            2 -> {
                                isClose = 2
                                upperLimit += 100
                            }
                            3 -> {
                                isClose = 3
                                upperLimit += 10
                            }
                            4 -> {
                                isClose = 4
                                upperLimit++
                            }
                        }
                    } else {
                        when (isClose) {
                            0 -> {
                                round = 1
                                upperLimit -= 5000
                            }
                            1 -> {
                                round = 2
                                upperLimit -= 500
                            }
                            2 -> {
                                round = 3
                                upperLimit -= 50
                            }
                            3 -> {
                                round = 4
                                upperLimit -= 5
                            }
                            4 -> {
                                //finally the job is done
                                upperLimit--
                                lost = false
                            }
                        }
                    }
                    runOnUiThread{
                        findViewById<TextView>(R.id.log).text = upperLimit.toString()
                    }
                } while (lost)

                /*
                runOnUiThread{
                    if (upperLimit < 0) {
                        findViewById<TextView>(R.id.log).text = "No Records Found !"
                    } else {
                        findViewById<TextView>(R.id.log).text = "$upperLimit Profiles Founds, We Have Started Downloading.."
                    }
                }
                 */
                if (upperLimit > 0) {
                    pEnd = upperLimit.toString()
                    //save upperLimit aka pEnd in cahce
                    util.setCache(ctx, "pEnd", pEnd)
                    //start the downloader
                    startDownloader()
                }
            }
        }


    }

    //the main downloader
    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.R)
    private fun startDownloader() {
        val path = "${Environment.getStorageDirectory()}/self/primary/Informer/$batch/"
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                var falseProof = 0
                for (ctr in pStart.toInt() until pEnd.toInt()+1) {
                    //now check fake downloads
                    if (falseProof == 5) {
                        break
                    }
                    //download limiter
                    if (dlLimiter > 10) {
                        dlLimiter = 1
                        delay(1000)
                    }
                    val logID = resources.getIdentifier("log$dlLimiter", "id", packageName)
                    val pbID = resources.getIdentifier("pb$dlLimiter", "id", packageName)
                    dlLimiter++
                    GlobalScope.launch {
                        withContext(Dispatchers.IO) {
                            //start the downloading process
                            runOnUiThread{
                                try {
                                    findViewById<TextView>(logID).text = "Profile $ctr"
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            runOnUiThread {
                                progressBar(300, pbID)
                            }
                            if (!isFileExists(ctr.toString())) {
                                //create link
                                val link = createUrl(ctr.toString())
                                //get file
                                try {
                                    runOnUiThread {
                                        progressBar(400, pbID)
                                    }
                                    val fileData = getData(link)

                                    if (fileData.size <= 97510) {
                                        //this is a fake file so don't save it
                                        falseProof++
                                    } else {
                                        runOnUiThread {
                                            progressBar(850, pbID)
                                        }

                                        //save file
                                        saveFile(fileData, makeName(ctr.toString()))

                                        //save UID counter to cache
                                        util.setCache(ctx, "pStart", ctr.toString())
                                        paramCtr++
                                        util.setCache(ctx, "paramCtr", paramCtr.toString())

                                        runOnUiThread {
                                            progressBar(1000, pbID)
                                            if (tFilesInDir(path) != 0) {
                                                findViewById<TextView>(R.id.log).text = "Downloaded ${tFilesInDir(path)} Profiles"
                                            } else {
                                                findViewById<TextView>(R.id.log).text = "Downloaded $paramCtr Profiles"
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    runOnUiThread {
                                        util.toast(applicationContext, "Check Your Internet Connection")
                                    }
                                    e.printStackTrace()
                                }
                            }
                            runOnUiThread {
                                if (tFilesInDir(path) != 0) {
                                    findViewById<TextView>(R.id.log).text = "Downloaded ${tFilesInDir(path)} Profiles"
                                } else {
                                    findViewById<TextView>(R.id.log).text = "Downloaded $paramCtr Profiles"
                                }
                            }
                        }
                    }

                }

            }
        }
    }

    //make the progress bar work
    private fun progressBar(progressCounter: Int = 0, pbID: Int) {
        try {
            val pb = findViewById<ProgressBar>(pbID)
            pb.max = 1000
            ObjectAnimator.ofInt(pb, "progress", progressCounter)
                    .setDuration(500)
                    .start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    //create link
    private fun createUrl (param: String): String {
        return "$mainUrl$batch$midUrl$param$urlEnd"
    }
    //get data from server
    private suspend fun getData(link: String): ByteArray {
        return withContext(Dispatchers.IO) {
            URL(link).readBytes()
        }
    }
    //create file name
    private fun makeName(param: String): String {
        return "Form No. - $param.pdf"
    }
    //check if directory exists or not
    @RequiresApi(Build.VERSION_CODES.R)
    private fun isDirExists(path: String) {
        //val path = "/mnt/user/0/primary/Informer/"
        try {
            val file = File(path)
            if (!file.exists()) {
                file.mkdir()
                //util.toast(applicationContext, "File created Now !")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            util.toast(applicationContext, "Failed To Create The File")
        }
    }
    //total files in dir
    private fun tFilesInDir(path: String): Int {
        var tFiles = 0
        try {
            val file = File(path)
            tFiles =  file.listFiles().lastIndex+1
            //util.toast(ctx, tFiles.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return tFiles
    }
    //checks if file exists or not
    @RequiresApi(Build.VERSION_CODES.R)
    private fun isFileExists(param: String): Boolean {
        val path = "${Environment.getStorageDirectory()}/self/primary/Informer/$batch/${makeName(param)}"
        val file = File(path)
        return file.exists()
    }
    //save file
    @RequiresApi(Build.VERSION_CODES.R)
    private suspend fun saveFile(fileData: ByteArray, fileName: String) {
        return withContext(Dispatchers.IO) {
            try {
                //create file
                //val path = "/mnt/user/0/primary/Informer/"
                val path = "${Environment.getStorageDirectory()}/self/primary/Informer/$batch"
                val file= File(path,fileName)
                if (!file.exists()) {
                    file.createNewFile()
                }
                val fileOutputStream = FileOutputStream(file)
                fileOutputStream.write(fileData)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
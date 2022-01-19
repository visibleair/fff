package com.example.timetable2

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.timetable2.databinding.ActivityReadFromFileBinding
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ReadFromFileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReadFromFileBinding
    var myDownloadid: Long = 0
    private lateinit var title : String

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadFromFileBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val sdf = SimpleDateFormat("dd.MM.yyyy")
        val c = Calendar.getInstance()
        c.add(Calendar.DATE, 1) // number of days to add
        val tomorrowDate = sdf.format(c.time) // dt is now the new date


        binding.button.setOnClickListener {
            Thread(Runnable {
                val titleArray : ArrayList<String> = ArrayList()
                val linkArray : ArrayList<String> = ArrayList()
                try {
                    val doc: Document = Jsoup.connect("https://www.novsu.ru/univer/timetable/spo/i.1473214//?id=1739778").get()
                    val block: Elements = doc.select("table.viewtablewhite tr")

                    for (i in 1 ..block.size) {
                        val link = block.select("a[href]")
                        titleArray.add(link.eq(i-1).text())
                        linkArray.add(link.eq(i-1).attr("href"))
                        if (titleArray[i-1] == tomorrowDate) {
                            title = titleArray[i-1]
                            downloadWord(linkArray[i-1], titleArray[i-1])
                        }
                    }
                } catch (e: IOException) {
                    println("Error")
                }
                runOnUiThread { binding.outText.text = tomorrowDate.toString() }
            }).start()
        }

        binding.readToFile.setOnClickListener {
            val file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            alignFile("${file}/15.04.2021.doc")
            println{file}
        }
    }

    fun alignFile(inputName: String) {
        try {
            File(inputName).bufferedReader().use {
//                for (line in File(inputName).readLines()) {
//                    for (word in line.split(" ")) {
//                        println(word)
//                    }
//                }
            }
        } catch (e: IOException) {
            Toast.makeText(this, "Прочитать не получилось", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadWord(link : String, title : String) {
        val request = DownloadManager.Request(
            Uri.parse(link))
            .setTitle("$title.doc")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setAllowedOverMetered(true)

        val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        myDownloadid = dm.enqueue(request)

        val br = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == myDownloadid) {
                    Toast.makeText(this@ReadFromFileActivity, "Download completed", Toast.LENGTH_SHORT).show()
                }
            }
        }
        registerReceiver(br, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }
}
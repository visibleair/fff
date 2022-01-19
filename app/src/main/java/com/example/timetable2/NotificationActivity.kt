package com.example.timetable2

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.timetable2.databinding.ActivityNotificationBinding
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class NotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkChangeTimetable()

        binding.notificationImage.setOnClickListener {
            startActivity(Intent(this, ReadFromFileActivity::class.java))
            finish()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun checkChangeTimetable() {
        val sdf = SimpleDateFormat("dd.MM.yyyy")
        val c = Calendar.getInstance()
        c.add(Calendar.DATE, 1) // number of days to add
        val tomorrowDate = sdf.format(c.time) // dt is now the new date

        Thread(Runnable {
            try {
                val titleArray: ArrayList<String> = ArrayList()
                val doc: Document =
                    Jsoup.connect("https://www.novsu.ru/univer/timetable/spo/i.1473214//?id=1739778").get()
                val block: Elements = doc.select("table.viewtablewhite tr")

                for (i in 1..block.size) {
                    val link = block.select("a[href]")
                    titleArray.add(link.eq(i - 1).text())

                    if (titleArray[i - 1] == tomorrowDate) {
                        binding.notificationImage.setImageResource(R.drawable.notification_active)
                        Toast.makeText(this, "Есть изменения в расписании", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                Toast.makeText(this, "Не получилось проверить изменения", Toast.LENGTH_SHORT).show()
            }
            runOnUiThread {}
        }).start()
    }
}
package com.test.nhkapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    companion object {
        val apiKey = "@@@@@@@@@@@@@"
        val area = "130"
        val service = "g1"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actvity_main)

        val btn: Button = findViewById(R.id.btn)
        btn.setOnClickListener {
            showProgramList(createUrl())
        }

        showProgramList(createUrl())
    }

    fun createUrl() : URL {
        val date = LocalDate.now()
        return URL("https://api.nhk.or.jp/v2/pg/list/$area/$service/$date.json?key=$apiKey")
    }

    private fun showProgramList(nhkApi: URL) {
        lifecycleScope.launch {
            val jsonStr = readProgramInfo(nhkApi)
            val listView = findViewById<ListView>(R.id.list_view)
            listView.adapter = createAdapter(jsonStr)
        }
    }

    suspend fun readProgramInfo(nhkApi: URL): String {
        return withContext(Dispatchers.IO) {
            try {
                val br = BufferedReader(InputStreamReader(nhkApi.openStream()))

                val result = StringBuilder()
                br.forEachLine { result.append(it) }
                return@withContext result.toString()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return@withContext ""
        }
    }

    fun createAdapter(jsonStr: String) : ListAdapter {
        var objects: MutableList<String> = mutableListOf();

        val jsonObj = JSONObject(jsonStr)
        val nhkAry = jsonObj.getJSONObject("list").getJSONArray(service)
        for (i in 0..nhkAry.length()-1) {
            val program = nhkAry[i] as JSONObject

            val title = program.getString("title")
            val startDateTime = OffsetDateTime.parse(program.getString("start_time"), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            val endDateTime = OffsetDateTime.parse(program.getString("end_time"), DateTimeFormatter.ISO_OFFSET_DATE_TIME)

            val formatter = DateTimeFormatter.ofPattern("h:mm")
            val start = formatter.format(startDateTime)
            val end = formatter.format(endDateTime)

            objects.add("$start ～ $end\n$title");
        }

        return ArrayAdapter(this, android.R.layout.simple_list_item_1, objects)
    }
}
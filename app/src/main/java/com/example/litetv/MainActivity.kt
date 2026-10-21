package com.example.litetv

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var list: RecyclerView
    private lateinit var status: TextView
    private lateinit var adapter: ChannelAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        list = findViewById(R.id.channel_list)
        status = findViewById(R.id.status)
        adapter = ChannelAdapter { ch -> openPlayer(ch) }
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter
        findViewById<View>(R.id.btn_source).setOnClickListener { askSource() }
        load()
    }

    private fun load() {
        val prefs = getSharedPreferences("cfg", MODE_PRIVATE)
        val url = prefs.getString("m3u_url", null)
        if (url.isNullOrBlank()) {
            status.text = getString(R.string.hint_set_source)
            return
        }
        status.text = getString(R.string.loading)
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val text = withContext(Dispatchers.IO) { URL(url).readText() }
                val channels = M3uParser.parse(text)
                ChannelStore.channels = channels
                adapter.submit(channels)
                status.text = getString(R.string.loaded_n, channels.size)
            } catch (e: Exception) {
                status.text = getString(R.string.load_failed, e.message ?: "?")
            }
        }
    }

    private fun askSource() {
        val prefs = getSharedPreferences("cfg", MODE_PRIVATE)
        val input = EditText(this).apply {
            setText(prefs.getString("m3u_url", ""))
            hint = "https://example.com/playlist.m3u"
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.set_source)
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                prefs.edit { putString("m3u_url", input.text.toString().trim()) }
                load()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun openPlayer(ch: Channel) {
        ChannelStore.current = ChannelStore.channels.indexOf(ch)
        startActivity(
            Intent(this, PlayerActivity::class.java)
                .putExtra("url", ch.url)
                .putExtra("name", ch.name)
        )
    }
}

/** Simple in-memory store so the player can switch channels without parceling. */
object ChannelStore {
    var channels: List<Channel> = emptyList()
    var current: Int = 0
}

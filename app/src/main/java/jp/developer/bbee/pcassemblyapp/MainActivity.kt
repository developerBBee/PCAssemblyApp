package jp.developer.bbee.pcassemblyapp

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.HandlerCompat
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import jp.developer.bbee.pcassemblyapp.databinding.ActivityMainBinding
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // Preparation settings for using API getDeviceList()
    val deviceTypeList = listOf(
        "pccase",
        "motherboard",
        "powersupply",
        "cpu",
        "cpucooler",
        "pcmemory",
        "hdd35inch",
        "ssd",
        "videocard",
        "ossoft",
        "lcdmonitor",
        "keyboard",
        "mouse",
        "dvddrive",
        "bluraydrive",
        "soundcard",
        "pcspeaker",
        "fancontroller",
        "casefan"
    )
    val API_URL = "https://www.pcbuilding.link/api/devicelist"
    val QUERY_PREFIX = "?device="

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var returnBool = true

        var device = ""
        Log.d("DEBUG", "item.order = ${item.order}")
        when (item.itemId) {
            R.id.action_pccase -> device = deviceTypeList[0]
            R.id.action_mother -> device = deviceTypeList[1]
            R.id.action_psu    -> device = deviceTypeList[2]
            R.id.action_cpu    -> device = deviceTypeList[3]
            R.id.action_cooler -> device = deviceTypeList[4]
            R.id.action_memory -> device = deviceTypeList[5]
            R.id.action_hdd    -> device = deviceTypeList[6]
            R.id.action_ssd    -> device = deviceTypeList[7]
            R.id.action_video  -> device = deviceTypeList[8]
            else -> returnBool = super.onOptionsItemSelected(item)
        }
        val handler = HandlerCompat.createAsync(mainLooper)

        val runner = Runnable {
            var jsonText = ""
            val url = URL(API_URL + QUERY_PREFIX + device)
            val connection = url.openConnection() as? HttpURLConnection

            connection?.let {
                try {
                    it.connectTimeout = 1000
                    it.readTimeout = 1000
                    it.requestMethod = "GET"
                    it.connect()

                    val inputStream = it.inputStream
                    jsonText = inputStream.bufferedReader(charset("UTF-8")).readLines().toString()
                    inputStream.close()
                } catch (e : SocketTimeoutException) {
                    Log.w("DEBUG", "API getDeviceList() request timed out...", e)
                }
                it.disconnect()
            }

            H(handler, jsonText).run()

        }

        Thread {
            runner.run()
        }.start()

        return returnBool
    }

    private inner class H(handler: Handler, str: String) : Runnable {
        private val _handler = handler
        private val _str = str

        override fun run() {
            val postExecutor = PostExecutor(_str)
            _handler.post(postExecutor)
        }
    }

    private inner class PostExecutor(text: String) : Runnable {
        var _text = text

        @UiThread
        override fun run() {
            findViewById<TextView>(R.id.jsonTextView).text = _text
        }
    }

}
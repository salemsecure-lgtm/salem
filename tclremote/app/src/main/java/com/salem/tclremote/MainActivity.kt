package com.salem.tclremote

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.salem.tclremote.databinding.ActivityMainBinding
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private lateinit var prefs: android.content.SharedPreferences

    private var remote: RemoteClient? = null
    private var discovery: TvDiscovery? = null
    private val devices = LinkedHashMap<String, Pair<String, Int>>()

    private var currentHost: String? = null
    private var currentPort: Int = 6466
    private var connected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        prefs = getSharedPreferences("tclremote", MODE_PRIVATE)
        CertStore.load(this) // generate the client identity up front

        currentHost = prefs.getString("host", null)
        currentPort = prefs.getInt("port", 6466)

        wireButtons()
        wireTouchpad()
        wireModeToggle()
        b.btnDevices.setOnClickListener { showDeviceChooser() }

        updateStatus()
        startDiscovery()
    }

    // ---- UI wiring -------------------------------------------------------

    private fun wireButtons() {
        val map = mapOf(
            b.btnPower to Keys.POWER,
            b.btnInput to Keys.TV_INPUT,
            b.btnSettings to Keys.SETTINGS,
            b.btnUp to Keys.UP,
            b.btnDown to Keys.DOWN,
            b.btnLeft to Keys.LEFT,
            b.btnRight to Keys.RIGHT,
            b.btnOk to Keys.CENTER,
            b.btnBack to Keys.BACK,
            b.btnHome to Keys.HOME,
            b.btnMenu to Keys.MENU,
            b.btnGuide to Keys.GUIDE,
            b.btnVolUp to Keys.VOL_UP,
            b.btnVolDown to Keys.VOL_DOWN,
            b.btnMute to Keys.MUTE,
            b.btnChUp to Keys.CH_UP,
            b.btnChDown to Keys.CH_DOWN,
            b.btnPrev to Keys.PREV,
            b.btnRewind to Keys.REWIND,
            b.btnPlayPause to Keys.PLAY_PAUSE,
            b.btnFfwd to Keys.FFWD,
            b.btnNext to Keys.NEXT,
            b.btnStop to Keys.STOP,
        )
        for ((button, code) in map) button.setOnClickListener { sendKey(code) }
    }

    private fun wireModeToggle() {
        b.modeToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val touchpad = checkedId == b.btnModeTouch.id
            b.touchpadContainer.visibility = if (touchpad) View.VISIBLE else View.GONE
            b.dpadContainer.visibility = if (touchpad) View.GONE else View.VISIBLE
        }
        b.modeToggle.check(b.btnModeDpad.id)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun wireTouchpad() {
        var accX = 0f
        var accY = 0f
        val step = 110f
        val detector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                accX = 0f; accY = 0f
                return true
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                sendKey(Keys.CENTER)
                return true
            }

            override fun onScroll(
                e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float,
            ): Boolean {
                accX += distanceX
                accY += distanceY
                if (abs(accX) > abs(accY)) {
                    while (abs(accX) >= step) {
                        sendKey(if (accX > 0) Keys.LEFT else Keys.RIGHT)
                        accX -= Math.copySign(step, accX)
                    }
                    accY = 0f
                } else {
                    while (abs(accY) >= step) {
                        sendKey(if (accY > 0) Keys.UP else Keys.DOWN)
                        accY -= Math.copySign(step, accY)
                    }
                    accX = 0f
                }
                return true
            }
        })
        b.touchpad.setOnTouchListener { v, e ->
            if (e.actionMasked == MotionEvent.ACTION_DOWN) v.performClick()
            detector.onTouchEvent(e)
        }
    }

    // ---- Sending ---------------------------------------------------------

    private fun sendKey(code: Int) {
        val r = remote
        if (r == null || !connected) {
            toast("Not connected — tap “Devices” to connect")
            return
        }
        r.sendKey(code)
    }

    // ---- Discovery & connection -----------------------------------------

    private fun startDiscovery() {
        discovery = TvDiscovery(this) { name, host, port ->
            runOnUiThread { devices[name] = host to port }
        }.also { it.start() }
    }

    private fun showDeviceChooser() {
        val names = devices.keys.toList()
        val items = (names + "Enter IP address…").toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Select your TCL TV")
            .setItems(items) { _, which ->
                if (which < names.size) {
                    val (host, port) = devices[names[which]]!!
                    connectTo(host, port)
                } else {
                    promptManualIp()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun promptManualIp() {
        val input = EditText(this).apply {
            hint = "192.168.1.x"
            setText(currentHost ?: "")
            inputType = InputType.TYPE_CLASS_TEXT
        }
        AlertDialog.Builder(this)
            .setTitle("TV IP address")
            .setView(input)
            .setPositiveButton("Connect") { _, _ ->
                val ip = input.text.toString().trim()
                if (ip.isNotEmpty()) connectTo(ip, 6466)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun connectTo(host: String, port: Int) {
        disconnect()
        currentHost = host
        currentPort = port
        prefs.edit().putString("host", host).putInt("port", port).apply()
        updateStatus("Connecting to $host…")

        remote = RemoteClient(
            context = this,
            host = host,
            port = port,
            onReady = {
                runOnUiThread {
                    connected = true
                    updateStatus()
                    toast("Connected")
                }
            },
            onDisconnect = { err ->
                runOnUiThread { onRemoteDisconnect(host, err) }
            },
        ).also { it.start() }
    }

    private fun onRemoteDisconnect(host: String, err: String?) {
        val wasConnected = connected
        connected = false
        updateStatus()
        if (!wasConnected) {
            AlertDialog.Builder(this)
                .setTitle("Pair with TV")
                .setMessage(
                    "Couldn't connect to $host.\n\n" +
                        "If this is the first time connecting, you need to pair. " +
                        "Start pairing now? A code will appear on your TV screen."
                )
                .setPositiveButton("Pair") { _, _ -> startPairing(host) }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            toast("Disconnected" + (err?.let { ": $it" } ?: ""))
        }
    }

    // ---- Pairing ---------------------------------------------------------

    private fun startPairing(host: String) {
        updateStatus("Pairing with $host…")
        val client = PairingClient(this, host)
        Thread {
            try {
                client.connectAndRequest(Build.MODEL ?: "Android phone")
                runOnUiThread { promptCode(client, host) }
            } catch (e: Exception) {
                runOnUiThread {
                    updateStatus()
                    toast("Pairing failed: ${e.message}")
                }
            }
        }.start()
    }

    private fun promptCode(client: PairingClient, host: String) {
        val input = EditText(this).apply {
            hint = "6-digit code from TV"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
        }
        AlertDialog.Builder(this)
            .setTitle("Enter pairing code")
            .setMessage("Type the code shown on your TV.")
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("Pair") { _, _ ->
                val code = input.text.toString().trim()
                Thread {
                    try {
                        val ok = client.sendSecret(code)
                        runOnUiThread {
                            if (ok) {
                                toast("Paired! Connecting…")
                                connectTo(host, currentPort)
                            } else {
                                updateStatus()
                                toast("Pairing rejected — check the code")
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            updateStatus()
                            toast("Pairing error: ${e.message}")
                        }
                    }
                }.start()
            }
            .setNegativeButton("Cancel") { _, _ ->
                client.close()
                updateStatus()
            }
            .show()
    }

    // ---- Misc ------------------------------------------------------------

    private fun disconnect() {
        remote?.stop()
        remote = null
        connected = false
    }

    private fun updateStatus(message: String? = null) {
        b.tvStatus.text = message ?: when {
            connected -> "Connected · $currentHost"
            currentHost != null -> "Not connected · $currentHost"
            else -> "No TV selected — tap Devices"
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    override fun onDestroy() {
        super.onDestroy()
        discovery?.stop()
        disconnect()
    }
}

package com.salem.tclremote

import android.content.Context
import android.os.Build
import com.salem.tclremote.remote.RemoteProto.RemoteConfigure
import com.salem.tclremote.remote.RemoteProto.RemoteDeviceInfo
import com.salem.tclremote.remote.RemoteProto.RemoteDirection
import com.salem.tclremote.remote.RemoteProto.RemoteKeyInject
import com.salem.tclremote.remote.RemoteProto.RemoteMessage
import com.salem.tclremote.remote.RemoteProto.RemotePingResponse
import com.salem.tclremote.remote.RemoteProto.RemoteSetActive
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.util.concurrent.Executors
import javax.net.ssl.SSLSocket

/**
 * Control connection (port 6466). Runs its own reader thread and answers the TV's
 * configure / set-active / ping messages so the session stays alive.
 */
class RemoteClient(
    private val context: Context,
    private val host: String,
    private val port: Int,
    private val onReady: () -> Unit,
    private val onDisconnect: (String?) -> Unit,
) {
    @Volatile
    private var running = false
    private var socket: SSLSocket? = null
    private var output: BufferedOutputStream? = null
    private val writer = Executors.newSingleThreadExecutor()
    @Volatile
    private var notified = false

    fun start() {
        running = true
        Thread({ runLoop() }, "tcl-remote").start()
    }

    private fun runLoop() {
        try {
            val ctx = CertStore.sslContext(context)
            val s = ctx.socketFactory.createSocket(host, port) as SSLSocket
            s.startHandshake()
            socket = s
            val input = BufferedInputStream(s.inputStream)
            output = BufferedOutputStream(s.outputStream)

            while (running) {
                val msg = RemoteMessage.parseDelimitedFrom(input) ?: break
                handle(msg)
            }
            finish(null)
        } catch (e: Exception) {
            finish(e.message ?: "connection error")
        } finally {
            running = false
            try {
                socket?.close()
            } catch (_: Exception) {
            }
        }
    }

    private fun handle(msg: RemoteMessage) {
        when {
            msg.hasRemoteConfigure() -> send(
                RemoteMessage.newBuilder().setRemoteConfigure(
                    RemoteConfigure.newBuilder()
                        .setCode1(622)
                        .setDeviceInfo(
                            RemoteDeviceInfo.newBuilder()
                                .setModel(Build.MODEL ?: "Android")
                                .setVendor(Build.MANUFACTURER ?: "Android")
                                .setUnknown1(1)
                                .setUnknown2("1")
                                .setPackageName("com.salem.tclremote")
                                .setAppVersion("1.0")
                        )
                ).build()
            )

            msg.hasRemoteSetActive() -> send(
                RemoteMessage.newBuilder()
                    .setRemoteSetActive(RemoteSetActive.newBuilder().setActive(622))
                    .build()
            )

            msg.hasRemoteStart() -> if (msg.remoteStart.started) onReady()

            msg.hasRemotePingRequest() -> send(
                RemoteMessage.newBuilder()
                    .setRemotePingResponse(
                        RemotePingResponse.newBuilder().setVal1(msg.remotePingRequest.val1)
                    )
                    .build()
            )
        }
    }

    /** Sends a single short key press (press + release). */
    fun sendKey(keyCode: Int) {
        send(
            RemoteMessage.newBuilder().setRemoteKeyInject(
                RemoteKeyInject.newBuilder()
                    .setKeyCode(keyCode)
                    .setDirection(RemoteDirection.SHORT)
            ).build()
        )
    }

    // All socket writes happen on a dedicated thread so the UI thread never does network I/O.
    private fun send(m: RemoteMessage) {
        if (!running && output == null) return
        try {
            writer.execute {
                val o = output ?: return@execute
                try {
                    m.writeDelimitedTo(o)
                    o.flush()
                } catch (e: Exception) {
                    finish(e.message ?: "write error")
                }
            }
        } catch (_: Exception) {
            // executor already shut down
        }
    }

    @Synchronized
    private fun finish(error: String?) {
        if (notified) return
        notified = true
        onDisconnect(error)
    }

    fun stop() {
        running = false
        try {
            socket?.close()
        } catch (_: Exception) {
        }
        try {
            writer.shutdownNow()
        } catch (_: Exception) {
        }
    }
}

package com.salem.tclremote

import android.content.Context
import com.google.protobuf.ByteString
import com.salem.tclremote.polo.PoloProto.Configuration
import com.salem.tclremote.polo.PoloProto.Options
import com.salem.tclremote.polo.PoloProto.OuterMessage
import com.salem.tclremote.polo.PoloProto.PairingRequest
import com.salem.tclremote.polo.PoloProto.Secret
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey
import javax.net.ssl.SSLSocket

/**
 * Implements the Polo pairing handshake (port 6467).
 *
 *   request  -> ack
 *   options  -> options
 *   config   -> config-ack   (TV now displays a 6 hex-digit code)
 *   secret   -> secret-ack   (computed from both certificates + the code)
 *
 * All calls are blocking; run them off the main thread.
 */
class PairingClient(
    private val context: Context,
    private val host: String,
    private val port: Int = 6467,
) {
    private var socket: SSLSocket? = null
    private lateinit var input: BufferedInputStream
    private lateinit var output: BufferedOutputStream
    private lateinit var serverCert: X509Certificate

    /** Connects, performs the handshake up to the point where the TV shows the code. */
    fun connectAndRequest(clientName: String) {
        val ctx = CertStore.sslContext(context)
        val s = ctx.socketFactory.createSocket(host, port) as SSLSocket
        s.soTimeout = 15000
        s.startHandshake()
        socket = s
        input = BufferedInputStream(s.inputStream)
        output = BufferedOutputStream(s.outputStream)
        serverCert = s.session.peerCertificates[0] as X509Certificate

        send(
            outer(OuterMessage.MessageType.MESSAGE_TYPE_PAIRING_REQUEST)
                .setPairingRequest(
                    PairingRequest.newBuilder()
                        .setServiceName("androidtvremote")
                        .setClientName(clientName)
                )
        )
        expectOk()

        send(
            outer(OuterMessage.MessageType.MESSAGE_TYPE_OPTIONS)
                .setOptions(
                    Options.newBuilder()
                        .setPreferredRole(Options.RoleType.ROLE_TYPE_INPUT)
                        .addInputEncodings(
                            Options.Encoding.newBuilder()
                                .setType(Options.Encoding.EncodingType.ENCODING_TYPE_HEXADECIMAL)
                                .setSymbolLength(6)
                        )
                )
        )
        expectOk()

        send(
            outer(OuterMessage.MessageType.MESSAGE_TYPE_CONFIGURATION)
                .setConfiguration(
                    Configuration.newBuilder()
                        .setClientRole(Options.RoleType.ROLE_TYPE_INPUT)
                        .setEncoding(
                            Options.Encoding.newBuilder()
                                .setType(Options.Encoding.EncodingType.ENCODING_TYPE_HEXADECIMAL)
                                .setSymbolLength(6)
                        )
                )
        )
        expectOk() // CONFIGURATION_ACK — TV is now showing the code
    }

    /** Sends the secret derived from [code]; returns true when the TV accepts the pairing. */
    fun sendSecret(code: String): Boolean {
        val hash = computeSecret(code)
        send(
            outer(OuterMessage.MessageType.MESSAGE_TYPE_SECRET)
                .setSecret(Secret.newBuilder().setSecret(ByteString.copyFrom(hash)))
        )
        val ack = read()
        close()
        return ack.status == OuterMessage.Status.STATUS_OK &&
            ack.type == OuterMessage.MessageType.MESSAGE_TYPE_SECRET_ACK
    }

    private fun computeSecret(code: String): ByteArray {
        val clientPub = CertStore.clientCertificate(context).publicKey as RSAPublicKey
        val serverPub = serverCert.publicKey as RSAPublicKey

        val codeBytes = hexToBytes(code)
        require(codeBytes.size >= 2) { "Code must be at least 2 hex characters" }
        val nonce = codeBytes.copyOfRange(1, codeBytes.size)

        val md = MessageDigest.getInstance("SHA-256")
        md.update(unsigned(clientPub.modulus))
        md.update(unsigned(clientPub.publicExponent))
        md.update(unsigned(serverPub.modulus))
        md.update(unsigned(serverPub.publicExponent))
        md.update(nonce)
        val digest = md.digest()

        require(digest[0] == codeBytes[0]) { "Wrong code" }
        return digest
    }

    /** Big-endian unsigned representation (drops the sign byte BigInteger may prepend). */
    private fun unsigned(v: BigInteger): ByteArray {
        val b = v.toByteArray()
        return if (b.size > 1 && b[0].toInt() == 0) b.copyOfRange(1, b.size) else b
    }

    private fun hexToBytes(s: String): ByteArray {
        val clean = s.trim().filter { !it.isWhitespace() }
        require(clean.length % 2 == 0) { "Code must have an even number of hex characters" }
        val out = ByteArray(clean.length / 2)
        for (i in out.indices) {
            val hi = Character.digit(clean[i * 2], 16)
            val lo = Character.digit(clean[i * 2 + 1], 16)
            require(hi >= 0 && lo >= 0) { "Code is not valid hexadecimal" }
            out[i] = ((hi shl 4) + lo).toByte()
        }
        return out
    }

    private fun outer(type: OuterMessage.MessageType): OuterMessage.Builder =
        OuterMessage.newBuilder()
            .setProtocolVersion(2)
            .setStatus(OuterMessage.Status.STATUS_OK)
            .setType(type)

    private fun send(m: OuterMessage.Builder) {
        m.build().writeDelimitedTo(output)
        output.flush()
    }

    private fun read(): OuterMessage =
        OuterMessage.parseDelimitedFrom(input) ?: error("Connection closed by TV")

    private fun expectOk(): OuterMessage {
        val m = read()
        if (m.status != OuterMessage.Status.STATUS_OK) {
            error("Pairing rejected by TV (status ${m.status})")
        }
        return m
    }

    fun close() {
        try {
            socket?.close()
        } catch (_: Exception) {
        }
    }
}

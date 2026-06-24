package com.salem.tclremote

import android.content.Context
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.File
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.Date
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Owns the persistent client identity (RSA key pair + self-signed certificate).
 *
 * The SAME certificate must be presented during pairing and during every later control
 * connection — that is how the TV recognises this app as an already-paired client.
 */
object CertStore {
    private const val KS_FILE = "client.p12"
    private const val ALIAS = "client"
    private val PASS = "tclremote".toCharArray()

    @Volatile
    private var keyStore: KeyStore? = null

    @Synchronized
    fun load(context: Context): KeyStore {
        keyStore?.let { return it }
        val file = File(context.filesDir, KS_FILE)
        val ks = KeyStore.getInstance("PKCS12")
        if (file.exists()) {
            file.inputStream().use { ks.load(it, PASS) }
        } else {
            ks.load(null, null)
            val kp = generateKeyPair()
            val cert = generateCertificate(kp)
            ks.setKeyEntry(ALIAS, kp.private, PASS, arrayOf(cert))
            file.outputStream().use { ks.store(it, PASS) }
        }
        keyStore = ks
        return ks
    }

    fun clientCertificate(context: Context): X509Certificate =
        load(context).getCertificate(ALIAS) as X509Certificate

    private fun generateKeyPair(): KeyPair {
        val kpg = KeyPairGenerator.getInstance("RSA")
        kpg.initialize(2048)
        return kpg.generateKeyPair()
    }

    private fun generateCertificate(kp: KeyPair): X509Certificate {
        // Use a fresh BouncyCastle provider instance to avoid clashing with the
        // stripped-down "BC" provider that ships inside Android.
        val bc = BouncyCastleProvider()
        val now = System.currentTimeMillis()
        val notBefore = Date(now - 24L * 60 * 60 * 1000)
        val notAfter = Date(now + 25L * 365 * 24 * 60 * 60 * 1000)
        val name = X500Name("CN=TCL Remote, O=salem")
        val builder = JcaX509v3CertificateBuilder(
            name, BigInteger.valueOf(now), notBefore, notAfter, name, kp.public
        )
        val signer = JcaContentSignerBuilder("SHA256withRSA").setProvider(bc).build(kp.private)
        return JcaX509CertificateConverter().setProvider(bc).getCertificate(builder.build(signer))
    }

    /**
     * SSLContext that presents our client certificate and trusts any server certificate
     * (the TV uses a self-signed cert; we authenticate it cryptographically during pairing).
     */
    fun sslContext(context: Context): SSLContext {
        val ks = load(context)
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(ks, PASS)

        val trustAll = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
        })

        val ctx = SSLContext.getInstance("TLS")
        ctx.init(kmf.keyManagers, trustAll, SecureRandom())
        return ctx
    }
}

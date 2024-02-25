package com.owncloud.android.lib.common.http

import android.content.Context
import android.security.KeyChain
import android.security.KeyChainException
import java.lang.RuntimeException
import java.lang.UnsupportedOperationException
import java.net.Socket
import java.security.NoSuchAlgorithmException
import java.security.Principal
import java.security.PrivateKey
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.KeyManager
import javax.net.ssl.SSLContext
import javax.net.ssl.X509KeyManager

class X509Impl(
    private val alias: String,
    private val certChain: Array<X509Certificate>,
    private val privateKey: PrivateKey
) : X509KeyManager {
    override fun chooseClientAlias(
        arg0: Array<String>,
        arg1: Array<Principal>,
        arg2: Socket
    ): String {
        return alias
    }

    override fun getCertificateChain(alias: String): Array<X509Certificate> {
        return if (this.alias == alias) certChain else emptyArray()
    }

    override fun getPrivateKey(alias: String): PrivateKey? {
        return if (this.alias == alias) privateKey else null
    }

    // Methods unused (for client SSLSocket callbacks)
    override fun chooseServerAlias(
        keyType: String,
        issuers: Array<Principal>,
        socket: Socket
    ): String {
        throw UnsupportedOperationException()
    }

    override fun getClientAliases(keyType: String, issuers: Array<Principal>): Array<String> {
        throw UnsupportedOperationException()
    }

    override fun getServerAliases(keyType: String, issuers: Array<Principal>): Array<String> {
        throw UnsupportedOperationException()
    }

    companion object {
        fun setForConnection(
            con: HttpsURLConnection,
            context: Context?,
            alias: String
        ): SSLContext {
            var sslContext: SSLContext? = null
            sslContext = try {
                SSLContext.getInstance("TLS")
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException("Should not happen...", e)
            }
            sslContext!!.init(arrayOf<KeyManager>(fromAlias(context, alias)), null, null)
            con.sslSocketFactory = sslContext.getSocketFactory()
            return sslContext
        }

        fun fromAlias(context: Context?, alias: String): X509Impl {
            val certChain: Array<X509Certificate>?
            val privateKey: PrivateKey?
            try {
                certChain = KeyChain.getCertificateChain(context!!, alias)
                privateKey = KeyChain.getPrivateKey(context, alias)
            } catch (e: KeyChainException) {
                throw CertificateException(e)
            } catch (e: InterruptedException) {
                throw CertificateException(e)
            }
            if (certChain == null || privateKey == null) {
                throw CertificateException("Can't access certificate from keystore")
            }
            return X509Impl(alias, certChain, privateKey)
        }
    }
}

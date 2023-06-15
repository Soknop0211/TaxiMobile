package com.eazy.daiku.data.signature

import android.content.Context
import android.text.TextUtils
import android.util.Base64
import com.google.gson.JsonObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.security.*
import java.security.Signature
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher

object RSA2 {

    @Throws(Exception::class)
    fun sign(context: Context, plainText: String): String? {
        // load the private key
        val publicKey: RSAPublicKey = readPublicKey(context)
        val publicSignature: Signature = Signature.getInstance("SHA256withRSA")

        //  publicSignature.initSign(publicKey)
        publicSignature.initVerify(publicKey)

        publicSignature.update(plainText.toByte())
        val signature: ByteArray = publicSignature.sign()
        return Base64.encodeToString(signature, Base64.DEFAULT)
    }

    fun encryptByPublicKey(context: Context, data: ByteArray): String? {
        val publicKey: RSAPublicKey = readPublicKey(context)
        val cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return Base64.encodeToString(cipher.doFinal(data), 0)
    }

    @Throws(Exception::class)
    private fun readPrivateKey(context: Context): RSAPrivateKey {
        val inputStream: InputStream =
            context.assets.open("clientCU2203-27605616881044253.key")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line: String?
        while (bufferedReader.readLine().also { line = it } != null) {
            stringBuilder.append(line)
        }
        val privateKeyPEM = stringBuilder.toString().replace("-----BEGIN PRIVATE KEY-----", "")
            .replace(System.lineSeparator().toRegex(), "").replace("-----END PRIVATE KEY-----", "")
        val encoded: ByteArray = Base64.decode(privateKeyPEM, Base64.DEFAULT)
        val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
        val keySpec = PKCS8EncodedKeySpec(encoded)
        return keyFactory.generatePrivate(keySpec) as RSAPrivateKey
    }

    @Throws(Exception::class)
    private fun readPublicKey(context: Context): RSAPublicKey {
        val inputStream: InputStream =
            context.assets.open("clientCU2203-27605616881044253.pub")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line: String?
        while (bufferedReader.readLine().also { line = it } != null) {
            stringBuilder.append(line)
        }
        val publicKeyPEM = stringBuilder.toString().replace("-----BEGIN PUBLIC KEY-----", "")
            .replace(System.lineSeparator().toRegex(), "").replace("-----END PUBLIC KEY-----", "")
        val encoded: ByteArray = Base64.decode(publicKeyPEM, Base64.DEFAULT)
        val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
        val keySpec = X509EncodedKeySpec(encoded)
        return keyFactory.generatePublic(keySpec) as RSAPublicKey
    }

    private fun getKSort(jsonObject: JsonObject): MutableList<String> {
        val keySort: MutableList<String> = ArrayList()
        for ((key) in jsonObject.entrySet()) {
            keySort.add(key)
        }

        // 1 o1 > o2
        // -1 o1 < o2
        // 0 o1 equal 02
        // System.out.println("o1.compareTo(o2): " + o1 + "|" + o2 + " result: " + o1.compareTo(o2));
        Collections.sort(keySort, String::compareTo)
        return keySort
    }

    fun makeKeySort(body: JsonObject): String {
        val keys = getKSort(body)
        val stringBuilder = StringBuilder()
        for (key in keys) {
            val elm = body[key]
            if (key == "sign" || elm.isJsonArray || elm.isJsonNull) {
                continue
            }
            if (elm.isJsonObject) {
                continue
            }
            if (elm.isJsonPrimitive) {
                val jsonPrimitive = elm.asJsonPrimitive
                if (jsonPrimitive.isBoolean && !jsonPrimitive.asBoolean) {
                    continue
                }
                if (jsonPrimitive.isNumber && jsonPrimitive.asNumber.toDouble() == 0.0) {
                    continue
                }
                if (jsonPrimitive.isString && TextUtils.isEmpty(jsonPrimitive.asString)) {
                    continue
                }
            }
            println(" => $key")
            stringBuilder.append(key).append("=")
                .append(elm.asJsonPrimitive.asString.trim { it <= ' ' })
            val idx = keys.indexOf(key)
            val isAppendAmp = idx != keys.size - 1
            if (isAppendAmp) {
                stringBuilder.append("&")
            }
        }
        //Todo fix string
        //To remove double quotes just from the beginning and end of the String
        return stringBuilder.toString().replace("^\"|\"$".toRegex(), "")
    }

}
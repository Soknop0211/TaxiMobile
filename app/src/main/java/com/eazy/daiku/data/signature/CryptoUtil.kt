package com.eazy.daiku.data.signature
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

class CryptoUtil {

    private val CRYPTO_METHOD = "RSA"
    private val CRYPTO_BITS = 2048

    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )
    fun generateKeyPair(): Map<String, String> {
        val kpg: KeyPairGenerator = KeyPairGenerator.getInstance(CRYPTO_METHOD)
        kpg.initialize(CRYPTO_BITS)
        val kp: KeyPair = kpg.genKeyPair()
        val publicKey: PublicKey = kp.public
        val privateKey: PrivateKey = kp.private
        val map: MutableMap<String, String> = HashMap<String, String>()
        map["privateKey"] = Base64.encodeToString(privateKey.encoded, Base64.DEFAULT)
        map["publicKey"] = Base64.encodeToString(publicKey.encoded, Base64.DEFAULT)
        return map
    }

    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class,
        InvalidKeySpecException::class
    )
    fun encrypt(plain: String, privk: String): String? {
        val cipher: Cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, stringToPrivateKey(privk))
        val encryptedBytes: ByteArray = cipher.doFinal(plain.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    @Throws(
        NoSuchPaddingException::class,
        NoSuchAlgorithmException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        InvalidKeySpecException::class,
        InvalidKeyException::class
    )
    fun decrypt(result: String?, pubk: String): String {
        val cipher: Cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding")
        cipher.init(Cipher.DECRYPT_MODE, stringToPublicKey(pubk))
        val decryptedBytes: ByteArray = cipher.doFinal(Base64.decode(result, Base64.DEFAULT))
        return String(decryptedBytes)
    }

    @Throws(InvalidKeySpecException::class, NoSuchAlgorithmException::class)
    private fun stringToPublicKey(publicKeyString: String): PublicKey? {
        val keyBytes: ByteArray = Base64.decode(publicKeyString, Base64.DEFAULT)
        val spec = X509EncodedKeySpec(keyBytes)
        val keyFactory: KeyFactory = KeyFactory.getInstance(CRYPTO_METHOD)
        return keyFactory.generatePublic(spec)
    }

    @Throws(InvalidKeySpecException::class, NoSuchAlgorithmException::class)
    private fun stringToPrivateKey(privateKeyString: String): PrivateKey? {
        val pkcs8EncodedBytes: ByteArray = Base64.decode(privateKeyString, Base64.DEFAULT)
        val keySpec = PKCS8EncodedKeySpec(pkcs8EncodedBytes)
        val kf: KeyFactory = KeyFactory.getInstance(CRYPTO_METHOD)
        return kf.generatePrivate(keySpec)
    }

}
package com.ciscowebex.androidsdk.kitchensink.utils

import com.nimbusds.jose.EncryptionMethod
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWEHeader
import com.nimbusds.jose.JWEObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.DirectDecrypter
import com.nimbusds.jose.crypto.DirectEncrypter

/**
 * @param payload : The payload to encrypt, can be any string.
 * @param encryptionKey : Symmetric encryption key to ecrypt the payload. Use 256 bit symmetric key for AES256GCM encryption algo
 * Dummy key for example usage: "@McQfTjWnZr4u7x!A%D*G-KaNdRgUkXp"
 */
fun encryptPushRESTPayload(payload: String, encryptionKey: String = Constants.Keys.PushRestEncryptionKey): String {
    // Create the header
    val header = JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A256GCM)

    val keyBA = encryptionKey.toByteArray()
    // Create the JWE object and encrypt it
    val jweObject = JWEObject(header, Payload(payload))
    jweObject.encrypt(DirectEncrypter(keyBA))

    // Serialise to compact JOSE form...
    return jweObject.serialize()
}

/**
 * @param payload : JWE format string (https://tools.ietf.org/html/rfc7516)
 * @param decryptionKey : AES 256 bit symmetric key. This is the same encryption key as was used to ecrypt the payload,
 * As we use dir algorithm, so both encryption/decryption keys are same here.
 */
fun decryptPushRESTPayload(payload: String, decryptionKey: String = Constants.Keys.PushRestEncryptionKey): String {
    val jweObject = JWEObject.parse(payload)
    jweObject.decrypt(DirectDecrypter(decryptionKey.toByteArray()))
    val decryptedPayload = jweObject.payload
    return decryptedPayload.toString()
}
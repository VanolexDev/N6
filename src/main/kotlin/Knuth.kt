package net.vanolex

val b64Charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
val b64Lookup = b64Charset.withIndex().associate { it.value to it.index }

fun idToB64URL(id: Int): String {
    val stepped = (id* config.prime) and 0xFFFFFF

    val salted = stepped xor config.salt

    val i1 = salted and 0x3F
    val i2 = (salted shr 6) and 0x3F
    val i3 = (salted shr 12) and 0x3F
    val i4 = (salted shr 18) and 0x3F

    return "" + b64Charset[i1] + b64Charset[i2] + b64Charset[i3] + b64Charset[i4]
}

fun b64URLToId(b64: String): Int {
    val v1 = b64Lookup[b64[0]] ?: 0
    val v2 = b64Lookup[b64[1]] ?: 0
    val v3 = b64Lookup[b64[2]] ?: 0
    val v4 = b64Lookup[b64[3]] ?: 0

    val salted = v1 or (v2 shl 6) or (v3 shl 12) or (v4 shl 18)

    val stepped = salted xor config.salt

    return (stepped * config.inverse) and 0xFFFFFF
}

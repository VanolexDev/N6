package net.vanolex

import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import java.io.ByteArrayOutputStream

fun generateQrCode(url: String): ByteArray {
    val qrWriter = QRCodeWriter()
    val bitMatrix = qrWriter.encode(url, BarcodeFormat.QR_CODE, 29, 29)
    val outputStream = ByteArrayOutputStream()
    MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream)
    return outputStream.toByteArray()
}

suspend fun processCode(call: ApplicationCall, code: String) {
    if (!isValidCode(code)) {
        call.respondRedirect("./")
        return
    }

    val url = "http://n6.lt/$code"

    val qrCodeBytes = qrMap.getOrPut(code) {
        generateQrCode(url)
    }

    call.respondBytes(qrCodeBytes, ContentType.Image.PNG)
}

package com.example.paddupushtakam

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.paddupushtakam.data.TransactionEntity
import com.example.paddupushtakam.data.TransactionType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import android.graphics.Bitmap

object ReportGenerator {
    private val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    fun generateCSV(context: Context, transactions: List<TransactionEntity>): File {
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val file = File(reportsDir, "Paddu_Pushtakam_Safe_Report.csv")
        FileOutputStream(file).bufferedWriter().use { writer ->
            writer.write("*** VERIFIED SAFE: Generated locally by Paddu Pushtakam (No Macros/Scripts) ***\n")
            writer.write("Date,Description,Category,Type,Amount,Payment Mode,Entered By\n")
            transactions.forEach { t ->
                val dateStr = dateFormat.format(Date(t.timestamp)).replace(",", "")
                val desc = (t.description ?: "").replace(",", " ")
                val cat = t.category.replace(",", " ")
                val type = if (t.type == TransactionType.IN) "IN" else "OUT"
                val amount = t.amount
                val mode = t.paymentMode
                val by = (t.enteredBy ?: "").replace(",", " ")
                writer.write("$dateStr,$desc,$cat,$type,$amount,$mode,$by\n")
            }
        }
        return file
    }

    fun generatePDF(context: Context, transactions: List<TransactionEntity>): File {
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val file = File(reportsDir, "Paddu_Pushtakam_Safe_Report.pdf")
        val pdfDocument = PdfDocument()
        
        val paint = Paint()
        paint.color = Color.BLACK
        paint.textSize = 12f
        
        val titlePaint = Paint()
        titlePaint.color = Color.BLACK
        titlePaint.textSize = 18f
        titlePaint.isFakeBoldText = true

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        var yPosition = 50f
        canvas.drawText("Cashbook Report", 220f, yPosition, titlePaint)
        
        // Trust Stamp
        val trustPaint = Paint()
        trustPaint.color = Color.parseColor("#2E7D32")
        trustPaint.textSize = 10f
        trustPaint.isFakeBoldText = true
        canvas.drawText("✔ VERIFIED SAFE", 460f, 30f, trustPaint)
        canvas.drawText("Locally Generated", 460f, 45f, trustPaint)

        yPosition += 40f

        // Headers
        paint.isFakeBoldText = true
        canvas.drawText("Date", 30f, yPosition, paint)
        canvas.drawText("Description", 110f, yPosition, paint)
        canvas.drawText("Category", 230f, yPosition, paint)
        canvas.drawText("By", 330f, yPosition, paint)
        canvas.drawText("IN", 420f, yPosition, paint)
        canvas.drawText("OUT", 480f, yPosition, paint)
        yPosition += 20f
        
        canvas.drawLine(30f, yPosition - 15f, 565f, yPosition - 15f, paint)
        
        paint.isFakeBoldText = false

        for (t in transactions) {
            if (yPosition > 800f) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 50f
            }

            val dateStr = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(t.timestamp))
            val desc = (t.description ?: "").take(12)
            val cat = t.category.take(10)
            val by = (t.enteredBy ?: "").take(10)
            
            canvas.drawText(dateStr, 30f, yPosition, paint)
            canvas.drawText(desc, 110f, yPosition, paint)
            canvas.drawText(cat, 230f, yPosition, paint)
            canvas.drawText(by, 330f, yPosition, paint)
            
            if (t.type == TransactionType.IN) {
                canvas.drawText(t.amount.toString(), 420f, yPosition, paint)
            } else {
                canvas.drawText(t.amount.toString(), 480f, yPosition, paint)
            }
            
            yPosition += 25f
        }

        pdfDocument.finishPage(page)

        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return file
    }

    fun getAppIconUri(context: Context): Uri {
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val file = File(reportsDir, "app_icon_v2.png")
        if (!file.exists()) {
            val drawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
            if (drawable != null) {
                val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 256
                val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 256
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            }
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}

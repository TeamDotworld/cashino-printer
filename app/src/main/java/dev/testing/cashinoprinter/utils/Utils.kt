package dev.testing.cashinoprinter.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import com.izettle.html2bitmap.Html2Bitmap
import com.izettle.html2bitmap.content.WebViewContent.html
import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.runtime.RuntimeServices
import org.apache.velocity.runtime.RuntimeSingleton
import org.apache.velocity.runtime.parser.node.SimpleNode
import java.io.StringReader
import java.io.StringWriter
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean


/**
 * @Author: Naveen Sakthivel
 * @Date: 01-09-2022
 * Copyright (c) 2022 Dotworld Technologies. All rights reserved.
 */
private const val TAG = "Utils"


fun generateTemplate(context: Context, data: Map<String, Any>): String? {
    try {

        val con = "<p><span style=\"font-size:24px\"><img alt=\"\" src=\"file:///android_res/drawable/ccmc.png\" style=\" width:250px\" /></span></p>\n" +
                "    \n" +
                "    <p><span style=\"font-size:24px\">Date : \${date}</span></p>\n" +
                "    \n" +
                "    <p><span style=\"font-size:24px\">Payment Mode: \${mode}</span></p>\n" +
                "    \n" +
                "    <p><span style=\"font-size:28px\">We have received your payment of ₹\${amount} for account number \${customerId}.</span></p>\n" +
                "    \n" +
                "    <p><span style=\"font-size:28px\">Your transaction number is \${transactionId}.</span></p>\n" +
                "    \n" +
                "    <p>#if( \$advance > 0 )</p>\n" +
                "    \n" +
                "    <p><span style=\"font-size:28px\">We have kept advance of ₹\${advance} in your CCMC account.</span></p>\n" +
                "    \n" +
                "    <p>#end</p>\n" +
                "    \n" +
                "    <p><span style=\"font-size:24px\">Kindly contact CCMC incase of any clarifications.</span>" +
                "    <center><p><span style=\"font-size:24px;text-align=center\"><b>powered by dotworld</b></span></center>" +
                "</p>\n\n\n"

        val con1 = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Sample HTML Template</title>\n" +
                "    <style>\n" +
                "        /* Add your CSS styles here */\n" +
                "        body {\n" +
                "            font-family: Arial, sans-serif;\n" +
                "            line-height: 1.6;\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "        }\n" +
                "\n" +
                "        header {\n" +
                "            background-color: #fff;\n" +
                "            color: #000;\n" +
                "            padding: 1rem;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "\n" +
                "        nav {\n" +
                "            background-color: #444;\n" +
                "            color: #fff;\n" +
                "            display: flex;\n" +
                "            justify-content: center;\n" +
                "            padding: 0.5rem;\n" +
                "        }\n" +
                "\n" +
                "        nav a {\n" +
                "            color: #fff;\n" +
                "            text-decoration: none;\n" +
                "            margin: 0 1rem;\n" +
                "        }\n" +
                "\n" +
                "        section {\n" +
                "            padding: 2rem;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "\n" +
                "        footer {\n" +
                "            background-color: #333;\n" +
                "            color: #fff;\n" +
                "            padding: 1rem;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <header>\n" +
                "        <h1>Sample Website</h1>\n" +
                "    </header>\n" +
                "\n" +
                "    <nav>\n" +
                "        <a href=\"#\">Home</a>\n" +
                "        <a href=\"#\">About</a>\n" +
                "        <a href=\"#\">Contact</a>\n" +
                "    </nav>\n" +
                "\n" +
                "    <section>\n" +
                "        <h2>Welcome to our Sample </h2>\n" +
                "        <p>\n" +
                "            This is a simple HTML template to help you get started with building your own bill .\n" +
                "        </p>\n" +
                "    </section>\n" +
                "\n" +
                "    <footer>\n" +
                "        <p>&copy; 2023 Your Name. All rights reserved.</p>\n" +
                "    </footer>\n" +
                "</body>\n" +
                "</html>"
//        val content = context.assets.open("template.vm").bufferedReader().use { it.readText() }
//        Log.d(TAG, "generateTemplate: content = $content")
        val rs: RuntimeServices = RuntimeSingleton.getRuntimeServices()
        val sr = StringReader(con1)
        val sn: SimpleNode = rs.parse(sr, "print")

        val t = Template()
        t.setRuntimeServices(rs)
        t.data = sn
        t.initDocument()

        val vc = VelocityContext()

        data.forEach {
            vc.put(it.key, it.value)
        }

        val sw = StringWriter()
        t.merge(vc, sw)

        return sw.toString()
    } catch (e: Exception) {
        Log.e(TAG, "Error creating velocity Template", e)
    }
    return null
}

fun generateImage(context: Context, data: Map<String, Any>): Bitmap? {
    Log.d(TAG, "generateImage: start")
    val content = generateTemplate(context, data)
    Log.d(TAG, "Content $content")
    return Html2Bitmap.Builder()
        .setContext(context)
        .setContent(html(content))
        .build()
        .bitmap
}

fun getBitmapFromView(view: View): Bitmap? {
    try {
        val returnedBitmap =
            Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable: Drawable = view.getBackground()
        if (bgDrawable != null) bgDrawable.draw(canvas) else canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return returnedBitmap
    }catch (e:Exception){
        Log.d(TAG, "getBitmapFromView: $e")
        return null
    }
}


fun getBitmap(
    w: WebView,
    containerWidth: Int,
    containerHeight: Int,
    content: String
): Bitmap {
    val signal = CountDownLatch(1)
    val b: Bitmap = Bitmap.createBitmap(containerWidth, containerHeight, Bitmap.Config.ARGB_8888)
    val ready = AtomicBoolean(false)
    w.post {
        w.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                ready.set(true)
            }
        }
        w.setPictureListener { view, picture ->
            if (ready.get()) {
                val c = Canvas(b)
                view.draw(c)
                w.setPictureListener(null)
                signal.countDown()
            }
        }
        w.layout(0, 0, containerWidth, w.measuredHeight)
        w.loadData(content, "text/html", "UTF-8")
    }
    try {
        signal.await()
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }
    return b
}
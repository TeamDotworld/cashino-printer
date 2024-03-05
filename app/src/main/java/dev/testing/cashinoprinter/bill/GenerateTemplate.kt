package dev.testing.cashinoprinter.bill

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.izettle.html2bitmap.Html2Bitmap
import com.izettle.html2bitmap.content.WebViewContent
import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.runtime.RuntimeServices
import org.apache.velocity.runtime.RuntimeSingleton
import org.apache.velocity.runtime.parser.node.SimpleNode
import java.io.StringWriter

fun billTemplate(billData: BillData): String {
    with(billData) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "    <head>\n" +
                "        <title>My web page</title>\n" +
                "    </head>\n" +
                "    <body>\n" +
                "        <h1>Hello, world!</h1>\n" +
                "        <p>This is my first web page.</p>\n" +
                "        <p>It contains a \n" +
                "             <strong>main heading</strong> and <em> paragraph </em>.\n" +
                "        </p>\n" +
                "    </body>\n" +
                "</html>"
    }
}

data class BillData(
    var paid: String = "",
    var token: String = "",
    var date: String = "",
    var patientDetails: String = "",
    var docterName: String = "",
    var transId: String = "",
    var rrn: String = "",
    var billNo: String = "",
    var consultationFee: String = "",
    var totalFee: String = "",
    var generatedDate: String = ""
)


fun generateTemplate(data: BillData): String? {
    try {
        val rs: RuntimeServices = RuntimeSingleton.getRuntimeServices()
        val sr = billTemplate(data)
        val sn: SimpleNode = rs.parse(sr, "print")
        val t = Template()
        t.setRuntimeServices(rs)
        t.data = sn
        t.initDocument()
        val vc = VelocityContext()
        val sw = StringWriter()
        t.merge(vc, sw)
        return sw.toString()
    } catch (e: Exception) {
        Log.e("billTemplate", "Error creating velocity Template", e)
    }
    return null
}

fun generateImage(context: Context, data: BillData): Bitmap? {
    val content = generateTemplate(data)
    return Html2Bitmap.Builder().setContext(context).setContent(WebViewContent.html(content))
        .build().bitmap
}
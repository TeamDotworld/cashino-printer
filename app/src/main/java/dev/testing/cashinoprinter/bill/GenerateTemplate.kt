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
                "\n" +
                "<head>\n" +
                "    <title>HTML Content</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            font-family: Arial, sans-serif;\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "            box-sizing: border-box;\n" +
                "        }\n" +
                "\n" +
                "        header,\n" +
                "        main,\n" +
                "        footer {\n" +
                "            display: flex;\n" +
                "            flex-direction: column;\n" +
                "            flex-wrap: wrap;\n" +
                "            align-items: center;\n" +
                "            justify-items: center;\n" +
                "        }\n" +
                "\n" +
                "        p {\n" +
                "            text-align: center;\n" +
                "            font-size: 20px;\n" +
                "        }\n" +
                "\n" +
                "        header p {\n" +
                "            margin: 5px auto;\n" +
                "        }\n" +
                "\n" +
                "        h1 {\n" +
                "            margin: 40px auto 0px auto;\n" +
                "        }\n" +
                "\n" +
                "        h2 {\n" +
                "            margin: 0px auto;\n" +
                "        }\n" +
                "\n" +
                "        h3 {\n" +
                "            margin: 0px auto;\n" +
                "        }\n" +
                "\n" +
                "        main div p {\n" +
                "            margin: 1px;\n" +
                "        }\n" +
                "\n" +
                "        main div {\n" +
                "            width: 100%;\n" +
                "            margin: 10px auto;\n" +
                "        }\n" +
                "\n" +
                "        .fee-section__title {\n" +
                "            text-align: center;\n" +
                "            margin-bottom: 15px;\n" +
                "        }\n" +
                "\n" +
                "        .fee-section__item {\n" +
                "            display: flex;\n" +
                "            flex-direction: row;\n" +
                "            flex-wrap: nowrap;\n" +
                "            align-items: center;\n" +
                "            justify-content: space-between;\n" +
                "            width: 50%;\n" +
                "            margin: 0px auto;\n" +
                "        }\n" +
                "\n" +
                "        .fee-section div p {\n" +
                "            display: inline-block;\n" +
                "        }\n" +
                "\n" +
                "        footer p {\n" +
                "            margin: 5px auto;\n" +
                "        }\n" +
                "\n" +
                "        .barcode {\n" +
                "            width: 150px;\n" +
                "            height: 30px;\n" +
                "            background-color: black;\n" +
                "            margin: 5px auto;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "    <header>\n" +
                "        <h1>Appointment Slip ($paid)</h1>\n" +
                "        <h2>Sri Ramakrishna Hospital</h2>\n" +
                "        <p style=\"width: 60%;\">395, Sarojini Naidu Rd, New Siddhapudur, Coimbatore, Tamil Nadu 641044.</p>\n" +
                "    </header>\n" +
                "    <main>\n" +
                "        <div class=\"barcode\"></div>\n" +
                "        <div>\n" +
                "            <p><strong>Token $token</strong></p>\n" +
                "            <p>$date </p>\n" +
                "            <p>$patientDetails</p>\n" +
                "            <p>$docterName</p>\n" +
                "        </div>\n" +
                "        <div class=\"fee-section\">\n" +
                "            <div class=\"fee-section__title\"><strong>Payment Details</strong></div>\n" +
                "            <div class=\"fee-section__item\">\n" +
                "                <p class=\"fee-section\">Transaction Id</p>\n" +
                "                <p>$transId</p>\n" +
                "            </div>\n" +
                "            <div class=\"fee-section__item\">\n" +
                "                <p>RRN</p>\n" +
                "                <p>$rrn</p>\n" +
                "            </div>\n" +
                "            <div class=\"fee-section__item\">\n" +
                "                <p>Bill Number</p>\n" +
                "                <p>$billNo</p>\n" +
                "            </div>\n" +
                "\n" +
                "            <div class=\"fee-section__title\"><strong>Fee Details</strong></div>\n" +
                "            <div class=\"fee-section__item\">\n" +
                "                <p class=\"fee-section\">Consultation Fee</p>\n" +
                "                <p>$consultationFee</p>\n" +
                "            </div>\n" +
                "            <div class=\"fee-section__item\">\n" +
                "                <p><strong>Total Fee</strong></p>\n" +
                "                <p style=\"margin-top: 12px;\"><strong>$totalFee</strong></p>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "    </main>\n" +
                "    <footer>\n" +
                "        <div>\n" +
                "            <p>Generated by: Kiosk User</p>\n" +
                "            <p>Generated on $generatedDate</p>\n" +
                "        </div>\n" +
                "    </footer>\n" +
                "</body>\n" +
                "\n" +
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
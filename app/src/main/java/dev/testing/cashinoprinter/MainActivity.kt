package dev.testing.cashinoprinter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import dev.dotworld.test.usbprinter.PosUsbPrinter
import dev.dotworld.test.usbprinter.PosUsbPrinter.getConnectedDevices
import dev.dotworld.test.usbprinter.PosUsbPrinter.usbPrintOpen
import dev.testing.cashinoprinter.bill.BillData
import dev.testing.cashinoprinter.bill.generateImage
import dev.testing.cashinoprinter.databinding.ActivityMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view: View = binding!!.root
        setContentView(view)
        PosUsbPrinter.posSetup(this)
        binding.printerList.setOnClickListener {

            val device = getConnectedDevices()
            var deviceName: Array<String?> = arrayOfNulls(device.size)
            device.forEachIndexed { index, usbDevice ->
                usbDevice.productName.let { it1 -> deviceName[index] = it1 }
            }
            AlertDialog.Builder(this)
                .setTitle("Select Printer")
                .setItems(deviceName, DialogInterface.OnClickListener() { _, p ->
                    binding.printerId.text="${device[p]?.productId} / ${device[p]?.vendorId} \n ${device[p].productName}"
                    usbPrintOpen(this,mDevice = device[p])
                }).create().show()
        }
        binding.printerScan.setOnClickListener {
            val device=PosUsbPrinter.searchAndSelectUsbPost(this@MainActivity)
            Log.d(TAG, "onCreate: device =$device ")
            binding.printerId.text="${device?.productId} / ${device?.vendorId}"
        }
        binding.printerSetting.setOnClickListener {
            val device = PosUsbPrinter.usbDevice()
            binding.printerId.text="${device?.productId} / ${device?.vendorId}/n${device?.productName}"
        }
        binding.printOpen.setOnClickListener {
            PosUsbPrinter.usbPrintOpen(this)
        }
        binding.print.setOnClickListener {
            printReceipt(this)
        }
        binding.createBitmap.setOnClickListener {
            GlobalScope.launch {
                val b = createBitmap(
                    this@MainActivity,
                    BillData(
                        paid = "Not Paid",
                        token = "NKIOSKG3001",
                        date = "2023-08-11",
                        patientDetails = "Arun Kumar/25yrs/M, A123123",
                        docterName = "Dr.Aby Thomas",
                        transId = "8fi000w9w2",
                        rrn = "R72345",
                        billNo = "B8232323",
                        consultationFee = "400",
                        totalFee = "500",
                        generatedDate = "400"
                    )
                )
                if (b != null) {
                    runOnUiThread() {
                        binding.htmlToBitmap.setImageBitmap(b)
                    }
                }
            }
        }
        binding.printerClose.setOnClickListener {
            PosUsbPrinter.usbPrintClose()
        }

        // new pack

    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private fun printReceipt(activity: Activity) {
        Log.d(TAG, "printReceipt: start")
        val bitmap: Bitmap?
        GlobalScope.launch {
            var bitmap: Bitmap? = createBitmap(
                this@MainActivity,
                BillData(
                    paid = "Not Paid",
                    token = "NKIOSKG3001",
                    date = "2023-08-11",
                    patientDetails = "Arun Kumar/25yrs/M, A123123",
                    docterName = "Dr.Aby Thomas",
                    transId = "8fi000w9w2",
                    rrn = "R72345",
                    billNo = "B8232323",
                    consultationFee = "400",
                    totalFee = "500",
                    generatedDate = "14-070-2023 11:30:00 am"
                )
            )

            if (bitmap != null) {
                runOnUiThread() {
                    binding.htmlToBitmap.setImageBitmap(bitmap)
                }
            }
            bitmap?.let {
                Log.d(TAG, "printReceipt.print: start")
                PosUsbPrinter.print(activity.applicationContext, bitmap,nPrintWidth = 600 )
            }
        }

    }

    private fun createBitmap(activity: Activity, billData: BillData): Bitmap? {

        return generateImage(activity.applicationContext, billData)

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        private const val TAG: String = "MainActivity"
        private lateinit var mUsbManager: UsbManager
        private var mDevice: UsbDevice? = null
        private lateinit var usbDeviceList: HashMap<String, UsbDevice>

        //new pack
        private var usbManager: UsbManager? = null
    }


}
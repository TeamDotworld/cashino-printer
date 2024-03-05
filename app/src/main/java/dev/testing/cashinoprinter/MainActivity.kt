package dev.testing.cashinoprinter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
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

    private lateinit var usbManager: UsbManager
    private lateinit var permissionIntent: PendingIntent

    override fun onResume() {
        super.onResume()
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(
                usbReceiver, IntentFilter(ACTION_USB_PERMISSION),
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            registerReceiver(usbReceiver, IntentFilter(ACTION_USB_PERMISSION))
        }*/
    }

    override fun onPause() {
        super.onPause()
        //unregisterReceiver(usbReceiver)
    }

 /*   private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (ACTION_USB_PERMISSION == action) {
                synchronized(this) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        // Permission granted, you can now access the USB device
                        Toast.makeText(context, "Usb Permission granted", Toast.LENGTH_SHORT).show()
                    } else {
                        // Permission denied, handle accordingly
                        Toast.makeText(context, "Usb Permission denied", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }*/

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view: View = binding!!.root
        setContentView(view)
        PosUsbPrinter.posSetup(this)
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        /*permissionIntent = PendingIntent.getBroadcast(
            this, 0, Intent(ACTION_USB_PERMISSION),
            PendingIntent.FLAG_IMMUTABLE
        )*/
        binding.printerScan.setOnClickListener {
            val device = PosUsbPrinter.searchAndSelectUsbPost(this@MainActivity)
            Log.d(TAG, "onCreate: device =$device ")
            binding.printerId.text = "pid = ${device?.productId} /vid = ${device?.vendorId} /class ${device?.deviceClass} / subclass ${device?.deviceSubclass} / protocol ${device?.deviceProtocol}"
        }
        binding.printerSetting.setOnClickListener {
            val device = PosUsbPrinter.usbDevice()
            binding.printerId.text =
                "${device?.productId} / ${device?.vendorId}/n${device?.productName}"
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
                PosUsbPrinter.print(activity.applicationContext, bitmap, nPrintWidth = 600)
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
        private const val ACTION_USB_PERMISSION = "com.example.USB_PERMISSION"

        //new pack
        private var usbManager: UsbManager? = null
    }


}
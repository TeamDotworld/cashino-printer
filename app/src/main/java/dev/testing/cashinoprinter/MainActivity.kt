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
import dev.testing.cashinoprinter.utils.generateImage
import dev.dotworld.test.usbprinter.PosUsbPrinter
import dev.dotworld.test.usbprinter.PosUsbPrinter.getConnectedDevices
import dev.dotworld.test.usbprinter.PosUsbPrinter.usbPrintOpen
import dev.testing.cashinoprinter.databinding.ActivityMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Date

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
            createBitmap(this)
        }
        binding.printerClose.setOnClickListener {
            PosUsbPrinter.usbPrintClose()
        }

        // new pack

    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private fun printReceipt(activity: Activity, data: Map<String, Any> = mapOf(
        "date" to Date().toLocaleString(),
        "mode" to 12,
        "amount" to 13000,
        "customerId" to "12D233",
        "transactionId" to "12DDDD3DQE",
        "advance" to "2000"
    )) {
        Log.d(TAG, "printReceipt: start")
        val bitmap: Bitmap?
        GlobalScope.launch {
            var bitmap: Bitmap? = generateImage(activity.applicationContext, data)
            /*val drawable = resources.getDrawable(R.drawable.image, null)
            val bitmapDrawable = drawable as BitmapDrawable
            val bitmap = bitmapDrawable.bitmap*/
            /*   try {
                   val b: InputStream = assets.open("S.png")
                   val bit: Bitmap = BitmapFactory.decodeStream(b)
                   bitmap = bit
               } catch (e1: IOException) {
                   // TODO Auto-generated catch block
                   e1.printStackTrace()
               }*/
            if (bitmap != null) {
                runOnUiThread() {
                    binding.htmlToBitmap.setImageBitmap(bitmap)
                }
            }
            bitmap?.let {
                Log.d(TAG, "printReceipt.print: start")
                PosUsbPrinter.print(activity.applicationContext, bitmap)
            }
        }

    }

    private fun createBitmap(activity: Activity){
        val data: Map<String, Any> = mapOf(
            "date" to Date().toLocaleString(),
            "mode" to 12,
            "amount" to 13000,
            "customerId" to "12D233",
            "transactionId" to "12DDDD3DQE",
            "advance" to "2000")
        var bitmap: Bitmap? = null
        Log.d(TAG, "createBitmap: bitmap1 = $bitmap")
        GlobalScope.launch{
             bitmap = generateImage(activity.applicationContext, data)
            Log.d(TAG, "createBitmap: bitmap2 = $bitmap")
            if (bitmap != null){
                Log.d(TAG, "createBitmap: bitmap size = ${(bitmap?.byteCount?.div(1024))}")
                runOnUiThread(){
                    binding.htmlToBitmap.setImageBitmap(bitmap)
                }
            }
        }


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
package dev.dotworld.usb.printer

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import dev.dotworld.test.usbprinter.PosUsbPrinter


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PosUsbPrinter.posSetup(this)
        initPrintDevices()
    }

    @SuppressLint("MutableImplicitPendingIntent")
    private fun initPrintDevices() {
        val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val printers = usbManager.deviceList.filter { isPrinter(it.value) }.map { it.value }
        printers.forEach {
            Log.i(TAG, "onCreate: usbDevice $it")
            val hasPermission = usbManager.hasPermission(it)
            if (!hasPermission) {
                Log.i(TAG, "onCreate: has permission for ${it.deviceName}")
                onPrintRequest(Bitmap.createBitmap(100, 50, Bitmap.Config.ARGB_8888), it)
            } else {
                val permissionIntent = PendingIntent.getBroadcast(
                    this, 0, Intent(actionUsbPermission),
                    PendingIntent.FLAG_MUTABLE
                )
                val filter = IntentFilter(actionUsbPermission)
                ContextCompat.registerReceiver(
                    this,
                    usbReceiver,
                    filter,
                    ContextCompat.RECEIVER_NOT_EXPORTED
                )
                usbManager.requestPermission(it, permissionIntent)
            }
        }
    }

    private fun onPrintRequest(bitmap: Bitmap, device: UsbDevice) {
        PosUsbPrinter.usbPrintOpen(this@MainActivity, device)
        Handler(Looper.getMainLooper()).postDelayed({
            Log.i(TAG, "onCreate: printing")
            PosUsbPrinter.print(
                this@MainActivity, bitmap
            )
        }, 3000)
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (actionUsbPermission == intent.action) {
                synchronized(this) {
                    val device: UsbDevice? =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(
                                UsbManager.EXTRA_DEVICE,
                                UsbDevice::class.java
                            )
                        } else {
                            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                        }
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.apply {
                            // call method to set up device communication
                            Log.i(TAG, "onReceive: permission granted for device $device $")
                            onPrintRequest(
                                Bitmap.createBitmap(100, 50, Bitmap.Config.ARGB_8888),
                                this
                            )
                        }
                    } else {
                        Log.d(TAG, "permission denied for device $device")
                    }
                }
            }
        }
    }

    private fun isPrinter(usbDevice: UsbDevice): Boolean {
        return usbDevice.getConfiguration(0)
            .getInterface(0).interfaceClass == UsbConstants.USB_CLASS_PRINTER
    }

    private val actionUsbPermission = "dev.dotworld.usb.printer.USB_PERMISSION"

    companion object {
        private const val TAG = "MainActivity"
    }
}
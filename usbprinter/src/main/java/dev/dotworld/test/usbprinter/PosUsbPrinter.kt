package dev.dotworld.test.usbprinter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import androidx.core.content.getSystemService
import com.lvrenyang.io.Pos
import com.lvrenyang.io.USBPrinting
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object PosUsbPrinter {

    private val TAG: String = "PosUsbPrinter"
    private val executorService: ExecutorService = Executors.newScheduledThreadPool(30)
    private var pos = Pos()

    @SuppressLint("StaticFieldLeak")
    private val usbPrinting = USBPrinting()
    private lateinit var mUsbManager: UsbManager
    private var mDevice: UsbDevice? = null

    fun posSetup(context: Context) {
        pos.Set(usbPrinting)
        usbPrinting.SetCallBack(context.getSystemService())
        mUsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    }

    var currentUsbDevice: UsbDevice?
        get() = mDevice
        set(value) {
            mDevice = value
        }

    fun usbPrintOpen(context: Context, mDevice: UsbDevice) {
        executorService.submit(
            TaskOpen(
                usbPrinting,
                mUsbManager,
                mDevice,
                context
            )
        )
    }

    fun usbPrintClose() {
        Log.d(TAG, "usbPrintClose: Usb Print Close")
        executorService.submit(
            TaskClose(
                usbPrinting
            )
        )
    }

    fun print(
        c: Context,
        bitmap: Bitmap,
        data: String? = null,
        printWidth: Int = 384,
        cutPaper: Boolean = true,
        isHalfCutPaper: Boolean = true,
        drawer: Boolean = false,
        beeper: Boolean = true,
        printCount: Int = 1,
        printContent: Int = 1,
        compressMethod: Int = 0
    ) {
        executorService.submit(
            TaskPrint(
                pos,
                c,
                printWidth,
                cutPaper,
                isHalfCutPaper,
                drawer,
                beeper,
                printCount,
                printContent,
                compressMethod,
                data,
                bitmap
            )
        )
    }

    class TaskOpen(
        private val usb: USBPrinting?,
        private val usbManager: UsbManager?,
        private val usbDevice: UsbDevice?,
        private val context: Context?
    ) : Runnable {
        override fun run() {

            Log.d(TAG, "TaskOpen run: ${usbDevice?.deviceName} / ${usb?.IsOpened()} / ")

            usb?.Open(usbManager, usbDevice, context) ?: Log.d(
                TAG,
                "TaskOpen run: usb disconnected"
            )
            if (usb?.IsOpened() == true) {
                Log.d(TAG, "TaskOpen run: usb print is open")
            } else {
                Log.d(TAG, "TaskOpen run: usb print is not open")
            }
        }

    }

    class TaskClose(private val usb: USBPrinting?) : Runnable {
        override fun run() {
            usb?.Close() ?: Log.d(TAG, "TaskClose run: usb printer dis connected")
            usb?.SkipAvailable()
        }
    }

    class TaskPrint(
        private val pos: Pos?, val c: Context,
        private val printWidth: Int = 384,
        private val paperCutter: Boolean = true,
        private val isHalfCutPaper: Boolean = true,
        private val drawer: Boolean = false,
        private val beeper: Boolean = true,
        private val printCount: Int = 1,
        private val printContent: Int = 1,
        private val compressMethod: Int = 0,
        private val data: String? = null,
        private val bitmap: Bitmap? = null
    ) : Runnable {
        override fun run() {
            val bPrintResult: Int = Prints.PrintTicket(
                c,
                pos,
                printWidth,
                paperCutter,
                isHalfCutPaper,
                drawer,
                beeper,
                printCount,
                printContent,
                compressMethod,
                data,
                bitmap
            )
            val bIsOpened = pos!!.GetIO().IsOpened()
            Log.d(
                TAG,
                "run: PrintResult = ($bPrintResult)  / $bIsOpened"
            )
        }
    }

    class TaskSkip(private val usb: USBPrinting?) : Runnable {
        override fun run() {
            usb?.SkipAvailable() ?: Log.d(TAG, "TaskSkip run: usb disconnected")
            Log.d(TAG, "TaskSkip : ${usb?.IsOpened()}")
            usb?.Close()
        }
    }
}
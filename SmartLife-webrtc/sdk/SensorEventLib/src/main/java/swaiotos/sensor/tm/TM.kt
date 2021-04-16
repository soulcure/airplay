package swaiotos.sensor.tm

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import java.util.concurrent.Executors

/**
 * @Author: yuzhan
 */
class TM {

    companion object {
        private lateinit var singleThread: HandlerThread
        private lateinit var ioHandler: Handler
        private val executor = Executors.newCachedThreadPool { r -> Thread(r, "SJ-IO-") }

        private val uiHandler = Handler(Looper.getMainLooper())

        init {
            singleThread = HandlerThread("SJ-Single-IO")
            singleThread.start()
            ioHandler = Handler(singleThread.looper)
        }

        fun ioSingle(r: Runnable) {
            ioHandler.post(r)
        }

        fun io(r: Runnable, delayInMs: Long) {
            ioHandler.postDelayed(r, delayInMs)
        }

        fun io(r: Runnable) {
            executor.execute(r)
        }

        fun removeIO(r: Runnable) {
            ioHandler.removeCallbacks(r)
        }

        fun ui(r: Runnable) {
            uiHandler.post(r)
        }

        fun ui(r: Runnable, delayInMs: Long) {
            uiHandler.postDelayed(r, delayInMs)
        }

        fun removeUI(r: Runnable) {
            uiHandler.removeCallbacks(r)
        }
    }
}
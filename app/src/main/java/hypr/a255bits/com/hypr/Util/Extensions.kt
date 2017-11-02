package hypr.a255bits.com.hypr.Util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.support.v4.view.ViewPager
import com.flurgle.camerakit.CameraListener
import com.flurgle.camerakit.CameraView
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream


fun Float.nonNegativeInt(): Int {
    return intArrayOf(this.toInt(), 0).max()!!
}

fun Int.negative1To1(): Double {
    return ((this - 100) / 100.00)
}

inline fun ViewPager.onPageSelected(crossinline listener: (position: Int) -> Unit) {
    addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {}
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
        override fun onPageSelected(position: Int) {
            listener(position)
        }

    })
}
inline fun CameraView.onPictureTaken(crossinline listener: (jpeg: ByteArray?) -> Unit){
   setCameraListener(object: CameraListener(){
       override fun onPictureTaken(jpeg: ByteArray?) {
           super.onPictureTaken(jpeg)
           listener(jpeg)
       }
   })
}

fun ByteArray.toBitmap(): Bitmap? {
    return BitmapFactory.decodeByteArray(this, 0, this.size)
}

fun Bitmap.toByteArray(): ByteArray{
    val stream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}
fun Bitmap.scaleBitmap(newWidth: Int, newHeight: Int): Bitmap {
        val width = this.width
        val height = this.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        val resizedBitmap = Bitmap.createBitmap(
                this, 0, 0, width, height, matrix, false)
        this.recycle()
        return resizedBitmap
    }

<<<<<<< HEAD
fun IntArray.toByteArrayImage(): ByteArray {
    val baos = ByteArrayOutputStream()
    val dos = DataOutputStream(baos)
    for (i in this.indices) {
        dos.writeInt(this[i])
=======
fun IntArray.toByteArrayImage(values: IntArray): ByteArray {
    val baos = ByteArrayOutputStream()
    val dos = DataOutputStream(baos)
    for (i in values.indices) {
        dos.writeInt(values[i])
>>>>>>> 7ace85673be25335e45780ed44bfdb3c4ecda23a
    }

    return baos.toByteArray()
}
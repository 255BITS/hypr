package hypr.hypergan.com.hypr.GeneratorLoader

import android.content.res.AssetManager
import android.graphics.Bitmap
import hypr.hypergan.com.hypr.Generator.Generator
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.experimental.GpuDelegate;
import java.nio.ByteBuffer
import java.nio.channels.FileChannel.MapMode.READ_ONLY
import android.content.res.AssetFileDescriptor
import java.io.FileInputStream
import java.io.IOException
import java.nio.channels.FileChannel


open class GeneratorLoader {

    lateinit var inference: Interpreter

    var generator: Generator? = null
    var channels: Int = 0
    var width: Int = 0
    var height: Int = 0
    var z_dimsArray: LongArray = longArrayOf()
    var z_dims: Long = 0
    var raw: FloatArray = floatArrayOf()
    var index: Int? = 0
    var assets: AssetManager? = null
    var bytes:ByteArray? = null
    var buffer:ByteBuffer? = null
    var delegate:GpuDelegate? = null
    var options: Interpreter.Options?

    init {
        this.delegate = GpuDelegate();
        this.options = Interpreter.Options().addDelegate(this.delegate)
    }
    fun setIndex(index: Int) {
        this.index = index
    }

    fun loadGenerator(generator: Generator) {
        this.generator = generator
        this.width = generator.generator?.output?.width!!
        this.height = generator.generator!!.output?.height!!
        channels = generator.generator!!.output!!.channels!!
        z_dimsArray = generator.generator!!.input!!.z_dims!!.map { item -> item.toLong() }.toLongArray()
        z_dims = z_dimsArray.fold(1.toLong(), { mul, next -> mul * next })
        raw = floatArrayOf()
        System.gc()

        raw = FloatArray(width * height * channels)

    }
    @Throws(IOException::class)
    private fun loadModelFile(assetManager: AssetManager, modelPath: String): ByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.getChannel()
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
    fun load() {
        val file = generator?.model_file
        this.buffer = loadModelFile(this.assets!!, "generators/"+file)//this.assets?.open( "generators/"+file)

        //this.bytes = asset!!.readBytes()

        //this.buffer = ByteBuffer.allocateDirect(this.bytes?.size!!)
        //this.buffer?.put(bytes)

        //this.buffer = asset!!.buffered(asset.available())

        this.inference = Interpreter( this.buffer!!, options )
        this.buffer?.clear()
    }

    fun sample(z: FloatArray, mask: FloatArray, bitmap: Bitmap): IntArray {
        print("Sampling ")

        var inputs:Array<Any> = arrayOf(z)
        var outputs:HashMap<Int, Any> = hashMapOf(0 to this.raw)

        if(!this::inference.isInitialized) {
            this.load()
        }
        this.inference.runForMultipleInputsOutputs(inputs, outputs)


        return manipulatePixelsInBitmap()
    }

    fun mask(bitmap: Bitmap): FloatArray {
        //feedInput(bitmap)
        val floatValues = FloatArray(width * height)
        return floatValues
    }

    fun sampleRandom(z: FloatArray, mask: FloatArray, scaled: Bitmap): IntArray {

        val dims = longArrayOf(1.toLong(), 1.toLong(), 1.toLong(), 1.toLong())

        var inputs:Array<Any> = arrayOf(z)
        var outputs:HashMap<Int, Any> = hashMapOf(0 to this.raw)
        if(!this::inference.isInitialized) {
            this.load()
        }

        this.inference.runForMultipleInputsOutputs(inputs, outputs)

        return manipulatePixelsInBitmap()
    }

    fun feedInput(bitmap: Bitmap) {
        val intValues = IntArray(width * height)
        val floatValues = FloatArray(width * height * channels)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        for (i in 0..intValues.size - 1) {
            val ival = intValues[i]
            floatValues[i * 3] = ((ival shr 16 and 0xFF) / 255.0f - 0.5f) * 2
            floatValues[i * 3 + 1] = ((ival shr 8 and 0xFF) / 255.0f - 0.5f) * 2
            floatValues[i * 3 + 2] = ((ival and 0xFF) / 255.0f - 0.5f) * 2
        }
        val dims = longArrayOf(1.toLong(), width.toLong(), height.toLong(), channels.toLong())
        if (index == 0) {

            //this.inference.feed("input", floatValues, *dims)
        }
    }

    fun encode(bitmap: Bitmap): FloatArray {
        //feedInput(bitmap)


        val z = FloatArray(z_dims.toInt())


        return z
    }

    fun random_z(): FloatArray {
        val z = FloatArray(z_dims.toInt())
        val r = java.util.Random()
        for (i in 0..z_dims.toInt() - 1)
            z[i] = r.nextFloat() * 2.0f - 1.0f
        return z
    }

    fun style_z(like:FloatArray):FloatArray {
        val z = FloatArray(z_dims.toInt())
        val r = java.util.Random()
        for (i in 0..z_dims.toInt() - 1) {
            //z[i] = 0.0f
            if (i >= z_dims.toInt() / 2) {
                z[i] = like[i]
            } else {
                z[i] = r.nextFloat() * 2.0f - 1.0f
            }
        }
        return z
    }

    private fun manipulatePixelsInBitmap(): IntArray {
        val pixelsInBitmap = IntArray(width * height)
        for (i in 0..pixelsInBitmap.size - 1) {

            val raw_one: Int = (((raw[i * 3] + 1) / 2.0 * 255).toInt()) shl 16
            val raw_two: Int = (((raw[i * 3 + 1] + 1) / 2.0 * 255).toInt()) shl 8
            val raw_three: Int = ((raw[i * 3 + 2] + 1) / 2.0 * 255).toInt()
            pixelsInBitmap[i] = 0xFF000000.toInt() or raw_one or raw_two or raw_three
        }
        return pixelsInBitmap
    }

}
package hypr.hypergan.com.hypr.ModelFragmnt

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import hypr.hypergan.com.hypr.Generator.Generator
import hypr.hypergan.com.hypr.GeneratorLoader.EasyGeneratorLoader
import hypr.hypergan.com.hypr.GeneratorLoader.FaceLocation
import hypr.hypergan.com.hypr.GeneratorLoader.Person
import hypr.hypergan.com.hypr.R
import hypr.hypergan.com.hypr.Util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import org.jetbrains.anko.toast
import java.io.File
import java.io.IOException
import kotlin.properties.Delegates

class ModelFragmentPresenter(val easyGenerator: EasyGeneratorLoader) : ModelFragmentMVP.presenter {

    val SHARE_IMAGE_PERMISSION_REQUEST = 10
    val SAVE_IMAGE_PERMISSION_REQUEST: Int = 11
    var generator: Generator by Delegates.observable(Generator()) { property, oldValue, newValue ->
        newValue.let { easyGenerator.loadGenerator(newValue) }
        newValue
    }
    var generatorIndex: Int? = null
    lateinit var person: Person
    lateinit var view: ModelFragmentMVP.view
    lateinit var interactor: ModelInteractor
    var generatorLaunch: Job? = null
    private var imageManipulatedFromzValue: Bitmap? = null
    var mutex:Mutex = Mutex()


    fun loadGenerator(context: Context, pbFile: File?) {
        generatorLaunch = GlobalScope.launch(Dispatchers.Main) {
            val imageBitmap = GlobalScope.async {
                val bitmap = person.fullImage.toBitmap()
                val faces = getFaceCroppedOutOfImageIfNoFaceGetFullImage(bitmap, context)
                val transformedImage: Bitmap? = sampleImage(person, faces, interactor.settings.getFaceLocation())
                return@async transformedImage
            }
            view.displayFocusedImage(imageBitmap.await())
        }
        generatorIndex?.let { easyGenerator.setIndex(it) }
    }

    fun setViews(view: ModelFragmentMVP.view) {
        this.view = view
    }

    fun setInteractors(interactor: ModelInteractor) {
        this.interactor = interactor
    }

    override fun disconnectFaceDetector() {
        interactor.faceDetection.release()
        generatorLaunch?.cancel()
    }

    override fun readImageToBytes(imagePath: String?): ByteArray? {
        return imagePath?.let { File(it).readBytes() }
    }

    fun shareImageToOtherApps() {
        if (interactor.checkIfPermissionGranted(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //val watermarkBitmap = interactor.placeWatermarkOnImage(view.displayedImageAsBitmap())
            val shareIntent = interactor.getIntentForSharingImagesWithOtherApps(view.displayedImageAsBitmap())
            view.shareImageToOtherApps(shareIntent)
        } else {
            view.requestPermissionFromUser(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), SHARE_IMAGE_PERMISSION_REQUEST)
        }
    }

    override fun randomizeModel(progress: Int) {
        easyGenerator.randomize()

        changeGanImageFromSlider(progress.negative1To1())
    }

    override fun getFaceCroppedOutOfImageIfNoFaceGetFullImage(imageWithFaces: Bitmap?, context: Context): Bitmap? {
        var image: Bitmap? = null
        try {
            image = if (imageWithFaces == null) {
                imageWithFaces
            } else {
                getCroppedFaceImagFromImageWithFaces(imageWithFaces, context)
            }
        } catch (exception: IOException) {
            Log.e("ModelFragment", exception.message)
        }
        return image
    }

    private fun getCroppedFaceImagFromImageWithFaces(imageWithFaces: Bitmap, context: Context): Bitmap? {
        val croppedFaces: MutableList<FaceLocation> = interactor.getFacesFromBitmap(imageWithFaces, imageWithFaces.width, imageWithFaces.height, context)
        val faceImage = if (isFacesDetected(croppedFaces)) {
            val faceIndex = interactor.settings.getFaceIndex()
            croppedFaces[faceIndex].croppedFace
        } else {
            imageWithFaces
        }
        return faceImage
    }

    fun isFacesDetected(listOfFaces: MutableList<FaceLocation>): Boolean {
        return !listOfFaces.isEmpty()
    }

    override fun saveImageDisplayedToPhone(context: Context): Boolean {
        var isSaved = false
        if (interactor.checkIfPermissionGranted(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            val faceImage = person.faceImage?.toBitmap()
            isSaved = ImageSaver().saveImageToInternalStorage(faceImage, context)

            //val waterMarkImage = interactor.placeWatermarkOnImage(view.displayedImageAsBitmap())
            //isSaved = ImageSaver().saveImageToInternalStorage(waterMarkImage, context)

        } else {
            view.requestPermissionFromUser(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), SAVE_IMAGE_PERMISSION_REQUEST)
        }
        return isSaved
    }

    fun inlineImage(person: Person, newCroppedImage: Bitmap): Bitmap? {
        val faceImage = person.faceImage?.toBitmap()
        val image: Bitmap? = if (faceImage != null) {
            easyGenerator.inlineImage(person, newCroppedImage, interactor.settings.getFaceLocation())
        } else {
            newCroppedImage
        }
        return image
    }

    override fun sampleImage(person: Person, image: Bitmap?, croppedPoint: Rect): Bitmap? {
        val transformedImage = if (image != null) {
            easyGenerator.sampleImageWithImage(person, image, croppedPoint)
        } else {
            easyGenerator.sampleImageWithoutImage().toBitmap(easyGenerator.width, easyGenerator.height)
        }
        return transformedImage
    }

    override fun changePixelToBitmap(transformedImage: IntArray): Bitmap? {
        return transformedImage.toBitmap(easyGenerator.width, easyGenerator.height)
    }

    override fun onRequestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray, context: Context) {
        grantResults.filter { item -> item == PackageManager.PERMISSION_GRANTED }.forEach { item ->
            if (requestCode == SHARE_IMAGE_PERMISSION_REQUEST) {
                shareImageToOtherApps()
            } else if (requestCode == SAVE_IMAGE_PERMISSION_REQUEST) {
                val coroutineContext = context
                GlobalScope.launch (Dispatchers.Main) {
                    GlobalScope.async { saveImageDisplayedToPhone(coroutineContext) }.await()
                }
            }
        }
    }

    private fun rateApp() {
        view.rateApp()
    }


    override fun onOptionsItemSelected(item: MenuItem, context: Context) {
        GlobalScope.launch(Dispatchers.Main) {
            when (item.itemId) {
                R.id.saveImage -> {
                    rateApp()
                    GlobalScope.async { saveImageDisplayedToPhone(context) }.await()
                    interactor.analytics.logEvent(AnalyticsEvent.SAVE_IMAGE)
                    context.toast(context.getString(R.string.image_saved_toast))
                }
                R.id.shareIamge -> {
                    rateApp()
                    shareImageToOtherApps()
                    interactor.analytics.logEvent(AnalyticsEvent.SHARE_IMAGE)
                }
            }
        }
    }

    fun startCameraActivity() {
        view.startCameraActivity()
    }

    fun getGeneratorImage(ganValue: Double): IntArray {
        return easyGenerator.sampleWithSlider(ganValue.toFloat())
    }

    fun changeGanImageFromSlider(ganValue: Double) {
        if (mutex.tryLock()) {
            val imageManipluatedFromZValue = getGeneratorImage(ganValue)
            val ganImage = imageManipluatedFromZValue.toBitmap(easyGenerator.width, easyGenerator.height)
            imageManipulatedFromzValue = ganImage
            ganImage.let { inlineImage(person, it) }

            view.displayFocusedImage(ganImage)
            mutex.unlock()

        }
    }

    fun getInfoFromFragmentCreation(arguments: Bundle) {
        generator = arguments.getParcelable(ModelFragment.MODEL_CONTROLS)
        val faceImage = readImageToBytes(arguments.getString(ModelFragment.IMAGE_PARAM))
        generatorIndex = arguments.getInt(ModelFragment.GENERATOR_INDEX)
        val fullImage: String? = arguments.getString(ModelFragment.FULL_IMAGE_LOCATION)
        val fullImageBit = if (fullImage != null) {
            File(fullImage).readBytes()
        } else {
            easyGenerator.sampleImageWithoutImage().toByteArrayImage()
        }
        this.person = Person(faceImage, fullImageBit)
    }

    fun askToRateAppNextSave() {
        interactor.settings.setAppOpenedAlready()
    }
}

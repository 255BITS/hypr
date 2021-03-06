package hypr.hypergan.com.hypr.MultiFaceSelection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.SparseArray
import collections.forEach
import com.google.android.gms.vision.face.Face
import hypr.hypergan.com.hypr.Util.FaceDetection
import hypr.hypergan.com.hypr.Util.ImageSaver
import hypr.hypergan.com.hypr.Util.SettingsHelper
import hypr.hypergan.com.hypr.Util.toByteArray
import java.io.File

class MultiFacePresenter(val view: MultiFaceMVP.view, val context: Context) : MultiFaceMVP.presenter {
    var imageOfPeoplesFaces: Bitmap? = null
    lateinit var faceCoordinates: SparseArray<Face>


    override fun displayImageWithFaces(imageOfPeoplesFaces: Bitmap?) {
        imageOfPeoplesFaces?.let { view.displayImageWithFaces(it) }
    }

    override fun addFaceBoxesToMultipleFacesImage(context: Context, imageOfPeoplesFaces: Bitmap?): Bitmap? {
//        var face: Bitmap? = null
        this.imageOfPeoplesFaces = imageOfPeoplesFaces
        if (imageOfPeoplesFaces != null) {
            val faceLocations = FaceDetection(context).getFaceLocations(imageOfPeoplesFaces, context)
            this.faceCoordinates = faceLocations!!
//            face = imageOfPeoplesFaces.copy(Bitmap.Config.ARGB_8888, true)
//            val canvasImageWithFaces = Canvas(face)
            faceLocations.forEach { i, facrCoordinate ->
                val rect = getFaceBoxLocationInImage(facrCoordinate)
//                view.addBoxAroundFace(rect, canvasImageWithFaces)
                view.addFaceLocationToImage(rect)
            }
        }
//        return face
        return imageOfPeoplesFaces
    }

    override fun sendCroppedFaceToMultiModel(croppedFace: Bitmap, index: Int) {
        val croppedImage = saveImageSoOtherFragmentCanViewIt(croppedFace.toByteArray(), "image")
        val fullImage = saveImageSoOtherFragmentCanViewIt(imageOfPeoplesFaces?.toByteArray(), "fullimage")
        SettingsHelper(context).setFaceIndex(index)
        view.sendImageToModel(croppedImage, fullImage)
    }

    override fun saveImageSoOtherFragmentCanViewIt(image: ByteArray?, filename: String): File {
        val file = File.createTempFile(filename, "png")
        ImageSaver().saveImageToFile(file, image)
        return file
    }

    private fun getFaceBoxLocationInImage(face: Face): Rect {
        return with(face) {
            val left: Int = (position.x - (0)).toInt()
            val right: Int = (position.x + (width)).toInt()
            val top: Int = (position.y - (0)).toInt()
            val bottom: Int = (position.y + (height)).toInt()
            Rect(left, top, right, bottom)
        }
    }

    override fun cropFaceFromImage(image: Bitmap, index: Int, context: Context): Bitmap {
        val images = FaceDetection(context).getListOfFaces(faceCoordinates, image)
        SettingsHelper(context).saveFaceLocation(images[index].faceLocation)
        return images[index].croppedFace
    }

}
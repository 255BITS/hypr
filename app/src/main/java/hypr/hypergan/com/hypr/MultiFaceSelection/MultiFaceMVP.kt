package hypr.hypergan.com.hypr.MultiFaceSelection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import java.io.File

interface MultiFaceMVP{
    interface view{
        fun displayImageWithFaces(imageOfPeople: Bitmap)
        fun addBoxAroundFace(rect: Rect, canvasImageWithFaces: Canvas)
        fun addFaceLocationToImage(rect: Rect)

        fun sendImageToModel(file: File, fullImage: File)
    }
    interface presenter{
        fun displayImageWithFaces(image: Bitmap?)
        fun addFaceBoxesToMultipleFacesImage(faceLocations: Context, imageOfPeoplesFaces: Bitmap?): Bitmap?
        fun cropFaceFromImage(image: Bitmap, bounds: Int, context: Context): Bitmap
        fun sendCroppedFaceToMultiModel(croppedFace: Bitmap, index: Int)
        fun saveImageSoOtherFragmentCanViewIt(image: ByteArray?, s: String): File
    }
}
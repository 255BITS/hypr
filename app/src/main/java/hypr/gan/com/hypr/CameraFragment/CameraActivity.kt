package hypr.gan.com.hypr.CameraFragment

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PointF
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.flurgle.camerakit.CameraView
import com.pawegio.kandroid.start
import hypr.gan.com.hypr.Main.MainActivity
import hypr.gan.com.hypr.R
import hypr.gan.com.hypr.Util.ImageSaver
import hypr.gan.com.hypr.Util.onPictureTaken
import hypr.gan.com.hypr.Util.toByteArray
import kotlinx.android.synthetic.main.activity_camera.*
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import java.io.File

class CameraActivity : AppCompatActivity(), CameraMVP.view {

    val presenter: CameraPresenter by lazy { CameraPresenter(this, applicationContext) }
    private val RESULT_GET_IMAGE: Int = 1
    var indexInJson: Int? = null
    val galleryFileLocation: Uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        setSupportActionBar(toolbar)
        indexInJson = intent.extras.getInt("indexInJson")
        takePictureListener(cameraView)
        takePicture.setOnClickListener { presenter.captureImage() }
    }

    override fun noFaceDetectedPopup() {
        longToast(getString(R.string.select_image_with_face))
    }

    fun galleryButtonClick(view: View) {
        presenter.displayGallery()
    }

    fun switchCameraClick(view: View) {
        cameraView.toggleFacing()
    }

    override fun takePicture() {
        cameraView.captureImage()
    }

    override fun isCameraViewEnabled(): Boolean {
        return cameraView.isEnabled
    }

    override fun startMultiFaceSelection(jpeg: ByteArray, facesDetected: MutableList<PointF>) {
        val intent = Intent()
        val image = ImageSaver().saveImageToFile(createTempFile("fullimage", "png"), jpeg)
        intent.putExtra("image", image.path)
        intent.putExtra("faceLocations", facesDetected.toTypedArray())
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun showFaceTooCloseErrorTryAgain() {
        toast(getString(R.string.face_too_close_camera_error))
    }
    private fun takePictureListener(cameraView: CameraView) {
        cameraView.onPictureTaken { jpeg -> presenter.sendPictureToModel(jpeg) }
    }

    override fun navigateUpActivity() {
        NavUtils.navigateUpFromSameTask(this)
    }

    override fun displayGallery() {
        val intent = Intent(Intent.ACTION_PICK, galleryFileLocation)
        startActivityForResult(intent, RESULT_GET_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == RESULT_GET_IMAGE && resultCode == Activity.RESULT_OK) {
            intent?.data?.let { presenter.getImageFromImageFileLocation(it) }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        intentFor<MainActivity>().start(applicationContext)
    }

    override fun onResume() {
        super.onResume()
        cameraView.start()
    }

    override fun onPause() {
        super.onPause()
        cameraView.stop()
    }

    override fun sendImageToModel(image: ByteArray?, croppedFace: Bitmap) {
        val fullImage = saveImageSoOtherFragmentCanViewIt(image)
        val croppedFaceFile = ImageSaver().saveImageToFile(createTempFile("fullimage", "jpg"), croppedFace.toByteArray())

        startActivity(intentFor<MainActivity>
//        ("indexInJson" to indexInJson, "image" to fullImage.path).clearTop())
        ("indexInJson" to indexInJson, "image" to croppedFaceFile.path, "fullimage" to fullImage.path).clearTop())
    }

    fun saveImageSoOtherFragmentCanViewIt(image: ByteArray?): File {
        val file = File.createTempFile("image", "png")
        ImageSaver().saveImageToFile(file, image)
        return file

    }
}
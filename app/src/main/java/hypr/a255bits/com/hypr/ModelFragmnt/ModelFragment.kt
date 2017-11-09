package hypr.a255bits.com.hypr.ModelFragmnt

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import com.pawegio.kandroid.onProgressChanged
import hypr.a255bits.com.hypr.CameraFragment.CameraActivity
import hypr.a255bits.com.hypr.Generator.Generator
import hypr.a255bits.com.hypr.R
import hypr.a255bits.com.hypr.Util.negative1To1
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_model.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.alert
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.intentFor
import java.io.File


class ModelFragment : Fragment(), ModelFragmentMVP.view {

    var pbFile: File? = null
    val interactor by lazy { ModelInteractor(context) }
    val presenter by lazy { ModelFragmentPresenter(this, interactor, context, pbFile) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            presenter.getInfoFromFragmentCreation(arguments)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater!!.inflate(R.layout.fragment_model, container, false)
    }

    override fun lockModel() {
        lockLayout.visibility = View.VISIBLE
        imageTransitionSeekBar.isEnabled = false
    }

    override fun unLockModel() {
        lockLayout.visibility = View.INVISIBLE
        imageTransitionSeekBar.isEnabled = true

    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayImageTransitionSeekbarProgress()
        randomizeModelClickListener()
        chooseImageFromGalleryButtonClickListener()
        lockLayoutClickListener()
    }

    private fun randomizeModelClickListener() {
        randomizeModel.setOnClickListener {
            presenter.direction = presenter.easyGenerator.random_z()
            presenter.randomizeModel(imageTransitionSeekBar.progress)
        }
    }

    private fun chooseImageFromGalleryButtonClickListener() {
        chooseImageFromGalleryButton.setOnClickListener {
            presenter.startCameraActivity()
        }
    }

    private fun lockLayoutClickListener() {
        lockLayout.setOnClickListener {
            activity.alert("Would you like to buy this model?", "Hypr") {
                positiveButton("Buy", { EventBus.getDefault().post(presenter.generatorIndex) })
                cancelButton { dialog -> dialog.dismiss() }
            }.show()
        }
    }

    override fun startCameraActivity() {
        val intent = activity.intentFor<CameraActivity>("indexInJson" to 0)
        EventBus.getDefault().post(intent)
    }

    private fun displayImageTransitionSeekbarProgress() {
        imageTransitionSeekBar.onProgressChanged { progress, _ ->
            presenter.changeGanImageFromSlider(progress.negative1To1())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.image_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        presenter.onOptionsItemSelected(item, context)
        return super.onOptionsItemSelected(item)
    }

    override fun onDetach() {
        super.onDetach()
        presenter.disconnectFaceDetector()
    }

    override fun displayFocusedImage(imageFromGallery: Bitmap?) {
        focusedImage.setImageBitmap(imageFromGallery)
    }


    override fun shareImageToOtherApps(shareIntent: Intent) {
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_image)))
    }

    override fun requestPermissionFromUser(permissions: Array<String>, REQUEST_CODE: Int) {
        requestPermissions(permissions, REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        presenter.onRequestPermissionResult(requestCode, permissions, grantResults)
    }

    companion object {
        val IMAGE_PARAM = "param2"
        val MODEL_CONTROLS = "modelControls"
        val GENERATOR_INDEX = "generatorPosition"
        val FULL_IMAGE_LOCATION = "fulliamgelocation"


        fun newInstance(generator: Generator, image: String?, pbFile: File, generatorIndex: Int, fullImage: String?): ModelFragment {
            val fragment = ModelFragment()
            val args = Bundle()
            args.putString(IMAGE_PARAM, image)
            args.putString(FULL_IMAGE_LOCATION, fullImage)
            args.putParcelable(MODEL_CONTROLS, generator)
            args.putInt(GENERATOR_INDEX, generatorIndex)
            fragment.arguments = args
            fragment.pbFile = pbFile
            return fragment
        }
    }

}

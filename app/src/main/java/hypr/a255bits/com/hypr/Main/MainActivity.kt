package hypr.a255bits.com.hypr.Main

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.SubMenu
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.SpinnerAdapter
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.api.GoogleApiClient
import hypr.a255bits.com.hypr.BuyGenerator
import hypr.a255bits.com.hypr.CameraFragment.CameraActivity
import hypr.a255bits.com.hypr.Generator.Control
import hypr.a255bits.com.hypr.Generator.Generator
import hypr.a255bits.com.hypr.MultiModels.MultiModels
import hypr.a255bits.com.hypr.R
import hypr.a255bits.com.hypr.Util.InAppBilling.IabHelper
import hypr.a255bits.com.hypr.WelcomeScreen.WelcomeScreen
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.android.synthetic.main.app_bar_main2.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, MainMvp.view {

    val interactor by lazy { MainInteractor(applicationContext) }
    val presenter by lazy { MainPresenter(this, interactor, applicationContext) }
    private var modelSubMenu: SubMenu? = null
    var progressDownloadingModel: ProgressDialog? = null
    private val SIGN_INTO_GOOGLE_RESULT: Int = 12
    val ZERO_PERCENT: Float = -0.0f
    private var spinner: Spinner? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        setSupportActionBar(toolbar)
        presenter.addModelsToNavBar()
        setupDrawer(toolbar)

    }

    override fun buyModelPopup(skus: String, billingHelper: IabHelper?) {
        billingHelper?.launchPurchaseFlow(this, skus, 1001, { result, info ->
            if (result.isSuccess) {
                println("success")
            } else {
                println("buy error: $result")
            }
        }, "")
    }

    override fun popupSigninGoogle(googleSignInClient: GoogleApiClient) {
        alert {
            message = "Would you like to sign into Google?"
            title = "Sign in to Google"
            okButton {
                signinToGoogle(googleSignInClient)
            }
            cancelButton { dialog ->
                dialog.dismiss()
            }
        }.show()
    }


    fun signinToGoogle(googleSignInClient: GoogleApiClient) {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleSignInClient)
        startActivityForResult(signInIntent, SIGN_INTO_GOOGLE_RESULT)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SIGN_INTO_GOOGLE_RESULT && resultCode == Activity.RESULT_OK) {
            presenter.isLoggedIntoGoogle = true
        }
    }

    override fun startModelOnImage(buyGenerators: MutableList<BuyGenerator>) {
        if (intent.hasExtra("indexInJson")) {
            val indexInJson = intent.extras.getInt("indexInJson")
            val image = intent.extras.getByteArray("image")
            presenter.startModel(indexInJson, image)
        } else {
            displayGeneratorsOnHomePage(buyGenerators)
        }
    }

    override fun displayGeneratorsOnHomePage(generators: MutableList<BuyGenerator>) {
        val fragment: Fragment = WelcomeScreen.newInstance(generators, "")
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
        presenter.startModel(0)
    }

    fun setupDrawer(toolbar: Toolbar) {
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.setDrawerListener(toggle)
        toggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)
        val navMenu = navigationView?.menu
        modelSubMenu = navMenu?.addSubMenu("Models")
    }

    override fun startCameraActivity(indexInJson: Int) {
        startActivity(intentFor<CameraActivity>("indexInJson" to indexInJson))
    }

    override fun applyModelToImage(controlArray: Array<Control>, image: ByteArray?, path: String, generators: List<Generator>?, itemId: Int) {
        val fragment: Fragment = MultiModels.newInstance(generators, itemId, path, presenter.file)
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
    }

    override fun addModelsToNavBar(generator: Generator, index: Int) {
        modelSubMenu?.add(R.id.group1, index, index, generator.name)
        modelSubMenu?.getItem(index)?.setIcon(R.drawable.ic_lock)

    }

    override fun displayModelDownloadProgress() {
        progressDownloadingModel = progressDialog("Downloading Model") {
            setMessage("It's downloading..")
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            max = 100
        }
        progressDownloadingModel?.show()

    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        presenter.onNavigationItemSelected(item)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    @Subscribe
    fun startIntent(intent: Intent){
        startActivity(intent)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun showModelDownloadProgress(progressPercent: java.lang.Float) {
        println("percent: $progressPercent")
        when {
            presenter.isDownloadComplete(progressPercent.toFloat()) -> presenter.downloadingModelFinished()
            progressPercent.toFloat() == ZERO_PERCENT -> {
            }
            else -> progressDownloadingModel?.progress = progressPercent.toInt()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun openCamera(index: java.lang.Integer) {
        startCameraActivity(index.toInt())
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun addControlNamesToToolbar(controlNames: List<String?>?) {
        toolbar.title = ""
        val adapter: SpinnerAdapter = ArrayAdapter<String>(this, R.layout.spinner_dropdown_item, controlNames)
        spinner.let { toolbar.removeView(it) }
        if (controlNames!!.isNotEmpty()) {

            this.spinner = Spinner(this)
            spinner?.background?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            spinner?.adapter = adapter
            toolbar.addView(spinner)
        }
    }

    override fun closeDownloadingModelDialog() {
        progressDownloadingModel?.dismiss()
    }


    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.stopInAppBilling()
    }
}

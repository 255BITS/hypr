package hypr.a255bits.com.hypr.Main

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.SubMenu
import hypr.a255bits.com.hypr.CameraFragment.CameraActivity
import hypr.a255bits.com.hypr.Generator
import hypr.a255bits.com.hypr.ModelFragmnt.ModelFragment
import hypr.a255bits.com.hypr.R
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.android.synthetic.main.app_bar_main2.*
import org.jetbrains.anko.intentFor

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, MainMvp.view {

    val interactor by lazy { MainInteractor(applicationContext) }
    val presenter by lazy { MainPresenter(this, interactor, applicationContext) }
    private var modelSubMenu: SubMenu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        setSupportActionBar(toolbar)
        setupDrawer(toolbar)
        presenter.addModelsToNavBar()

    }

    override fun startModelOnImage() {
        if (intent.hasExtra("indexInJson")) {
            val indexInJson = intent.extras.getInt("indexInJson")
            val image = intent.extras.getByteArray("image")
            presenter.startModel(indexInJson, image)
        }
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

    override fun startModelFragment(indexInJson: Int) {
        startActivity(intentFor<CameraActivity>("indexInJson" to indexInJson))
    }

    override fun applyModelToImage(modelUrl: String, image: ByteArray?) {
        val fragment: Fragment = ModelFragment.newInstance(modelUrl, image)
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()

    }

    override fun modeToNavBar(generator: Generator, index: Int) {
        modelSubMenu?.add(R.id.group1, index, index, generator.name)
        modelSubMenu?.getItem(index)?.setIcon(R.drawable.ic_lock)

    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.itemId in 0..100) {
            presenter.startModel(item.itemId)

        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }


}

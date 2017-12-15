package hypr.a255bits.com.hypr.Main

import android.content.Context
import android.support.v4.app.Fragment
import android.view.MenuItem
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.storage.FileDownloadTask
import hypr.a255bits.com.hypr.BuyGenerator
import hypr.a255bits.com.hypr.Dashboard.DashboardFragment
import hypr.a255bits.com.hypr.Generator.Generator
import hypr.a255bits.com.hypr.ModelFragmnt.ModelFragment
import hypr.a255bits.com.hypr.Util.InAppBilling.IabHelper
import hypr.a255bits.com.hypr.Util.InAppBilling.IabResult
import kotlinx.coroutines.experimental.Deferred
import java.io.File

interface MainMvp {
    interface view {
        fun addModelsToNavBar(generator: Generator, index: Int)
        fun displayModelDownloadProgress()
        fun closeDownloadingModelDialog()
        fun startCameraActivity(indexInJson: Int)
        fun startMultipleModels(multiModels: DashboardFragment)
        fun  displayGeneratorsOnHomePage(generators: MutableList<BuyGenerator>)
        fun popupSigninGoogle(googleSignInClient: GoogleApiClient)
        fun  buyModelPopup(skus: String, billingHelper: IabHelper?, generatorIndex: Int)
        fun goBackToMainActivity()
        fun  displayBackButton()

    }

    interface presenter {
        fun addModelsToNavBar(applicationContext: Context)
        fun isDownloadComplete(progressPercent: Float): Boolean
        fun downloadingModelFinished()
        fun startModel(itemId: Int)
        fun createMultiModels(itemId: Int, image: String?)
        fun createGeneratorLoader(fileName: String, itemId: Int)
        fun stopInAppBilling()
        fun  signInToGoogle(googleSignInClient: GoogleApiClient)
        fun  buyModel(skus: String, generatorIndex: Int)
        fun onNavigationItemSelected(item: MenuItem)
        fun onOptionsItemSelected(item: MenuItem?)
        fun handlePurchase(result: IabResult, generatorIndex: Int)
        fun getModelFragment(position: Int): ModelFragment?
    }

    interface interactor {
        fun getGeneratorsFromNetwork(applicationContext: Context): Deferred<List<Generator>?>

        fun getModelFromFirebase(saveLocation: File, filenameInFirebase: String): FileDownloadTask?
        fun stopInAppBilling()
        fun hasBoughtItem(itemId: String): Boolean
    }

}

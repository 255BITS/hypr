package hypr.a255bits.com.hypr.Main

import android.content.Context
import hypr.a255bits.com.hypr.BuyGenerator
import hypr.a255bits.com.hypr.Generator.Control
import hypr.a255bits.com.hypr.Generator.Generator
import java.io.File

class MainPresenter(val view: MainMvp.view, val interactor: MainInteractor, val context: Context) : MainMvp.presenter {
    val file = File(context.filesDir, "optimized_weight_conv.pb")
    private val DOWNLOAD_COMPLETE: Float = 100.0f
    var buyGenerators: MutableList<BuyGenerator> = mutableListOf()

    override fun createGeneratorLoader(file: File, itemId: Int) {
        if (!file.exists()) {
            val pbFilePointer = interactor.getModelFromFirebase(file, "optimized_weight_conv.pb")
            interactor.showProgressOfFirebaseDownload(pbFilePointer)
            pbFilePointer.addOnSuccessListener { taskSnapshot ->
                println("successs")
                view.startCameraActivity(itemId)
            }
        } else {
            view.startCameraActivity(itemId)
        }
    }

    override fun startModel(itemId: Int) {
        val generator = interactor.listOfGenerators?.get(itemId)
        if (generator != null) {
            createGeneratorLoader(file, itemId)
//            view.displayModelDownloadProgress()
//            val file = File.createTempFile("optimized_weight_conv", "pb")
//            val filePointer = interactor.getModelFromFirebase(file, "optimized_weight_conv.pb")
//            interactor.showProgressOfFirebaseDownload(filePointer)
        }
    }

    override fun startModel(itemId: Int, image: ByteArray?) {
        val generator = interactor.listOfGenerators?.get(itemId)
        if (generator != null) {
            val controlArray: Array<Control>? = generator.generator?.viewer?.controls?.toTypedArray()
            controlArray?.let { view.applyModelToImage(it, image) }
        }
    }

    override fun downloadingModelFinished() {
        view.closeDownloadingModelDialog()
    }

    override fun isDownloadComplete(progressPercent: Float): Boolean {
        return progressPercent >= DOWNLOAD_COMPLETE
    }


    override fun addModelsToNavBar() {
        interactor.addModelsToNavBar(object : GeneratorListener {
            override fun getGenerators(generators: List<Generator>, index: Int) {
                buyGenerators = mutableListOf<BuyGenerator>()
                generators.forEachIndexed { index, generator ->
                    view.modeToNavBar(generator, index)
                    if (generator.name != null) {

                        val buyGenerator = BuyGenerator(generator.name!!)
                        buyGenerators.add(buyGenerator)
                    }
                }
                view.startModelOnImage(buyGenerators)
            }
        })
    }
}
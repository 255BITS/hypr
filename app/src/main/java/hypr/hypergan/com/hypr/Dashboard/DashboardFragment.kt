package hypr.hypergan.com.hypr.Dashboard

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import hypr.hypergan.com.hypr.BuyGenerator
import hypr.hypergan.com.hypr.Generator.Generator
import hypr.hypergan.com.hypr.R
import kotlinx.android.synthetic.main.fragment_dashboard.*

class DashboardFragment : Fragment(), DashboardMVP.view {
   val presenter by lazy { DashboardPresenter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            presenter.generators = arguments?.getParcelableArray(GENERATORS) as Array<Generator>
            presenter.indexOfGenerator = arguments?.getInt(INDEX_OF_GENERATOR_IN_USE)
            presenter.image = arguments?.getString(PATH_TO_IMAGE)
            presenter.fullImage = arguments?.getString(PATH_TO_FULL_IMAGE)

            presenter.pathToGenerators = arguments?.getStringArray(PATH_TO_GENERATORS) as Array<String?>
            presenter.isBackPressed = arguments?.getBoolean(IS_BACK_PRESS) as Boolean
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (!presenter.isBackPressed) {
            presenter.startModelIfFullImageIsPresent()
        }
        return inflater!!.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view as View, savedInstanceState)
        presenter.displayListOfModels(context as Context)
    }

    override fun displayListOfModels(buyGenerators: MutableList<BuyGenerator>, welcomeScreenAdapter: WelcomeScreenAdapter) {
        recyclerview.layoutManager = GridLayoutManager(activity, 2)
        recyclerview.adapter = welcomeScreenAdapter
        welcomeScreenAdapter.notifyDataSetChanged()
    }

    companion object {
        private val GENERATORS = "param1"
        private val INDEX_OF_GENERATOR_IN_USE = "param2"
        private val PATH_TO_IMAGE: String? = "pathtoImage"
        private val PATH_TO_GENERATORS: String? = "pathtoGenerator"
        private val PATH_TO_FULL_IMAGE: String = "pathtofullimage"
        private val IS_BACK_PRESS: String = "isbackpress"

        fun newInstance(generators: List<Generator>?, indexOfGenerator: Int, pathToImage: String?, generatorPaths: Array<String>, fullImage: String?, onBackPressed: Boolean): DashboardFragment {
            val fragment = DashboardFragment()
            val args = Bundle()
            args.putParcelableArray(GENERATORS, generators?.toTypedArray())
            args.putInt(INDEX_OF_GENERATOR_IN_USE, indexOfGenerator)
            args.putString(PATH_TO_IMAGE, pathToImage)
            args.putStringArray(PATH_TO_GENERATORS, generatorPaths)
            args.putString(PATH_TO_FULL_IMAGE, fullImage)
            args.putBoolean(IS_BACK_PRESS, onBackPressed)
            fragment.arguments = args
            return fragment
        }
    }
}

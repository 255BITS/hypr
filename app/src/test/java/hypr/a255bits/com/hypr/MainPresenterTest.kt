package hypr.hypergan.com.hypr

import android.content.Context
import com.nhaarman.mockito_kotlin.mock
import hypr.hypergan.com.hypr.Main.MainInteractor
import hypr.hypergan.com.hypr.Main.MainMvp
import hypr.hypergan.com.hypr.Main.MainPresenter
import org.junit.Test
import java.io.File


class MainPresenterTest{
    val view: MainMvp.view = mock()
    val interactor: MainInteractor = mock()
    val context: Context = mock()
    val presenter: MainPresenter = MainPresenter(view, interactor, context)
    @Test
    fun openCameraApp(){
        val file: File = getFileFromPath(this, "testfile")
//        presenter.createGeneratorLoader(file, any())
//        verify(view).startCameraActivity(any())

    }

    fun getFileFromPath(obj: Any, fileName: String): File {
        val classLoader = obj.javaClass.classLoader
        val resource = classLoader.getResource(fileName)
        return File(resource.path)
    }
}
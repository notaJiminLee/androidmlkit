package com.example.SchoolProject

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.SchoolProject.labeldetector.LabelDetectorProcessor
import com.google.android.gms.common.annotation.KeepName
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


@Suppress("DEPRECATION")
@KeepName
class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {
    private var imageCapture: ImageCapture? = null

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private var imageProcessor: VisionImageProcessor? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var needUpdateGraphicOverlayImageSourceInfo = false
    private var lensFacing = CameraSelector.LENS_FACING_BACK

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 제일 처음 앱 실행할 때 카메라 권한이 있는지 확인하고, 없으면 권한 요청
        if (allPermissionsGranted()) {
            startCamera()  // 카메라 권한이 있다면 카메라 실행
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // 촬영 버튼 ClickListener 설정
        camera_capture_button.setOnClickListener { takePhoto() }
        graphicOverlay = findViewById(R.id.graphic_overlay)
        outputDirectory = getOutputDirectory()  // 저장할 파일 경로 설정
        cameraExecutor = Executors.newSingleThreadExecutor()

        imageProcessor = LabelDetectorProcessor(this)
    }

    private fun takePhoto() {
        needUpdateGraphicOverlayImageSourceInfo = true

        Log.d("CUSTOM", "Function 'takePhoto' started.")
        // use case(?) 가져오고 null이면 종료(이미지 캡처를 설정하기 전에 사진 버튼을 탭하면 null)
        val imageCapture = imageCapture ?: return
        //var ar: analyzer? = analyzer(this)  // 이미지 받아와서 처리하고 Inference 해주는 class. 여기에 이미지 전달해주기 위해서 변수에 저장
        val activity = this
        // 사진 촬영 후 호출되는 capture 리스너 설정
        imageCapture.takePicture(ContextCompat.getMainExecutor(this), object :
            ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                //Log.d("CUSTOM", "Function 'takePicture' started.")
                //ar?.analyze(image)  // analyzer 클래스의 analyze 함수에 이미지 전달
                Log.i("CUSTOM","Using Image Label Detector Processor")

                if (needUpdateGraphicOverlayImageSourceInfo) {
                    Log.i("CUSTOM","needUpdateGraphicOverlayImageSourceInfo")
                    val rotationDegrees = image.imageInfo.rotationDegrees
                    if (rotationDegrees == 0 || rotationDegrees == 180) {
                        graphicOverlay!!.setImageSourceInfo(image.width, image.height, false)
                        Log.d("CUSTOM", "width : " + image.width + ", height : " + image.height)
                    } else {
                        graphicOverlay!!.setImageSourceInfo(image.height, image.width, false)
                        Log.d("CUSTOM", "width : " + image.width + ", height : " + image.height)
                    }
                    needUpdateGraphicOverlayImageSourceInfo = false
                }

                imageProcessor!!.processImageProxy(image, graphicOverlay)
                super.onCaptureSuccess(image)
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
            }

        })
    }

    private fun startCamera() {
        // 카메라의 라이프 사이클을 라이프 사이클 소유자에게 바인딩하는 데 사용되는 ProcessCameraProvider 인스턴스 생성
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {  // 리스너 추가

            // 카메라의 수명주기를 LifecycleOwner 애플리케이션의 프로세스 내에서 바인딩하는 데 사용
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // 개체 초기화, 빌드 호출, 뷰 파인더에서 SurfaceProvider를 가져오고 미리보기에서 설정
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.createSurfaceProvider())
                }

            imageCapture = ImageCapture.Builder().build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, analyzer(this))
                }

            // Default 카메라 선택(후면)
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // 다시 바인딩하기 전에 use cases 바인딩 해제
                cameraProvider.unbindAll()

                // use cases를 카메라에게 바인딩
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {  // 요청 코드가 올바른지 확인
            if (allPermissionsGranted()) {
                startCamera()  // 권한 있으면 카메라 실행
            } else {  // 권한 없으면 알림
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
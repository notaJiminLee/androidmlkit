package com.example.SchoolProject.labeldetector

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import com.example.SchoolProject.GraphicOverlay
import com.example.SchoolProject.MainActivity
import com.example.SchoolProject.VisionProcessorBase
import com.example.SchoolProject.preference.PreferenceUtils
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.android.synthetic.main.activity_main.*

class LabelDetectorProcessor(context: Context) : VisionProcessorBase<List<ImageLabel>>(context){

    val context = context
    val options = ImageLabelerOptions.DEFAULT_OPTIONS
    private val imageLabeler: ImageLabeler = ImageLabeling.getClient(options)

    override fun detectInImage(image: InputImage): Task<List<ImageLabel>> {
        return imageLabeler.process(image)
    }

    override fun onSuccess(labels: List<ImageLabel>, graphicOverlay: GraphicOverlay) {
        graphicOverlay.add(LabelGraphic(graphicOverlay, labels))
        logExtrasForTesting(labels)
    }

    override fun onFailure(e: Exception) {
        Log.w("CUSTOM", "Label detection failed.$e")
    }

    companion object {
        private fun logExtrasForTesting(labels: List<ImageLabel>?) {
            if (labels == null) {
                Log.v(MANUAL_TESTING_LOG, "No labels detected")
            } else {
                labels.sortedBy {
                    it.confidence
                }
                Log.v(MANUAL_TESTING_LOG, String.format("Label %s, confidence %f", labels[0].text, labels[0].confidence))
//                for (label in labels) {
//                    Log.v(
//                        MANUAL_TESTING_LOG,
//                        String.format("Label %s, confidence %f", label.text, label.confidence)
//                    )
//                }
            }
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun processImageProxy(
        image: ImageProxy,
        graphicOverlay: GraphicOverlay
    ) {
        var bitmap: Bitmap? = null
        if (!PreferenceUtils.isCameraLiveViewportEnabled(graphicOverlay.context)) {
            val maina : MainActivity = context as MainActivity
            bitmap = context.viewFinder.bitmap
        }
        requestDetectInImage(
            InputImage.fromMediaImage(image.image!!, image.imageInfo.rotationDegrees),
            graphicOverlay, /* originalCameraImage= */
            bitmap, /* shouldShowFps= */
            true
        )
            // When the image is from CameraX analysis use case, must call image.close() on received
            // images when finished using them. Otherwise, new images may not be received or the camera
            // may stall.
            .addOnCompleteListener { image.close() }
    }
}
package com.example.SchoolProject

import android.annotation.SuppressLint
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.android.synthetic.main.activity_main.*
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


@Suppress("DEPRECATION")
class analyzer : ImageAnalysis.Analyzer {

    private val mainActivity : MainActivity
    private val tflite: Interpreter

    private val width : Int
    private val height : Int
    private val colorChannel : Int
    private val ORIENTATIONS = SparseIntArray()

    protected val inputArray : IntArray
    protected val outputArray : Array<FloatArray>

    companion object{
        const val DIM_BATCH_SIZE = 1
        const val DIM_PIXEL_SIZE = 4
    }

    enum class COLOR_CHANNEL {
        RGB, BGR, GRAYSCALE
    }

    constructor(context : MainActivity){  // analyzer 실행될 때 한 번만 실행되는 코드. 초기화해주는 정도?

        /*
        1. model 1생성(asset에서 tflite 파일 긁어오고, object에서 받기)
        2. @param image를 bitmap 으로 변환
        3. bitmap -> byte로 변환
        4. model -> byte input
        */

        this.mainActivity = context
        var ast: AssetManager = context.assets
        tflite = Interpreter(loadModelFile(ast,"face_v2.1_final_post_quant.tflite"))

        val inputSize = tflite!!.getInputTensor(0).shape()
        width = inputSize[1]!!
        height = inputSize[2]!!
        colorChannel = inputSize[3]!!

        this.inputArray = IntArray(width * height)

        val outputSize = tflite!!.getOutputTensor(0).shape()
        this.outputArray = Array(outputSize[0]) { FloatArray(outputSize[1]) }

        this.imageBuffers = ByteBuffer.allocateDirect(DIM_BATCH_SIZE * this.width * this.height * DIM_PIXEL_SIZE * this.colorChannel)
        this.imageBuffers!!.order(ByteOrder.nativeOrder())

    }

    init {
        ORIENTATIONS.append(Surface.ROTATION_0, 0)
        ORIENTATIONS.append(Surface.ROTATION_90, 90)
        ORIENTATIONS.append(Surface.ROTATION_180, 180)
        ORIENTATIONS.append(Surface.ROTATION_270, 270)
    }


    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(image: ImageProxy) {
        Log.e("CUSTOM", "analyze joined")

        try {
            val bitmap = mainActivity.viewFinder.bitmap  // 비트맵 이미지
            val resized = Bitmap.createScaledBitmap(bitmap!!, width, height, true)  // 비트맵 resize
            val inputbuffer = convertBitmapToByteBuffer(resized)

            Log.e("CUSTOM", "analyze joinedadfdasfdfasdaf")
            tflite.run(inputbuffer, outputArray)

            Log.d("CUSTOM", outputArray[0].joinToString(" "))


            /**
             * Image Labeling MLkit 모델 돌리는 부분
             */
            val mediaImage = image.image
            val image = InputImage.fromMediaImage(mediaImage!!, image.imageInfo.rotationDegrees)
            val rotationDegrees = image.rotationDegrees


            val inputimage = InputImage.fromByteBuffer(
                inputbuffer!!,
                /* image width */ width,
                /* image height */ height,
                rotationDegrees,
                InputImage.IMAGE_FORMAT_NV21 // or IMAGE_FORMAT_YV12
            )

            val options = ImageLabelerOptions.Builder().setConfidenceThreshold(0.9f).build()

            val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

            labeler.process(inputimage)
                .addOnSuccessListener {  labels ->

                    labels.sortBy {
                        it.confidence
                    }

                    for (label in labels) {
                        val text = labels[0].text
                        val confidence = labels[0].confidence
                        val index = labels[0].index
                        Toast.makeText(mainActivity, "Label is : " + labels.size, Toast.LENGTH_SHORT).show()
                        Log.d("CUSTOM", "Labels : " + labels[0])
                        Log.d("CUSTOM", "Label.text : " + text + ", Label.confidence : " + confidence + ", Label.Index : " + index)
                    }
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    // ...
                }



        }catch(e: IllegalStateException){
            Log.e("CUSTOM", "IllegalStateException : " + e)
            return
        }

    }


    /**
     * Preallocated buffers for storing image data in.
     */
    protected var imageBuffers : ByteBuffer? = null

    /**
     * Writes Image data into a `ByteBuffer`.
     */
    internal fun convertBitmapToByteBuffer(bitmap: Bitmap) : ByteBuffer? {
        if(imageBuffers == null)
            return null

        imageBuffers!!.rewind()
        applyNormalization(imgData = imageBuffers!!, bitmap = bitmap, isModelQuantized = false)

        return imageBuffers
    }

    private fun loadModelFile(assets: AssetManager, modelFilename: String): MappedByteBuffer {
        val fileDescriptor = assets?.openFd(modelFilename)
        Log.e("ddddddddddd", "he" + fileDescriptor)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Apply normalization to the input bitmap before processing it with the ai model
     *
     * @param imgData : ByteBuffer to hold processed pixel values of the bitmap
     * @param pixelArray : pixel values retrieved from the bitmap
     * @param imgMean : mean value of the pixel value. If the model uses range -1~1, imgMean = 128f, if 0~1, imgMean = 0f
     * @param imgStd : standard deviation of the pixel value. If the model uses range -1~1, imgStd = 128f, if 0~1, imgStd = 255f
     * @param isModelQuantized : if model is not quantized, all pixel values are then normalized
     */
    internal fun applyNormalization(imgData : ByteBuffer, bitmap : Bitmap, imgMean: Float = 0f, imgStd: Float = 255f, isModelQuantized: Boolean = false){
        bitmap.getPixels(inputArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var r : Any
        var g : Any
        var b : Any

        for(pixelValue in inputArray){
            if(isModelQuantized){
                r = (pixelValue shr 16 and 0xFF).toByte()
                g = (pixelValue shr 8 and 0xFF).toByte()
                b = (pixelValue shr 0xFF).toByte()
                orderPixelValue(COLOR_CHANNEL.RGB, imgData, r, g, b)

            } else { // Float mode
                r = ((pixelValue shr 16 and 0xFF) - imgMean) / imgStd
                g = ((pixelValue shr 8 and 0xFF) - imgMean) / imgStd
                b = ((pixelValue and 0xFF) - imgMean) / imgStd
                orderPixelValue(COLOR_CHANNEL.RGB, imgData, r, g, b)
            }
        }
    }



    /**
     * Reorder pixel values in the fashion the the model was initially trained for Float Model
     * @see COLOR_CHANNEL for detail
     */
    private fun orderPixelValue(colorChannel: COLOR_CHANNEL, imgData : ByteBuffer, r : Float, g : Float, b : Float) {
        when(colorChannel){
            COLOR_CHANNEL.RGB ->{
                imgData.putFloat(r)
                imgData.putFloat(g)
                imgData.putFloat(b)
            }

            COLOR_CHANNEL.BGR ->{
                imgData.putFloat(b)
                imgData.putFloat(g)
                imgData.putFloat(r)
            }
        }
    }

    /**
     * Reorder pixel values in the fashion the the model was initially trained for Quantized Model
     * @see COLOR_CHANNEL for detail
     */
    private fun orderPixelValue(colorChannel: COLOR_CHANNEL, imgData : ByteBuffer, r : Byte, g : Byte, b : Byte){

        when(colorChannel){
            COLOR_CHANNEL.RGB ->{
                imgData.put(r)
                imgData.put(g)
                imgData.put(b)
            }

            COLOR_CHANNEL.BGR ->{
                imgData.put(b)
                imgData.put(g)
                imgData.put(r)
            }
        }
    }

}
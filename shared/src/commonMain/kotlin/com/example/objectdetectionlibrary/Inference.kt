package com.example.myapplication8


import com.soywiz.kds.atomic.kdsFreeze
import com.suparnatural.core.fs.FileSystem
import de.voize.example.plm.PrePostProcessor
import de.voize.example.plm.Yolo5PostProcess

import de.voize.pytorch_lite_multiplatform.Tensor
import de.voize.pytorch_lite_multiplatform.TorchModule
import de.voize.pytorch_lite_multiplatform.IValue
import de.voize.pytorch_lite_multiplatform.plmScoped

import kotlin.native.concurrent.SharedImmutable
import kotlin.native.concurrent.ThreadLocal

class Inference {
    private val contentsDir = FileSystem.contentsDirectory.absolutePath
 private val localModulePath = contentsDir?.byAppending("yolov5s.torchscript.ptl")?.component!!
 //    private val localModulePath = contentsDir?.byAppending("dummy_module.ptl")?.component!!

 private lateinit var module: TorchModule
    private lateinit var classes: ArrayList<String>
    val output_size = 25200*85;


   constructor(path: String){
        this.intializeModule(path)
       kdsFreeze(this)
     }
    constructor(){

    }
    fun intializeModule(path: String) {
        module = TorchModule(path)
        println("hello module is intialized -> $path")
    }
    fun intializeModule(path: String,classPath:String) {
        module = TorchModule(path)

        println("hello module is intialized -> $path")
    }


    fun run(
        //path: String,
        floatArray: FloatArray,
        bitmapWidth: Int,
        bitmapHeight: Int,
        imageViewWidth:Int,
        imageViewHeight:Int,
        prePostProcessorWidth: Int,
        prePostProcessorHeight: Int
    ): ArrayList<Yolo5PostProcess.ResultRect>? {
        val rectResult =ArrayList<Yolo5PostProcess.ResultRect>()

        //new long[] {1, 3, height, width}
        if (this::module.isInitialized) {
            plmScoped {
                val inputTensor: Tensor = Tensor.fromBlob(
                    data = floatArray,
                    shape = longArrayOf(
                        1,
                        3,
                        prePostProcessorWidth.toLong(),
                        prePostProcessorHeight.toLong()
                    ),
                    scope = this
                )


                val inputIValue = IValue.from(inputTensor)
                val output: List<IValue> = module.forward(inputIValue).toTuple()

                // you could also use
                // module.runMethod("forward", inputIValue)

                try {
                    val outputTuple = output[0].toTensor()
                    outputTuple.let {
                        val outputData = it.getDataAsFloatArray()
                        val mResults: ArrayList<Yolo5PostProcess.Result> =
                            Yolo5PostProcess.postProcess(
                                outputData,
                                bitmapWidth,
                                bitmapHeight,
                                imageViewWidth,
                                imageViewHeight,
                                prePostProcessorWidth,
                                prePostProcessorHeight
                            )
                        for (result in mResults) {
                            rectResult.add(
                                Yolo5PostProcess.ResultRect(
                                    result.classIndex,
                                    result.score,
                                    result.rect.left,
                                    result.rect.top,
                                    result.rect.right,
                                    result.rect.bottom
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        return rectResult
    }
    fun run(
        //path: String,
        floatArray: List<Float>,
        bitmapWidth: Int,
        bitmapHeight: Int,
        imageViewWidth:Int,
        imageViewHeight:Int
    ): ArrayList<Yolo5PostProcess.ResultRect>? {
        val rectResult =ArrayList<Yolo5PostProcess.ResultRect>()

        //new long[] {1, 3, height, width}
        if (this::module.isInitialized) {
            plmScoped {
                val inputTensor: Tensor = Tensor.fromBlob(
                    data = floatArray.toFloatArray(),
                    shape = longArrayOf(
                        1,
                        3,
                        PrePostProcessor.mInputWidth.toLong(),
                        PrePostProcessor.mInputHeight.toLong()
                    ),
                    scope = this
                )


                val inputIValue = IValue.from(inputTensor)
                val output: List<IValue> = module.forward(inputIValue).toTuple()

                // you could also use
                // module.runMethod("forward", inputIValue)

                try {
                    val outputTuple = output[0].toTensor()
                    outputTuple.let {
                        val outputData = it.getDataAsFloatArray()
                        val mResults: ArrayList<Yolo5PostProcess.Result> =
                            Yolo5PostProcess.postProcess(
                                outputData,
                                bitmapWidth,
                                bitmapHeight,
                                imageViewWidth,
                                imageViewHeight,
                                640,
                                640
                            )
                        for (result in mResults) {
                            rectResult.add(
                                Yolo5PostProcess.ResultRect(
                                    result.classIndex,
                                    result.score,
                                    result.rect.left,
                                    result.rect.top,
                                    result.rect.right,
                                    result.rect.bottom
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        return rectResult
    }

    fun run(
        path: String,
        floatArray: FloatArray,
        bitmapWidth: Int,
        bitmapHeight: Int,
        imageViewWidth:Int,
        imageViewHeight:Int,
        prePostProcessorWidth: Int,
        prePostProcessorHeight: Int
    ): ArrayList<Yolo5PostProcess.ResultRect>? {
        val rectResult =ArrayList<Yolo5PostProcess.ResultRect>()

        if (!FileSystem.exists(path)) {
            println("File not found")
        } else {
            println("Loading module from file ")

            val module = TorchModule(path)
            //new long[] {1, 3, height, width}

            plmScoped {
                val inputTensor: Tensor = Tensor.fromBlob(
                    data = floatArray,
                    shape = longArrayOf(
                        1,
                        3,
                        prePostProcessorWidth.toLong(),
                        prePostProcessorHeight.toLong()
                    ),
                    scope = this
                )


                val inputIValue = IValue.from(inputTensor)
                val output: List<IValue> = module.forward(inputIValue).toTuple()

                // you could also use
                // module.runMethod("forward", inputIValue)

                try {
                    val outputTuple = output[0].toTensor()
                    outputTuple.let {
                        val outputData = it.getDataAsFloatArray()
                        val mResults: ArrayList<Yolo5PostProcess.Result> =
                            Yolo5PostProcess.postProcess(
                                outputData,
                                bitmapWidth,
                                bitmapHeight,
                                imageViewWidth,
                                imageViewHeight,
                                prePostProcessorWidth,
                                prePostProcessorHeight
                            )
                        for (result in mResults) {
                            rectResult.add(Yolo5PostProcess.ResultRect(result.classIndex,result.score,
                                result.rect.left,result.rect.top,result.rect.right,result.rect.bottom))
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return rectResult
    }

    fun run(
        floatArray: List<Float>,
        bitmap:Yolo5PostProcess.Size,
        imageView:Yolo5PostProcess.Size
    ): ArrayList<Yolo5PostProcess.ResultRect>? {
        val rectResult = ArrayList<Yolo5PostProcess.ResultRect>()
      //  println("localModulePath $localModulePath")
        plmScoped {
            val inputTensor: Tensor = Tensor.fromBlob(
                data = floatArray.toFloatArray(),
                shape = longArrayOf(
                    1,
                    3,
                    PrePostProcessor.mInputWidth.toLong(),
                    PrePostProcessor.mInputHeight.toLong()
                ),
                scope = this
            )
            val inputIValue = IValue.from(inputTensor)
            val output: List<IValue> = module.forward(inputIValue).toTuple()
            try {
                val outputTuple = output[0].toTensor()
                outputTuple.let {
                    val outputData = it.getDataAsFloatArray()
                    val mResults: ArrayList<Yolo5PostProcess.Result> =
                        Yolo5PostProcess.postProcess(
                            outputData,
                            bitmap.width,
                            bitmap.height,
                            imageView.width,
                            imageView.height,
                            PrePostProcessor.mInputWidth,
                            PrePostProcessor.mInputHeight)
                    for (result in mResults) {
                        rectResult.add(
                            Yolo5PostProcess.ResultRect(
                                result.classIndex,
                                result.score,
                                result.rect.left,
                                result.rect.top,
                                result.rect.right,
                                result.rect.bottom
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return rectResult
    }

   /*
    fun run(
        floatArray: List<Float>,
        bitmap:Yolo5PostProcess.Size

    ): ArrayList<Yolo5PostProcess.ResultRect>? {
        val rectResult = ArrayList<Yolo5PostProcess.ResultRect>()
        println("localModulePath $localModulePath")
        plmScoped {
            val inputTensor: Tensor = Tensor.fromBlob(
                data = floatArray.toFloatArray(),
                shape = longArrayOf(
                    1,
                    3,
                    PrePostProcessor.mInputWidth.toLong(),
                    PrePostProcessor.mInputHeight.toLong()
                ),
                scope = this
            )
            val inputIValue = IValue.from(inputTensor)
            val output: List<IValue> = module.forward(inputIValue).toTuple()
            try {
                val outputTuple = output[0].toTensor()

                outputTuple.let {
                    val outputData = it.getDataAsFloatArray()
                    val mResults: ArrayList<Yolo5PostProcess.Result> =
                        Yolo5PostProcess.postProcess(
                            outputData,
                            bitmap

                        )
                    for (result in mResults) {
                        rectResult.add(
                            Yolo5PostProcess.ResultRect(
                                result.classIndex,
                                result.score,
                                result.rect.left,
                                result.rect.top,
                                result.rect.right,
                                result.rect.bottom
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        /*
        var outputTensor = outputTuple.elements()[0].toTensor()

        var floatBuffer = outputTensor.data_ptr

        var results: [AnyHashable] = []
        for i in 0..<output_size {
            results.append(NSNumber(value: floatBuffer[i]))
        }
        return results
         */

        return rectResult
    }

    */

}


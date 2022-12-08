package de.voize.example.plm



import com.soywiz.kds.atomic.kdsFreeze
import com.soywiz.korim.tiles.tiled.TiledMap
import com.soywiz.korio.lang.format
import com.soywiz.korma.geom.RectangleInt
import kotlin.math.max
import kotlin.math.min

object Yolo5PostProcess {

    class Result(var classIndex: Int, var score: Float, var rect: RectangleInt)
    class ResultRect(var classIndex: Int, var score: Float, var left: Int, var top:Int,var right: Int, var bottom:Int)
    class Size{
        var height:Float
        var width:Float
        constructor( height: Float, width: Float) {
            this.height = height;
            this.width = width
            // Not a super call, thus freeze the object

           kdsFreeze(this)
        }


    }
    class InputParameters(var bitmap:Size?, var imageview:Size?,var yolo:Size?)

    // model output is of size 25200*(num_of_class+5)
    private const val mOutputRow =
        25200 // as decided by the YOLOv5 model for input image of size 640*640
    private const val mOutputColumn =
        85 // left, top, right, bottom, score and 80 class probability
    private const val mThreshold =  0.40f // score above which a detection is generated
    private const val mNmsLimit = 15


    fun postProcess(outputs: FloatArray, bitmapWidth: Int, bitmapHeight: Int,inputWidth:Int, inputHeight: Int): ArrayList<Result> {

        val imgScaleX = bitmapWidth.toFloat() / inputWidth
        val imgScaleY = bitmapHeight.toFloat() / inputHeight
        val ivScaleX = 1f //mResultView.getWidth() / bitmap.getWidth();
        val ivScaleY = 1f //mResultView.getHeight() / bitmap.getHeight();
        val results = outputsToNMSPredictions(outputs, imgScaleX, imgScaleY, ivScaleX, ivScaleY, 0f, 0f)
        return results

    }

    fun postProcess(outputs: FloatArray, bitmapWidth: Int, bitmapHeight: Int,imageViewWidth:Int,imageViewHeight:Int,mInputWidth:Int, mInputHeight: Int): ArrayList<Result> {
        val mImgScaleX = bitmapWidth.toFloat() / mInputWidth
        val mImgScaleY = bitmapHeight.toFloat() / mInputHeight

        val mIvScaleX =
            if (bitmapWidth > bitmapHeight) imageViewWidth.toFloat() / bitmapWidth else imageViewHeight.toFloat() / bitmapHeight

        val mIvScaleY =
            if (bitmapHeight > bitmapWidth) imageViewHeight.toFloat() / bitmapHeight else imageViewWidth.toFloat() / bitmapWidth

        val mStartX = (imageViewWidth - mIvScaleX * bitmapWidth) / 2
        val mStartY = (imageViewHeight - mIvScaleY * bitmapHeight) / 2

        return outputsToNMSPredictions(outputs, mImgScaleX, mImgScaleY, mIvScaleX, mIvScaleY, mStartX, mStartY)
    }
    fun postProcess(outputs: FloatArray, bitmapWidth: Float, bitmapHeight: Float,imageViewWidth:Float,imageViewHeight:Float,mInputWidth:Int, mInputHeight: Int): ArrayList<Result> {
        val mImgScaleX = bitmapWidth / mInputWidth
        val mImgScaleY = bitmapHeight / mInputHeight

        val mIvScaleX =
            if (bitmapWidth > bitmapHeight) imageViewWidth/ bitmapWidth else imageViewHeight / bitmapHeight

        val mIvScaleY =
            if (bitmapHeight > bitmapWidth) imageViewHeight / bitmapHeight else imageViewWidth / bitmapWidth

        val mStartX = (imageViewWidth - mIvScaleX * bitmapWidth) / 2
        val mStartY = (imageViewHeight - mIvScaleY * bitmapHeight) / 2

        return outputsToNMSPredictions(outputs, mImgScaleX, mImgScaleY, mIvScaleX, mIvScaleY, mStartX, mStartY)
    }

    fun postProcess(outputs: FloatArray,bitmap:Size): ArrayList<Result> {
        val mInputWidth =   PrePostProcessor.mInputWidth.toFloat()
        val mInputHeight =   PrePostProcessor.mInputHeight.toFloat()
      //  let ivScaleX : Double =  Double(strongSelf.imageViewLive.frame.size.width / CGFloat(PrePostProcessor.inputWidth))
        val ivScaleX = bitmap.width / mInputWidth
        //let ivScaleY : Double = Double(strongSelf.imageViewLive.frame.size.height / CGFloat(PrePostProcessor.inputHeight))
        val ivScaleY = bitmap.height/ mInputHeight
       // let startX = Double((strongSelf.imageViewLive.frame.size.width - CGFloat(ivScaleX) * CGFloat(PrePostProcessor.inputWidth))/2)
       val  startX = (bitmap.width - ivScaleX * mInputWidth) / 2
        // let startY = Double((strongSelf.imageViewLive.frame.size.height -  CGFloat(ivScaleY) * CGFloat(PrePostProcessor.inputHeight))/2)
        val  startY = (bitmap.height - ivScaleY * mInputHeight) / 2

        return outputsToNMSPredictions(outputs, 1f, 1f,  ivScaleX, ivScaleY, startX,startY)
    }

    // The two methods nonMaxSuppression and IOU below are ported from https://github.com/hollance/YOLO-CoreML-MPSNNGraph/blob/master/Common/Helpers.swift
    /**
     * Removes bounding boxes that overlap too much with other boxes that have
     * a higher score.
     * - Parameters:
     * - boxes: an array of bounding boxes and their scores
     * - limit: the maximum number of boxes that will be selected
     * - threshold: used to decide whether boxes overlap too much
     */
    private fun nonMaxSuppression(
        boxes: ArrayList<Result>,
        limit: Int,
        threshold: Float
    ): ArrayList<Result> {

        // Do an argsort on the confidence scores, from high to low.
        boxes.sortWith({ o1, o2 -> o1.score.compareTo(o2.score) })
        val selected = ArrayList<Result>()
        val active = BooleanArray(boxes.size)
        active.fill(true)
//        Arrays.fill(active, true)
        var numActive = active.size

        // The algorithm is simple: Start with the box that has the highest score.
        // Remove any remaining boxes that overlap it more than the given threshold
        // amount. If there are any boxes left (i.e. these did not overlap with any
        // previous boxes), then repeat this procedure, until no more boxes remain
        // or the limit has been reached.
        var done = false
        var i = 0
        while (i < boxes.size && !done) {
            if (active[i]) {
                val boxA = boxes[i]
                selected.add(boxA)
                if (selected.size >= limit) break
                for (j in i + 1 until boxes.size) {
                    if (active[j]) {
                        val boxB = boxes[j]
                        if (IOU(boxA.rect, boxB.rect) > threshold) {
                            active[j] = false
                            numActive -= 1
                            if (numActive <= 0) {
                                done = true
                                break
                            }
                        }
                    }
                }
            }
            i++
        }

        return selected
    }

    /**
     * Computes intersection-over-union overlap between two bounding boxes.
     */
    private fun IOU(a: RectangleInt, b: RectangleInt): Float {
        val areaA = ((a.right - a.left) * (a.bottom - a.top)).toFloat()
        if (areaA <= 0.0) return 0.0f
        val areaB = ((b.right - b.left) * (b.bottom - b.top)).toFloat()
        if (areaB <= 0.0) return 0.0f
        val intersectionMinX = max(a.left, b.left).toFloat()
        val intersectionMinY = max(a.top, b.top).toFloat()
        val intersectionMaxX = min(a.right, b.right).toFloat()
        val intersectionMaxY = min(a.bottom, b.bottom).toFloat()
        val intersectionArea = max(intersectionMaxY - intersectionMinY, 0f) *
                max(intersectionMaxX - intersectionMinX, 0f)
        return intersectionArea / (areaA + areaB - intersectionArea)
    }

    private fun outputsToNMSPredictions(
        outputs: FloatArray,
        imgScaleX: Float,
        imgScaleY: Float,
        ivScaleX: Float,
        ivScaleY: Float,
        startX: Float,
        startY: Float
    ): ArrayList<Result> {
        val outputLength = outputs.size
        val results = ArrayList<Result>()
        val outputAsString = StringBuilder()
        outputAsString.append("Model outputsToNMSPredictions \n")
        for (i in 0 until mOutputRow) {
            val start_position = i * mOutputColumn + 4
            if (start_position < outputLength &&
                outputs[start_position] > mThreshold
            ) {
                val x = outputs[i * mOutputColumn]
                val y = outputs[i * mOutputColumn + 1]
                val w = outputs[i * mOutputColumn + 2]
                val h = outputs[i * mOutputColumn + 3]
                val left = imgScaleX * (x - w / 2)
                val top = imgScaleY * (y - h / 2)
                val right = imgScaleX * (x + w / 2)
                val bottom = imgScaleY * (y + h / 2)
                outputAsString.append(
//
                    "score: %f %f %f %f %f left, top, right, bottom \n".format(
                        outputs[start_position],
                        left,
                        top,
                        right,
                        bottom
                    )
                )
                var max = outputs[i * mOutputColumn + 5]
                var cls = 0
                for (j in 0 until mOutputColumn - 5) {
                    if (outputs[i * mOutputColumn + 5 + j] > max) {
                        max = outputs[i * mOutputColumn + 5 + j]
                        cls = j
                    }
                }

                // Rect rect = new Rect((int)(startX+ivScaleX*left), (int)(startY+top*ivScaleY), (int)(startX+ivScaleX*right), (int)(startY+ivScaleY*bottom));

                val rect = RectangleInt.fromBounds(
                    (startX + ivScaleX * left).toInt(),
                    (startY + top * ivScaleY).toInt(),
                    (startX + ivScaleX * right).toInt(),
                    (startY + ivScaleY * bottom).toInt()
                )
                val result = Result(
                    cls,
                    outputs[i * mOutputColumn + 4], rect
                )
                results.add(result)
            }
        }

//        MultiLogger.getInstance().file(outputAsString.toString());
        return nonMaxSuppression(results, mNmsLimit, mThreshold)
    }
}
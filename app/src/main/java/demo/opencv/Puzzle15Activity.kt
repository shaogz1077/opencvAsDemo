package demo.opencv

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.*
import org.opencv.android.*
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener
import org.opencv.core.Mat

@SuppressLint("LongLogTag")
class Puzzle15Activity : Activity(), CvCameraViewListener, View.OnTouchListener {

    private var mOpenCvCameraView: CameraBridgeViewBase? = null
    private var mPuzzle15: Puzzle15Processor? = null
    private var mItemHideNumbers: MenuItem? = null
    private var mItemStartNewGame: MenuItem? = null


    private var mGameWidth: Int = 0
    private var mGameHeight: Int = 0

    private val mLoaderCallback = object : BaseLoaderCallback(this) {


        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")

                    /* Now enable camera view to start receiving frames */
                    mOpenCvCameraView!!.setOnTouchListener(this@Puzzle15Activity)
                    mOpenCvCameraView!!.enableView()
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        Log.d(TAG, "Creating and setting view")
        mOpenCvCameraView = JavaCameraView(this, -1)
        setContentView(mOpenCvCameraView)
        mOpenCvCameraView!!.visibility = CameraBridgeViewBase.VISIBLE
        mOpenCvCameraView!!.setCvCameraViewListener(this)
        mPuzzle15 = Puzzle15Processor()
        mPuzzle15!!.prepareNewGame()
    }

    public override fun onPause() {
        super.onPause()
        if (mOpenCvCameraView != null)
            mOpenCvCameraView!!.disableView()
    }

    public override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback)
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (mOpenCvCameraView != null)
            mOpenCvCameraView!!.disableView()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.i(TAG, "called onCreateOptionsMenu")
        mItemHideNumbers = menu.add("Show/hide tile numbers")
        mItemStartNewGame = menu.add("Start new game")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i(TAG, "Menu Item selected $item")
        if (item === mItemStartNewGame) {
            /* We need to start new game */
            mPuzzle15!!.prepareNewGame()
        } else if (item === mItemHideNumbers) {
            /* We need to enable or disable drawing of the tile numbers */
            mPuzzle15!!.toggleTileNumbers()
        }
        return true
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        mGameWidth = width
        mGameHeight = height
        mPuzzle15!!.prepareGameSize(width, height)
    }

    override fun onCameraViewStopped() {}

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        var xpos: Int
        var ypos: Int

        xpos = (view.width - mGameWidth) / 2
        xpos = event.x.toInt() - xpos

        ypos = (view.height - mGameHeight) / 2
        ypos = event.y.toInt() - ypos

        if (xpos >= 0 && xpos <= mGameWidth && ypos >= 0 && ypos <= mGameHeight) {
            /* click is inside the picture. Deliver this event to processor */
            mPuzzle15!!.deliverTouchEvent(xpos, ypos)
        }

        return false
    }

    override fun onCameraFrame(inputFrame: Mat): Mat {
        return mPuzzle15!!.puzzleFrame(inputFrame)
    }

    companion object {

        private val TAG = "Sample::Puzzle15::Activity"
    }
}

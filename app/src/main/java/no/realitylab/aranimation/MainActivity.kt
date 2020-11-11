package no.realitylab.aranimation

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AlertDialog
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.SkeletonNode
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var arFragment: ArFragment
    private lateinit var model: Uri
    private var renderable: ModelRenderable? = null
    private var animator: ModelAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = sceneform_fragment as ArFragment
        model = Uri.parse("nathan.sfb")

        arFragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->
            if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) {
                return@setOnTapArPlaneListener
            }
            val anchor = hitResult.createAnchor()
            placeObject(arFragment, anchor, model)

            animateModel("rp_nathan_animated_003_walking")

        }
    }

    private fun animateModel(name: String) {
        animator?.let { it ->
            if (it.isRunning) {
                it.end()
            }
        }
        renderable?.let { modelRenderable ->
            val data = modelRenderable.getAnimationData(0)
            val animator = ModelAnimator(data, modelRenderable)
            animator.setRepeatCount(1000)
            animator?.start()
        }
    }

    private fun placeObject(fragment: ArFragment, anchor: Anchor, model: Uri) {
        ModelRenderable.builder()
            .setSource(fragment.context, model)
            .build()
            .thenAccept {
                renderable = it
                addToScene(fragment, anchor, it)
            }
            .exceptionally {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(it.message).setTitle("Error")
                val dialog = builder.create()
                dialog.show()
                return@exceptionally null
            }
    }

    private fun addToScene(fragment: ArFragment, anchor: Anchor, renderable: Renderable) {
        val anchorNode = AnchorNode(anchor)

        val skeletonNode = SkeletonNode()
        skeletonNode.renderable = renderable
        skeletonNode.localRotation = Quaternion.axisAngle(
            Vector3(0.0f, 1.0f, 0.0f),
            -180f
        )

        val node = TransformableNode(fragment.transformationSystem)
        node.addChild(skeletonNode)
        node.setParent(anchorNode)
        node.rotationController.rotationRateDegrees = 180f
        fragment.arSceneView.scene.addChild(anchorNode)
    }

    fun createCubeNode(modelObject: ModelRenderable): Node {
        val cubeNode =  Node().apply {
            renderable = modelObject
            localPosition = Vector3(0.0f, 0.15f, 0.0f)
        }

        return cubeNode
    }
}

package no.realitylab.aranimation

import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.sceneform.Camera
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.SkeletonNode
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var arFragment: ArFragment
    private lateinit var model: Uri
    private var renderable: ModelRenderable? = null
    private var animator: ModelAnimator? = null
    private var bulletRenderable: ModelRenderable? = null
    private var point: Point? = null
    private var nathansLeft = 8
    private var nathansLeftTxt: TextView? = null
    private var shoot: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val display = windowManager.defaultDisplay

        point = Point()
        display.getRealSize(point)
        nathansLeftTxt = findViewById(R.id.nathansCntTxt)
        shoot = findViewById(R.id.shootButton)
        var shoot: Button = findViewById(R.id.shootButton)
        shoot.setOnClickListener {
            shoot()
        }
        arFragment = sceneform_fragment as ArFragment
        model = Uri.parse("nathan.sfb")
        buildBulletModel()


        for (i in 0..7) {
            placeObject(arFragment, i, model)
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

    private fun placeObject(fragment: ArFragment, i: Int, model: Uri) {
        ModelRenderable.builder()
            .setSource(fragment.context, model)
            .build()
            .thenAccept {
                renderable = it
                addToScene(fragment, i, it)
            }
            .exceptionally {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(it.message).setTitle("Error")
                val dialog = builder.create()
                dialog.show()
                return@exceptionally null
            }
    }

    private fun buildBulletModel() {
        Texture
            .builder()
            .setSource(this, R.drawable.texture)
            .build()
            .thenAccept { texture ->
                MaterialFactory
                    .makeOpaqueWithTexture(this, texture)
                    .thenAccept { material ->

                        bulletRenderable = ShapeFactory
                            .makeSphere(
                                0.01f,
                                Vector3(0f, 0f, 0f),
                                material
                            )

                    }


            }
    }

    private fun shoot() {
        var camera: Camera? = null

        camera = arFragment.arSceneView.scene.camera
        val scene = arFragment.arSceneView.scene
        val ray = camera.screenPointToRay((point?.x ?: 1) / 2f, (point?.y ?: 1) / 2f)
        val node = Node()
        node.renderable = bulletRenderable
        arFragment.arSceneView.scene.addChild(node)

        Thread {

            for (i in 0..199) {

                runOnUiThread {

                    val vector3 = ray.getPoint(i * 0.1f)
                    node.worldPosition = vector3

                    val nodeInContact = scene.overlapTest(node)

                    if (nodeInContact != null) {
                        nathansLeft--
                        nathansLeftTxt?.setText("Nathans Left: $nathansLeft")
                        scene.removeChild(nodeInContact)
                    }

                }

                try {
                    Thread.sleep(10)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }

            runOnUiThread { scene.removeChild(node) }

        }.start()

    }

    private fun addToScene(fragment: ArFragment, i: Int, renderable: Renderable) {
        var x = 0f
        var z = 0f
        var rotationRateDegrees = (4 + i) * -45f
        when (i) {
            0 -> {
                x = 0f
                z = -10f
//                rotationRateDegrees = 4 * 45f

            }
            1 -> {
                x = 10f
                z = -10f
//                rotationRateDegrees = 5 * 45f - 180f

            }
            2 -> {
                x = 10f
                z = 0f
//                rotationRateDegrees = 6 * 45f - 180f

            }
            3 -> {
                x = 10f
                z = 10f
//                rotationRateDegrees = 7 * 45f - 180f

            }
            4 -> {
                x = 0f
                z = 10f
//                rotationRateDegrees = 0f - 180f

            }
            5 -> {
                x = -10f
                z = 10f
//                rotationRateDegrees = 1 * 45f - 180f

            }
            6 -> {
                x = -10f
                z = 0f
//                rotationRateDegrees = 2 * 45f - 180f

            }
            7 -> {
                x = -10f
                z = -10f
//                rotationRateDegrees = 3 * 45f - 180f

            }
        }

        val skeletonNode = SkeletonNode()
        skeletonNode.renderable = renderable
        skeletonNode.localRotation = Quaternion.axisAngle(
            Vector3(0.0f, 1.0f, 0.0f),
            rotationRateDegrees
        )

        val node = TransformableNode(fragment.transformationSystem)
        node.addChild(skeletonNode)

        val start_vector = Vector3(x, -1.5f, z)
        node.worldPosition = start_vector
        fragment.arSceneView.scene.addChild(node)
        animateModel("rp_nathan_animated_003_walking")
//        val ray = Ray(start_vector, Vector3(0f, -1.5f, 0f))
//
//        Thread {
//            for (i in 0..199) {
//                runOnUiThread {
//                    val vector3 = ray.getPoint(i * 0.1f)
//                    node.worldPosition = vector3
//                }
//                try {
//                    Thread.sleep(10)
//                } catch (e: InterruptedException) {
//                    e.printStackTrace()
//                }
//
//            }
//
//        }.start()
    }
}

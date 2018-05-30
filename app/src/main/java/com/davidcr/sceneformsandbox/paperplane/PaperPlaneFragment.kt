package com.davidcr.sceneformsandbox.paperplane

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.davidcr.sceneformsandbox.R
import com.davidcr.sceneformsandbox.utils.DemoUtils
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableException
import com.google.ar.sceneform.ArSceneView

class PaperPlaneFragment : Fragment() {


    private var installRequested = false
    private var arScene: ArSceneView? = null

    companion object {
        fun newInstance() = PaperPlaneFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.paper_plane_fragment, container, false)
        arScene = root.findViewById(R.id.ar_scene_view)
        return root
    }

    override fun onResume() {
        super.onResume()
        if (arScene?.session == null) {
            try {
                val session = DemoUtils.createArSession(activity, installRequested)
                if (session == null) {
                    installRequested = DemoUtils.hasCameraPermission(activity)
                    return
                } else {
                    arScene?.setupSession(session)
                }
            } catch (e: UnavailableException) {
                DemoUtils.handleSessionException(activity, e)
            }
        }

        try {
            arScene?.resume()
        } catch (ex: CameraNotAvailableException) {
            DemoUtils.displayError(activity?.baseContext, "Unable to get camera", ex)
            return
        }

    }

    override fun onPause() {
        super.onPause()
        arScene?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        arScene?.destroy()
    }
}
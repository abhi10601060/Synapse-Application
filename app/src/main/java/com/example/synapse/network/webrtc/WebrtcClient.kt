package com.example.synapse.network.webrtc

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnection.Observer
import org.webrtc.PeerConnectionFactory
import org.webrtc.ScreenCapturerAndroid
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoCapturer
import org.webrtc.VideoTrack
import javax.inject.Inject

class WebrtcClient (
    private val context : Context
) {

    private val TAG = "WEBRTC_CLIENT"
    private val LOCAL_TRACK_ID="local_track"
    private val lOCAL_STREAM_ID="local_stream"

    // Initialize manually
    private lateinit var  surfaceView : SurfaceViewRenderer
    private lateinit var observer : Observer

    private var streamerConnection : PeerConnection? = null
    private val viewersConnections : ArrayList<PeerConnection?> = ArrayList()

    private val peerConnectionFactory by lazy { createPeerConnectionFactory() }
    private val eglBaseContext = EglBase.create().eglBaseContext
    private val mediaConstraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
    }
    private val iceServer = listOf(
        PeerConnection.IceServer(
            "turn:openrelay.metered.ca:443?transport=tcp", "openrelayproject", "openrelayproject"
        )
    )

    // for screenCapture
    private var screenCapturer : VideoCapturer? = null
    private val localVideoSource by lazy { peerConnectionFactory.createVideoSource(false) }
    private var localVideoTrack: VideoTrack?=null
    private var localStream: MediaStream?=null

    init {
        initializePeerConnectionFactoryOptions()
    }

    fun startScreenCaptureStream(permissionIntent : Intent){
        val displayMatrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMatrics)

        val screenWidthPixels = displayMatrics.widthPixels
        val screenHeightPixels = displayMatrics.heightPixels

        val surfaceTextureHelper = SurfaceTextureHelper.create(
            Thread.currentThread().name, eglBaseContext
        )

        screenCapturer = createScreenCapturer(permissionIntent)
        screenCapturer!!.initialize(surfaceTextureHelper, context, localVideoSource.capturerObserver)
        screenCapturer!!.startCapture(screenWidthPixels, screenHeightPixels, 20)

        localVideoTrack = peerConnectionFactory.createVideoTrack(LOCAL_TRACK_ID+"_video", localVideoSource)
        localVideoTrack?.addSink(surfaceView)
        localStream = peerConnectionFactory.createLocalMediaStream(lOCAL_STREAM_ID)
        localStream?.addTrack(localVideoTrack)
    }

    private fun createScreenCapturer(permissionIntent: Intent): VideoCapturer{
        return ScreenCapturerAndroid(permissionIntent, object : MediaProjection.Callback(){
            override fun onStop() {
                super.onStop()
                Log.d(TAG, "onStop: stopped screen capture stream")
            }
        })
    }

    private fun initializePeerConnectionFactoryOptions(){
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true).setFieldTrials("WebRTC-H264HighProfile/Enabled/")
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    private fun createPeerConnectionFactory() : PeerConnectionFactory{
        return PeerConnectionFactory.builder()
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBaseContext))
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBaseContext, true, true))
            .setOptions(
                PeerConnectionFactory.Options().apply {
                    disableEncryption = false
                    disableNetworkMonitor = false
                }
            ).createPeerConnectionFactory()
    }

    private fun createPeerConnection() : PeerConnection? {
        return peerConnectionFactory.createPeerConnection(iceServer, observer)
    }

    private fun createOffer(listener : (SessionDescription) -> Unit){
        streamerConnection?.createOffer(object : MySdpObserver(){
            override fun onCreateSuccess(sdp: SessionDescription?) {
                super.onCreateSuccess(sdp)
                setLocalDescription(streamerConnection, sdp)
            }
        }, mediaConstraints)
    }

    private fun createAnswer(peerConnection: PeerConnection?, listener: (answer : SessionDescription) -> Unit){
        peerConnection?.createAnswer(object : MySdpObserver(){
            override fun onCreateSuccess(sdp: SessionDescription?) {
                super.onCreateSuccess(sdp)
                setLocalDescription(peerConnection, sdp)
                listener(sdp!!)
            }
        }, mediaConstraints)
    }

    private fun setLocalDescription(peerConnection : PeerConnection?, sdp : SessionDescription?){
        try {
            peerConnection?.setLocalDescription(object  : MySdpObserver(){
                override fun onSetSuccess() {
                    super.onSetSuccess()
                    Log.d(TAG, "setLocalDescription, onSetSuccess: ")
                }
            }, sdp)
        }
        catch (e : Exception){
            Log.d(TAG, "setLocalDescription: error : ${e.message}")
        }
    }

    private fun setRemoteDescription(peerConnection : PeerConnection?, sdp : SessionDescription?){
        try {
            peerConnection?.setRemoteDescription(object  : MySdpObserver(){
                override fun onSetSuccess() {
                    super.onSetSuccess()
                    Log.d(TAG, "setRemoteDescription, onSetSuccess: ")
                }
            }, sdp)
        }
        catch (e : Exception){
            Log.d(TAG, "setRemoteDescription: error : ${e.message}")
        }
    }

    private fun addStreamerConnectionToWatch(answerSdp : SessionDescription){
        setRemoteDescription(streamerConnection, answerSdp)
    }

    private fun addViewerToStream(offerSdp: SessionDescription, listener: (answer : SessionDescription) -> Unit) {
        val peerConnection = createPeerConnection()
        setRemoteDescription(peerConnection, offerSdp)
        peerConnection?.addStream(localStream)
        createAnswer(peerConnection, listener)
        viewersConnections.add(peerConnection)
    }

    fun useOffer(offer : SessionDescription, answerListener : (answer : SessionDescription) -> Unit){
        addViewerToStream(offer, answerListener)
    }

    fun useAnswer(answer: SessionDescription){
        addStreamerConnectionToWatch(answer)
    }

    fun addIceCandidate(iceCandidate: IceCandidate){
        streamerConnection?.addIceCandidate(iceCandidate)
    }

    fun sendIceCandidate(candidate: IceCandidate){
        addIceCandidate(candidate)
    }

    fun closeWatchConnection(){
        try {
            streamerConnection?.close()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun closeStreamConnections(){
        try {
            screenCapturer?.stopCapture()
            screenCapturer?.dispose()
            localStream?.dispose()
            if (viewersConnections != null) {
                for(peerConnection in viewersConnections)
                    peerConnection?.close()
            }
        }catch (e:Exception) {
            e.printStackTrace()
        }
    }
}
package com.example.synapse.network.webrtc

import android.util.Log
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnection.Observer
import org.webrtc.RtpReceiver

open class MyPeerObserver(private val user : String) : Observer {
    
    val TAG = "MY_PEER_OBSERVER"
    
    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
        Log.d(TAG, "onSignalingChange: ")
    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
        Log.d(TAG, "onIceConnectionChange: ")
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
        Log.d(TAG, "onIceConnectionReceivingChange: ")
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
        Log.d(TAG, "onIceGatheringChange: ")
    }

    override fun onIceCandidate(p0: IceCandidate?) {
        Log.d(TAG, "onIceCandidate: ")
    }

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
        Log.d(TAG, "onIceCandidatesRemoved: ")
    }

    override fun onAddStream(p0: MediaStream?) {
        Log.d(TAG, "onAddStream: ")
    }

    override fun onRemoveStream(p0: MediaStream?) {
        Log.d(TAG, "onRemoveStream: ")
    }

    override fun onDataChannel(p0: DataChannel?) {
        Log.d(TAG, "onDataChannel: ")
    }

    override fun onRenegotiationNeeded() {
        Log.d(TAG, "onRenegotiationNeeded: ")
    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
        Log.d(TAG, "onAddTrack: ")
    }
}
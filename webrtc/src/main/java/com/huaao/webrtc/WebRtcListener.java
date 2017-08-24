package com.huaao.webrtc;

import org.webrtc.MediaStream;

/**
 * Implement this interface to be notified of events.
 */
public interface WebRtcListener {

    void onStatusChanged(String type, String message, int meetingType);

    void onLocalStream(MediaStream localStream);

    void onAddRemoteStream(MediaStream remoteStream, int endPoint);

    void onRemoveRemoteStream(int endPoint);

}

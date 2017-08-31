package com.huaao.webrtc;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.WindowManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import io.socket.client.Ack;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static android.content.Context.WINDOW_SERVICE;

public class WebRtcClient {
    //    private final static String TAG = WebRtcClient.class.getCanonicalName();
    private final static String TAG = "WebRTCLogInfo";
    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String AUDIO_CODEC_OPUS = "opus";
    private final static int MAX_PEER = 6;
    private boolean[] endPoints = new boolean[MAX_PEER];
    private PeerConnectionFactory factory;
    private HashMap<String, Peer> peers = new HashMap<>();
    private LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
    private PeerConnectionParameters pcParams;
    private MediaConstraints pcConstraints = new MediaConstraints();
    private MediaStream localMS;
    private VideoSource videoSource;
    private WebRtcListener mListener;
    private Socket socket;
    private static WebRtcClient instance;
    private AudioSource audioSource;
    private String uid;
    private HashMap<String, ArrayList<JSONObject>> remoteIceEvents = new HashMap<>();

    public void setWebRtcListener(WebRtcListener mListener) {
        this.mListener = mListener;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
        MessageHandler messageHandler = new MessageHandler();
        socket.on("message", messageHandler.onMessage);
    }

    public Socket getSocket() {
        return socket;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    private WebRtcClient() {
    }

    public static WebRtcClient getInstance() {
        if (instance == null) {
            synchronized (WebRtcClient.class) {
                if (instance == null) {
                    instance = new WebRtcClient();
                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        initDisplay(context);

        iceServers.clear();
        iceServers.add(new PeerConnection.IceServer("turn:101.37.29.65:3478", "huaaotech", "poiu0987)(*&"));
//        iceServers.add(new PeerConnection.IceServer("stun:23.21.150.121"));
//        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));


        pcConstraints.mandatory.clear();
        pcConstraints.optional.clear();
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        pcConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
    }

    public void initDisplay(Context context) {
        if (factory != null) {
            return;
        }
        Point displaySize = new Point();
        WindowManager wm = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        wm.getDefaultDisplay().getSize(displaySize);
        pcParams = new PeerConnectionParameters(
                true, false, displaySize.x, displaySize.y, 30, 1, VIDEO_CODEC_VP9, true, 1,
                AUDIO_CODEC_OPUS, true);

        PeerConnectionFactory.initializeAndroidGlobals(context, true, true,
                pcParams.isVideoCodecHwAcceleration());
        factory = new PeerConnectionFactory();

    }

    private class MessageHandler {
        private HashMap<String, Command> commandMap;

        private MessageHandler() {
            this.commandMap = new HashMap<>();
            commandMap.put("join", new CreateOfferCommand());
            commandMap.put("offer", new CreateAnswerCommand());
            commandMap.put("answer", new SetRemoteSDPCommand());
            commandMap.put("candidate", new AddIceCandidateCommand());
            commandMap.put("bye", new RemoteHangupCommand());
        }

        private Emitter.Listener onMessage = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    JSONObject payload = new JSONObject(args[0].toString());
                    String fromId = payload.optString("userId");
                    String type = payload.optString("type");
                    String message = payload.optString("message");
                    int meetingType = payload.optInt("meetingType");

                    Log.d(TAG, "userId：" + fromId + "，type：" + type + "，message：" + message);
                    //1.加入会议为newpeer；2.拒接时为refuse；3.挂断时为bye；4.无应答时为noresponse
                    if (mListener != null) {
                        mListener.onStatusChanged(type, message, meetingType);
                    }
                    if ("newpeer".equals(type)) {
                        if (!peers.containsKey(fromId)) {
                            // if MAX_PEER is reach, ignore the call
                            int endPoint = findEndPoint();
                            if (endPoint != MAX_PEER) {
                                addPeer(fromId, endPoint, false);
                            }
                        }
                    }
                    if (commandMap.get(type) != null) {
                        commandMap.get(type).execute(fromId, payload);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private class CreateOfferCommand implements Command {
        public void execute(String peerId, JSONObject payload) throws JSONException {
            Log.d(TAG, "CreateOfferCommand" + " - " + "peerId：" + peerId);
            Peer peer = peers.get(peerId);
            peer.createOffer();
        }
    }

    private class CreateAnswerCommand implements Command {
        public void execute(String peerId, JSONObject payload) throws JSONException {
            Peer peer = peers.get(peerId);
//            Log.d(TAG, "CreateAnswerCommand：" + pcConstraints + "，peerId：" + peerId + "，state:" + peer.pc.signalingState().toString());
            SessionDescription sdp = new SessionDescription(
                    SessionDescription.Type.fromCanonicalForm(payload.getString("type")),
                    payload.getString("sdp")
            );
            peer.setRemoteDescription(sdp);
            peer.createAnswer();
        }
    }

    private class SetRemoteSDPCommand implements Command {
        public void execute(String peerId, JSONObject payload) throws JSONException {
            Peer peer = peers.get(peerId);
//            Log.d(TAG, "SetRemoteSDPCommand：" + "，peerId：" + peerId + "，state " + peer.pc.signalingState().toString());
            SessionDescription sdp = new SessionDescription(
                    SessionDescription.Type.fromCanonicalForm(payload.getString("type")),
                    payload.getString("sdp")
            );
            peer.setRemoteDescription(sdp);
        }
    }

    private class AddIceCandidateCommand implements Command {
        public void execute(String peerId, JSONObject payload) throws JSONException {
            Peer peer = peers.get(peerId);
//            Log.d(TAG, "AddIceCandidateCommand：" + "，peerId：" + peerId + "，SigalingState " + peer.pc.signalingState().toString());
            if (peer.sdpReady) {
                IceCandidate candidate = new IceCandidate(
                        payload.getString("id"),
                        payload.getInt("label"),
                        payload.getString("candidate")
                );
                peer.pc.addIceCandidate(candidate);
            } else {
                cacheIceEvents(peerId, payload);
            }

        }
    }

    private class RemoteHangupCommand implements Command {
        public void execute(String peerId, JSONObject payload) throws JSONException {
            Peer peer = peers.get(peerId);
            Log.d(TAG, "RemoteHangupCommand：" + "，peerId：" + peerId);
            if (peer != null && peer.pc != null) {
                removePeer(peerId);
            }
        }
    }

    public void cacheIceEvents(String peerId, JSONObject payload) {
        Log.d(TAG, "cache ice events for wrong state：" + peerId);
        if (!remoteIceEvents.containsKey(peerId)) {
            remoteIceEvents.put(peerId, new ArrayList<JSONObject>());
        }
        remoteIceEvents.get(peerId).add(payload);
    }

    /**
     * Send a message through the signaling server
     *
     * @param to      id of recipient
     * @param type    type of message
     * @param payload payload of message
     * @throws JSONException
     */
    public void sendMessage(String to, String type, JSONObject payload) throws JSONException {
        if (payload == null) {
            payload = new JSONObject();
            payload.put("type", type);
        }
        payload.put("userId", uid);
        Log.d(TAG, "send message to server：" + payload.toString());

        if (type.equals("join")) {
            createOffer(payload);
        } else {
            payload.put("toUserId", to);
            socket.emit("webrtc", payload.toString());
        }
    }

    /**
     * -1: 不在线; 0:等待加入；1:已加入；2:已拒绝; 3:已退出; 4: 用户忙; 5:无应答
     */
    private void createOffer(JSONObject payload) {
        socket.emit("webrtc", payload.toString(), new Ack() {
            @Override
            public void call(Object... args) {
                if (args[0] == null) {
                    return;
                }
                Log.d(TAG, "join ack：" + args[0].toString());
                try {
                    JSONObject jsonObject = new JSONObject(args[0].toString());
                    JSONArray users = jsonObject.optJSONArray("users");
                    if (users != null && users.length() > 0) {
                        for (int i = 0; i < users.length(); i++) {
                            JSONObject user = (JSONObject) users.get(i);
                            int status = user.optInt("status");
                            String userId = user.optString("userId");
                            if (status == 1) {
                                int endPoint = findEndPoint();
                                if (endPoint != MAX_PEER) {
                                    Log.e(TAG, "uid：" + userId);
                                    Peer peer = addPeer(userId, endPoint, true);
                                    peer.createOffer();
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private class Peer implements SdpObserver, PeerConnection.Observer {
        private PeerConnection pc;
        private String id;
        private int endPoint;
        private boolean sendOffer;
        private boolean sdpReady;

        @Override
        public void onCreateSuccess(final SessionDescription sdp) {
            try {
                Log.d(TAG, "onCreateSuccess：" + "，id：" + id);
                JSONObject payload = new JSONObject();
                payload.put("type", sdp.type.canonicalForm());
                payload.put("sdp", sdp.description);
                sendMessage(id, sdp.type.canonicalForm(), payload);
                pc.setLocalDescription(Peer.this, sdp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSetSuccess() {
            Log.d(TAG, "onSetSuccess");
            if (sendOffer && pc.getRemoteDescription() != null) {
                sdpReady = true;
                drainCandidates();
            } else if (!sendOffer && pc.getLocalDescription() != null) {
                sdpReady = true;
                drainCandidates();
            }
        }

        public void drainCandidates() {
            ArrayList<JSONObject> events = remoteIceEvents.get(id);
            if (events == null) {
                return;
            }
            for (JSONObject payload : events) {
                try {
                    IceCandidate candidate = new IceCandidate(
                            payload.getString("id"),
                            payload.getInt("label"),
                            payload.getString("candidate")
                    );
                    pc.addIceCandidate(candidate);
                } catch (JSONException ex) {
                    Log.e(TAG, "execute ice candidate event error.");
                    ex.printStackTrace();
                }
            }
            remoteIceEvents.put(id, new ArrayList<JSONObject>());
        }

        @Override
        public void onCreateFailure(String s) {
            Log.d(TAG, "onCreateFailure：" + s);
        }

        @Override
        public void onSetFailure(String s) {
            Log.d(TAG, "onSetFailure：" + s);
        }

        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
            Log.d(TAG, "onSignalingChange：" + signalingState);
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            Log.d(TAG, "onIceConnectionChange：" + iceConnectionState);
            //if (iceConnectionState == PeerConnection.IceConnectionState.DISCONNECTED) {
            //    removePeer(id);
            //}
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {
            Log.d(TAG, "onIceConnectionReceivingChange：" + b);
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        }

        @Override
        public void onIceCandidate(final IceCandidate candidate) {
            Log.d(TAG, "onIceCandidate：" + candidate);
            try {
                JSONObject payload = new JSONObject();
                payload.put("label", candidate.sdpMLineIndex);
                payload.put("id", candidate.sdpMid);
                payload.put("candidate", candidate.sdp);
                payload.put("type", "candidate");
                sendMessage(id, "candidate", payload);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
            Log.d(TAG, "onAddStream：" + mediaStream.label() + "，endPoint：" + endPoint);
            // remote streams are displayed from 1 to MAX_PEER (0 is localStream)
            if (mListener != null) {
                mListener.onAddRemoteStream(mediaStream, endPoint);
            }
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {
            Log.d(TAG, "onRemoveStream：" + mediaStream.label());
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
            Log.d(TAG, "onDataChannel");
        }

        @Override
        public void onRenegotiationNeeded() {
            Log.d(TAG, "onRenegotiationNeeded");
        }

        public Peer(String id, int endPoint, boolean sendOffer) {
            Log.d(TAG, "new Peer: " + id + "，endPoint：" + endPoint + "，sendOffer: " + sendOffer);
            if (factory == null || localMS == null) {
                //initDisplay(mContext);
                return;
            }
            PeerConnection.RTCConfiguration rtcConfig =
                    new PeerConnection.RTCConfiguration(iceServers);
            // TCP candidates are only useful when connecting to a server that supports
            // ICE-TCP.
            rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
            rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
            rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
            rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
            // Use ECDSA encryption.
            rtcConfig.keyType = PeerConnection.KeyType.ECDSA;

            this.pc = factory.createPeerConnection(rtcConfig, pcConstraints, this);

            //this.pc = factory.createPeerConnection(iceServers, pcConstraints, this);
            this.id = id;
            this.endPoint = endPoint;
            this.sendOffer = sendOffer;
            this.sdpReady = false;
            pc.addStream(localMS); //, new MediaConstraints()
        }

        public void createAnswer() {
            pc.createAnswer(Peer.this, pcConstraints);
        }

        public void createOffer() {
            pc.createOffer(Peer.this, pcConstraints);
        }

        public void setRemoteDescription(SessionDescription sdp) {
            pc.setRemoteDescription(Peer.this, sdp);
        }

        public void setLocalDescription(SessionDescription sdp) {
            pc.setLocalDescription(Peer.this, sdp);
        }
    }

    private Peer addPeer(String id, int endPoint, boolean sendOffer) {
        Peer peer = new Peer(id, endPoint, sendOffer);
        peers.put(id, peer);
        endPoints[endPoint] = true;
        return peer;
    }

    private void removePeer(String id) {
        Log.d(TAG, "removePeer id：" + id);
        Peer peer = peers.get(id);
        if (mListener != null) {
            mListener.onRemoveRemoteStream(peer.endPoint);
        }
        peer.pc.removeStream(localMS);
        peer.pc.dispose();
        peer.pc = null;
        peers.remove(peer.id);
        endPoints[peer.endPoint] = false;
    }

    /**
     * Call this method in Activity.onPause()
     */
    public void onPause() {
        if (videoSource != null) videoSource.stop();
    }

    /**
     * Call this method in Activity.onResume()
     */
    public void onResume() {
        if (videoSource != null) videoSource.restart();
    }

    /**
     * Call this method in Activity.onDestroy()
     */
    public void onDestroy() {
        Log.d(TAG, "onDestroy：" + peers.size());
        int peercount = peers.size();
        for (Peer peer : peers.values()) {
            if (mListener != null) {
                mListener.onRemoveRemoteStream(peer.endPoint);
            }
            peer.pc.removeStream(localMS);
            Log.d(TAG, "onDestroy pc dispose：" + peer.id);
            peer.pc.dispose();
            endPoints[peer.endPoint] = false;
        }
        peers.clear();
        localMS.dispose();
        if (videoSource != null && peercount >= 0) videoSource.dispose();
        if (audioSource != null && peercount >= 0) audioSource.dispose();
        //You need to turn OFF and then disconnect  and then close it.
        factory.dispose();
        factory = null;
    }

    private int findEndPoint() {
        for (int i = 0; i < MAX_PEER; i++) if (!endPoints[i]) return i;
        return MAX_PEER;
    }

    public void setCamera() {
        Log.d(TAG, "setCamera");
        localMS = factory.createLocalMediaStream("ARDAMS");
        if (pcParams.isVideoCallEnabled()) {
            MediaConstraints videoConstraints = new MediaConstraints();
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight", Integer
                    .toString(pcParams.getVideoHeight())));
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth", Integer
                    .toString(pcParams.getVideoWidth())));
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxFrameRate",
                    Integer.toString(pcParams.getVideoFps())));
            videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minFrameRate",
                    Integer.toString(pcParams.getVideoFps())));

            videoSource = factory.createVideoSource(getVideoCapturer(), videoConstraints);
            localMS.addTrack(factory.createVideoTrack("ARDAMSv0", videoSource));
        }

        audioSource = factory.createAudioSource(new MediaConstraints());
        localMS.addTrack(factory.createAudioTrack("ARDAMSa0", audioSource));

        mListener.onLocalStream(localMS);
    }

    private VideoCapturer getVideoCapturer() {
        String frontCameraDeviceName = CameraEnumerationAndroid.getNameOfFrontFacingDevice();
        return VideoCapturerAndroid.create(frontCameraDeviceName);
    }
}
package com.huaao.webrtc;

public class PeerConnectionParameters {

    private boolean videoCallEnabled;
    private boolean loopback;
    private int videoWidth;
    private int videoHeight;
    private int videoFps;
    private int videoStartBitrate;
    private String videoCodec;
    private boolean videoCodecHwAcceleration;
    private int audioStartBitrate;
    private String audioCodec;
    private boolean cpuOveruseDetection;

    public PeerConnectionParameters(
            boolean videoCallEnabled, boolean loopback,
            int videoWidth, int videoHeight, int videoFps, int videoStartBitrate,
            String videoCodec, boolean videoCodecHwAcceleration,
            int audioStartBitrate, String audioCodec,
            boolean cpuOveruseDetection) {
        this.videoCallEnabled = videoCallEnabled;
        this.loopback = loopback;
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        this.videoFps = videoFps;
        this.videoStartBitrate = videoStartBitrate;
        this.videoCodec = videoCodec;
        this.videoCodecHwAcceleration = videoCodecHwAcceleration;
        this.audioStartBitrate = audioStartBitrate;
        this.audioCodec = audioCodec;
        this.cpuOveruseDetection = cpuOveruseDetection;
    }

    public boolean isVideoCallEnabled() {
        return videoCallEnabled;
    }

    public boolean isLoopback() {
        return loopback;
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public int getVideoFps() {
        return videoFps;
    }

    public int getVideoStartBitrate() {
        return videoStartBitrate;
    }

    public String getVideoCodec() {
        return videoCodec;
    }

    public boolean isVideoCodecHwAcceleration() {
        return videoCodecHwAcceleration;
    }

    public int getAudioStartBitrate() {
        return audioStartBitrate;
    }

    public String getAudioCodec() {
        return audioCodec;
    }

    public boolean isCpuOveruseDetection() {
        return cpuOveruseDetection;
    }
}
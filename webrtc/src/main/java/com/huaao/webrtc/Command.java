package com.huaao.webrtc;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author: xzp
 * Date: 2017/6/30
 */
interface Command {
    void execute(String peerId, JSONObject payload) throws JSONException;
}

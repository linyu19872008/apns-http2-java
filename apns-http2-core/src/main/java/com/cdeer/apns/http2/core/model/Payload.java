//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.cdeer.apns.http2.core.model;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Payload {

    private static final String APS = "aps";

    private Map<String, Object> params;

    private String alert;

    private Integer badge;

    private String sound = "default.caf";

    private Integer contentAvailable;

    private String alertBody;

    private String alertActionLocKey;

    private String alertLocKey;

    private String[] alertLocArgs;

    private String alertLaunchImage;

    public Payload() {
    }

    public static void main(String[] args) {
        Payload payload = new Payload();
        payload.setAlert("How are you?");
        payload.setBadge(Integer.valueOf(1));
        payload.setSound("a");
        payload.addParam("para1", "1231dfasfwer");
        payload.addParam("number", Long.valueOf(12312312312L));
        System.out.println(payload.toString());
    }

    public Map<String, Object> getParams() {
        return this.params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public void addParam(String key, Object obj) {
        if (this.params == null) {
            this.params = new HashMap();
        }

        if ("aps".equalsIgnoreCase(key)) {
            throw new IllegalArgumentException("the key can\'t be aps");
        } else {
            this.params.put(key, obj);
        }
    }

    public String getAlert() {
        return this.alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public Integer getBadge() {
        return this.badge;
    }

    public void setBadge(Integer badge) {
        this.badge = badge;
    }

    public String getSound() {
        return this.sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public String toString() {
        JSONObject object = new JSONObject();
        JSONObject apsObj = new JSONObject();
        if (this.getAlert() != null) {
            apsObj.put("alert", this.getAlert());
        } else if (this.getAlertBody() != null || this.getAlertLocKey() != null) {
            JSONObject alertObj = new JSONObject();
            this.putIntoJson("body", this.getAlertBody(), alertObj);
            this.putIntoJson("action-loc-key", this.getAlertActionLocKey(), alertObj);
            this.putIntoJson("loc-key", this.getAlertLocKey(), alertObj);
            this.putIntoJson("launch-image", this.getAlertLaunchImage(), alertObj);
            if (this.getAlertLocArgs() != null) {
                JSONArray e = new JSONArray();
                String[] var5 = this.getAlertLocArgs();
                int var6 = var5.length;

                for (int var7 = 0; var7 < var6; ++var7) {
                    String str = var5[var7];
                    e.add(str);
                }

                alertObj.put("loc-args", e);
            }

            apsObj.put("alert", alertObj);
        }

        if (this.getBadge() != null) {
            apsObj.put("badge", Integer.valueOf(this.getBadge().intValue()));
        }

        this.putIntoJson("sound", this.getSound(), apsObj);
        if (this.getContentAvailable() != null) {
            apsObj.put("content-available", Integer.valueOf(this.getContentAvailable().intValue()));
        }

        object.put("aps", apsObj);
        if (this.getParams() != null) {
            Iterator var9 = this.getParams().entrySet().iterator();

            while (var9.hasNext()) {
                Entry var10 = (Entry) var9.next();
                object.put(var10.getKey().toString(), var10.getValue());
            }
        }

        return object.toString();
    }

    private void putIntoJson(String key, String value, JSONObject obj) {
        if (value != null) {
            obj.put(key, value);
        }

    }

    public String getAlertBody() {
        return this.alertBody;
    }

    public void setAlertBody(String alertBody) {
        this.alertBody = alertBody;
    }

    public String getAlertActionLocKey() {
        return this.alertActionLocKey;
    }

    public void setAlertActionLocKey(String alertActionLocKey) {
        this.alertActionLocKey = alertActionLocKey;
    }

    public String getAlertLocKey() {
        return this.alertLocKey;
    }

    public void setAlertLocKey(String alertLocKey) {
        this.alertLocKey = alertLocKey;
    }

    public String getAlertLaunchImage() {
        return this.alertLaunchImage;
    }

    public void setAlertLaunchImage(String alertLaunchImage) {
        this.alertLaunchImage = alertLaunchImage;
    }

    public String[] getAlertLocArgs() {
        return this.alertLocArgs;
    }

    public void setAlertLocArgs(String[] alertLocArgs) {
        this.alertLocArgs = alertLocArgs;
    }

    public Integer getContentAvailable() {
        return this.contentAvailable;
    }

    public void setContentAvailable(Integer contentAvailable) {
        this.contentAvailable = contentAvailable;
    }
}

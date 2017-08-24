package com.huaao.webrtc.bean;

/**
 * Created by MingPeng on 2017/5/31.
 */

public class DispatcherBean {

    /**
     * id : 8a2b2a045a936287015a936b11cb0001
     * isOnline : 1
     * realname : 茅店派出所所长
     * phone : 13090000005
     * position : 114.50276,30.554556
     * distance : 39.362560440828545
     * jobsName : 所长
     * img : files/2017-04-13/1492063342708_1492063348.jpg
     * deptName : 茅店派出所
     */

    private String id;
    private int isOnline;
    private String realname;
    private String phone;
    private String position;
    private double distance;
    private String jobsName;
    private String img;
    private String deptName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(int isOnline) {
        this.isOnline = isOnline;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getJobsName() {
        return jobsName;
    }

    public void setJobsName(String jobsName) {
        this.jobsName = jobsName;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }
}

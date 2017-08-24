package com.huaao.webrtc.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author hz
 * @version 1.1
 */
public class CommonUtils {

    private static long lastClickTime;

    /**
     * 检测快速点击的工具
     *
     * @return
     */
    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 800) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    /**
     * 检测是否有相机权限
     *
     * @return
     */
    public static boolean cameraIsCanUse() {
        boolean isCanUse = true;
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
            Camera.Parameters mParameters = mCamera.getParameters(); //针对魅族手机
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            isCanUse = false;
        }

        if (mCamera != null) {
            try {
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
                return isCanUse;
            }
        }
        return isCanUse;
    }



    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * sp转px
     */
    public static int Sp2Px(Context context, int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context
                .getResources().getDisplayMetrics());
    }

    /**
     * px转sp
     */
    public static int Px2Sp(Context context, int px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, context
                .getResources().getDisplayMetrics());
    }

    /**
     * 判断姓名  中文 · 空格
     *
     * @param name
     * @return
     */
    public static boolean isName(String name) {
        String szNamePattern = "^([\\u4e00-\\u9fa5\\·\\u0020]{1,10})$";
        Pattern namePattern = Pattern.compile(szNamePattern);
        return namePattern.matcher(name).matches();
    }

    /**
     * 判断是否是手机号
     *
     * @param phoneNum
     * @return
     */
    public static boolean isPhoneNum(String phoneNum) {
        String szPhonePattern = "((^(13|14|15|17|18)[0-9]{9}$)|(^0[1,2]{1}\\d{1}-?\\d{8}$)|" + ""
                + "(^0[3-9]{1}\\d{2}-?\\d{7,8}$)|(^0[1,2]{1}\\d{1}-?\\d{8}-(\\d{1,4})$)|" + "" +
                "(^0[3-9]{1}\\d{2}-?\\d{7,8}-(\\d{1,4})$))";
        Pattern phonePattern = Pattern.compile(szPhonePattern);
        return phonePattern.matcher(phoneNum).matches();
    }

    /**
     * 判断是否是密码
     *
     * @param pwd
     * @return
     */
    public static boolean isPassWd(String pwd) {
        String patternStr = "[a-zA-Z0-9]+";
        Pattern pattern = Pattern.compile(patternStr);
        if (!pattern.matcher(pwd).matches()) {
            return false;
        }
        return true;
    }

    /**
     * 判断英文姓名
     *
     * @param name
     * @return
     */
    public static boolean isEnglishName(String name) {
        String szNamePattern = "[a-zA-Z]{1,10}";
        Pattern namePattern = Pattern.compile(szNamePattern);
        return namePattern.matcher(name).matches();
    }

    public static boolean isAlertContent(String name) {
        String szNamePattern = "[\\u4e00-\\u9fa5a-zA-Z0-9]{1,200}";
        Pattern namePattern = Pattern.compile(szNamePattern);
        return namePattern.matcher(name).matches();
    }

    /**
     * 判断是否英文或中文
     *
     * @param name
     * @return
     */
    public static boolean isEnglishOrChinese(String name) {
        String szNamePattern = "[\\u4e00-\\u9fa5a-zA-Z]{1,20}";
        Pattern namePattern = Pattern.compile(szNamePattern);
        return namePattern.matcher(name).matches();
    }

    /**
     * 判断是否是身份证
     *
     * @param idcard
     * @return
     */
    public static boolean isIdcard(String idcard) {
        String matchString15 = "^[1-9]\\d{7}((0[1-9])||(1[0-2]))((0[1-9])||(1\\d)||(2\\d)||" + ""
                + "(3[0-1]))\\d{3}$";
        String matchString18 = "^[1-9]\\d{5}[1-9]\\d{3}((0[1-9])||(1[0-2]))((0[1-9])||(1\\d)||" +
                "(2\\d)||(3[0-1]))\\d{3}([0-9]||X)$";
        Pattern pattern15 = Pattern.compile(matchString15);
        Pattern pattern18 = Pattern.compile(matchString18);
        return pattern15.matcher(idcard).matches() || pattern18.matcher(idcard).matches();
    }


    /**
     * 根据身份证获取年龄
     *
     * @param IDCardNum
     * @return
     */
    public static int getAgeByIdCard(String IDCardNum) {
        Calendar cal1 = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        cal1.set(Integer.parseInt(IDCardNum.substring(6, 10)), Integer.parseInt(IDCardNum
                .substring(10, 12)), Integer.parseInt(IDCardNum.substring(12, 14)));
        return getYearDiff(today, cal1);
    }

    public static int getYearDiff(Calendar cal, Calendar cal1) {
        int m = (cal.get(cal.MONTH)) - (cal1.get(cal1.MONTH));
        int y = (cal.get(cal.YEAR)) - (cal1.get(cal1.YEAR));
        return (y * 12 + m) / 12;
    }

    /*
    * 动态请求权限
    * */
    public static final int HAS_PERMISSION = 1;//有权限
    public static final int ALREADY_REJECT_ONE = 2;//已经拒绝过一次
    public static final int ALREADY_REJECT_FOREVER = 3;//永久拒绝

    public static int requestPermission(Activity context, String mPermission, int requestCode) {
        // Here, thisActivity is the current activity
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return HAS_PERMISSION;
        }
        if (ActivityCompat.checkSelfPermission(context, mPermission) == PackageManager
                .PERMISSION_GRANTED) {
            //有权限
            return HAS_PERMISSION;
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, mPermission)) {
                //已经拒绝过一次
                ActivityCompat.requestPermissions(context, new String[]{mPermission}, requestCode);
                return ALREADY_REJECT_ONE;
            } else {
                //已勾选不在询问
//                ActivityCompat.requestPermissions(context, new String[]{mPermission},
// requestCode);
                return ALREADY_REJECT_FOREVER;
            }

        }
    }


    /**
     * 判断是否中文或者数字或者字母
     *
     * @param str
     * @return
     */
    public static boolean isLetterDigitOrChinese(String str) {
        String regex = "^[a-z0-9A-Z\u4e00-\u9fa5]+$";
        return str.matches(regex);
    }

    /**
     * 获取流的编码格式
     *
     * @param is
     * @return
     * @throws IOException
     */
    public static String getStreamEncoding(InputStream is) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(is);
        bis.mark(2);
        byte[] first3bytes = new byte[3];
        int count = bis.read(first3bytes);
        if (count > 0) {
            bis.reset();
        }

        String encoding = null;
        if (first3bytes[0] == -17 && first3bytes[1] == -69 && first3bytes[2] == -65) {
            encoding = "utf-8";
        } else if (first3bytes[0] == -1 && first3bytes[1] == -2) {
            encoding = "unicode";
        } else if (first3bytes[0] == -2 && first3bytes[1] == -1) {
            encoding = "utf-16be";
        } else if (first3bytes[0] == -1 && first3bytes[1] == -1) {
            encoding = "utf-16le";
        } else {
            encoding = "GBK";
        }

        return encoding;
    }

    /**
     * 网络连接是否可用
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService
                (Context.CONNECTIVITY_SERVICE);
        if (null != connectivityManager) {
            NetworkInfo networkInfo[] = connectivityManager.getAllNetworkInfo();

            if (null != networkInfo) {
                for (NetworkInfo info : networkInfo) {
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        Toast.makeText(context, "网络连接失败",Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * 获取录音持续时间
     *
     * @param filePath
     * @return
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    @SuppressLint("NewApi")
    public static long getVoiceDuration(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return 0;
        }
        MediaMetadataRetriever triever = new MediaMetadataRetriever();
        triever.setDataSource(filePath);

        String duration = triever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long times = Long.valueOf(duration);

        return times / 1000;
    }


    /**
     * bitmap放大1.5倍
     */
    public static Bitmap bigBitmap(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postScale(1.5f, 1.5f); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight
                (), matrix, true);
        return resizeBmp;
    }


    /**
     * 检查手机上是否安装了指定的软件
     *
     * @param context
     * @param packageName：应用包名
     * @return
     */
    public static boolean isInstalled(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                String pkName = packageInfos.get(i).packageName;
                if (pkName.equals(packageName)) return true;
            }
        }
        return false;
    }


    //拼接服务器返回来的文件地址（兼容绝对路径和相对路径）
    public static String getAbsoluteUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        } else if (url.startsWith("http")) {
            return url;
        } else if (url.startsWith("//www")) {
            return "http:" + url;
        } else {
            if (url.contains("/icon")) {
                return "https://webrtc.huaaotech.com/" + url;
            } else {
                return "https://webrtc.huaaotech.com/upload/" + url;
            }
        }
    }


    //手机型号
    public static String getPhoneModel() {
        String model = Build.MODEL;
        if (!TextUtils.isEmpty(model)) {
            String replace = model.replace(" ", "");
            return replace;
        } else {
            return "";
        }
    }
}

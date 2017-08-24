package com.huaao.webrtc.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.huaao.webrtc.R;
import com.huaao.webrtc.adapters.CallUserListAdapter;
import com.huaao.webrtc.bean.DispatcherBean;
import com.huaao.webrtc.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by xdg on 2017/7/14.
 */

public  class AddCallPopupWindow<T extends DispatcherBean> extends PopupWindow implements View.OnClickListener,
        CallUserListAdapter.OnSelectItemListener {
    private final View conentView;
    private Context mContext;
    private BaseQuickAdapter adapter;
    private RecyclerView recyclerView;
    private Button submitBtn;
    private SwipeRefreshLayout swipeRefreshLayout;
    protected View loadingView;
    private String userId;
    private String roomId;
    private ArrayList<String> selectList;
//    private List<T> data;

    public AddCallPopupWindow(Context context, String roomId, OnSubmitListener listener,List<T> data) {
        this.mContext = context;
        this.roomId = roomId;
        mOnSubmitListener = listener;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        conentView = inflater.inflate(R.layout.add_call_popwindow, null);
        this.setContentView(conentView);
        //设置宽高
        this.setWidth(CommonUtils.dp2px(mContext, 300));
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.setTouchable(true);

        //实例化一个ColorDrawable
        ColorDrawable dw = new ColorDrawable(0xffffff);
        // 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
        this.setBackgroundDrawable(dw);
        this.setAnimationStyle(R.anim.slide_in_from_right);
        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1f);
            }
        });
        selectList = new ArrayList<>();
        initView();
        initAdapter();
        adapter.setNewData(data);
    }


    protected void initView() {
        loadingView = conentView.findViewById(R.id.loading_view);
        submitBtn = (Button) conentView.findViewById(R.id.btn_submit);
        submitBtn.setOnClickListener(this);
        recyclerView = (RecyclerView) conentView.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        //RecyclerView添加分割线
        recyclerView.addItemDecoration(new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL));

        swipeRefreshLayout = (SwipeRefreshLayout) conentView.findViewById(R.id.rotate_header_list_view_frame);
//        setRefreshBySelf(swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mOnSubmitListener!=null){
                    loadingView.setVisibility(View.VISIBLE);
                    mOnSubmitListener.onRefreshListener();
                }
            }
        });
    }



    public void setData(List<T> data){
        if (data!=null && data.size()>0) {
            adapter.setNewData(data);
            loadingView.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
        }else {
            setEmptyView();
        }
    }

    public void setEmptyView() {
        loadingView.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            View emptyView = inflater.inflate(R.layout.empty_view, null);
            try {
                TextView tvEmpty = (TextView) emptyView.findViewById(R.id.tv_empty);
                tvEmpty.setText(R.string.has_no_data);
            } catch (Exception e) {
                e.printStackTrace();
            }
            adapter.setEmptyView(emptyView);
        }
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_submit) {
            if (selectList == null || selectList.size() == 0) {
                Toast.makeText(mContext, "请选择被叫人",Toast.LENGTH_SHORT).show();
                return;
            }
            String addstring = listToString(selectList, ',');
            Log.d("AddCallPopupWindow", "selectList " + addstring);
            mOnSubmitListener.onSubmitClick(addstring);
            this.dismiss();

        }
    }

    public String listToString(List list, char separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i)).append(separator);
        }
        return sb.toString().substring(0, sb.toString().length() - 1);
    }

    @Override
    public void onCheckItemClick(String id, boolean check) {
        selectList.clear();
        selectList.add(id);
    }

    protected int getSize() {
        return 10;
    }

    private void initAdapter() {
        if (adapter == null) {
            adapter = new CallUserListAdapter(R.layout.add_call_item, null, this);
        }
        adapter.setEnableLoadMore(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        adapter.openLoadAnimation();
        //设置条目的动画效果
        adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        adapter.isFirstOnly(false);
        recyclerView.setAdapter(adapter);
    }

//    protected void requestData() {
//        String token = UserInfoHelper.getUserInfoHelper().getToken();
//
//        HttpRequestClient httpRequestClient = HttpRequestClient.getInstance();
//        Observable<JsonObject> observable = httpRequestClient.getHttpRequestUtils().invitedOnlinePolice(token, roomId);
//        httpRequestClient.toSubscribe(observable, null, new HttpListener<JsonObject>() {
//            @Override
//            public void onSuccess(DataRequestType type, JsonObject jsonObject) {
//                if (swipeRefreshLayout != null) {
//                    swipeRefreshLayout.refreshComplete();
//                }
//                if (loadingView != null) {
//                    loadingView.setVisibility(View.GONE);
//                }
//                if (!jsonObject.get("data").isJsonNull()) {
//                    data = GsonUtils.jsonToList(jsonObject.getAsJsonArray("data"), DispatcherBean.class);
//                }
//                if (data == null || data.size() == 0) {
//                    setEmptyView();
//                } else {
//                    adapter.setNewData(data);
//                }
//            }
//
//            @Override
//            public void onFailure(DataRequestType type, String errorMsg, int errorCode) {
//                ToastUtils.ToastShort(mContext, errorMsg);
//                if (swipeRefreshLayout != null) {
//                    swipeRefreshLayout.refreshComplete();
//                }
//                if (loadingView != null) {
//                    loadingView.setVisibility(View.GONE);
//                }
//                setEmptyView();
//            }
//        });
//    }

//    private void setEmptyView() {
//        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        if (inflater != null) {
//            View emptyView = inflater.inflate(R.layout.empty_view, null);
//            try {
//                TextView tvEmpty = (TextView) emptyView.findViewById(R.id.tv_empty);
//                tvEmpty.setText(R.string.has_no_data);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            adapter.setEmptyView(emptyView);
//        }
//    }

    public void showPopupWindow(View parent) {
        if (!this.isShowing()) {
            int[] location = new int[2];
            parent.getLocationOnScreen(location);
            this.getContentView().measure(0, 0);
            WindowManager wm = (WindowManager) mContext
                    .getSystemService(Context.WINDOW_SERVICE);

            int width = wm.getDefaultDisplay().getWidth();
            int height = wm.getDefaultDisplay().getHeight();
            //由于测量宽度不准确，在此用实际列表顶部与底部高度+列表间隔+字体高度计算组成
//        ze(    int measuredHeight = CommonUtils.dp2px(mContext, 16 * 2 + 18 * (mData.si) - 1) +
//                    14 * mData.size());//this.getContentView().getMeasuredHeight();
            this.showAtLocation(parent, Gravity.NO_GRAVITY, width - getWidth(), 0);
            backgroundAlpha(0.6f);
        } else {
            this.dismiss();
        }
    }

    /**
     * 设置添加屏幕的背景透明度  1,：全透明；0.5：半透明  0~1，取自己想到的透明度
     *
     * @param bgAlpha
     */
    private void backgroundAlpha(float bgAlpha) {
        if (mContext != null && mContext instanceof Activity) {
            WindowManager.LayoutParams lp = ((Activity) mContext).getWindow().getAttributes();
            lp.alpha = bgAlpha; //0.0-1.0
            ((Activity) mContext).getWindow().setAttributes(lp);
        }
    }

    //设置接口回调
    private AddCallPopupWindow.OnSubmitListener mOnSubmitListener;

    public interface OnSubmitListener {
        void onSubmitClick(String toUids);
        void onRefreshListener();
    }
}
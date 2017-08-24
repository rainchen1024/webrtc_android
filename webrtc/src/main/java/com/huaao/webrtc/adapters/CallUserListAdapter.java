package com.huaao.webrtc.adapters;

import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.huaao.webrtc.R;
import com.huaao.webrtc.bean.DispatcherBean;
import com.huaao.webrtc.utils.CommonUtils;

import java.util.List;


/**
 * Created by xdg on 2017/7/14.
 */

public class CallUserListAdapter extends BaseQuickAdapter<DispatcherBean, BaseViewHolder> {
    private int checkIndex = -1;
    private int lastIndex = -1;

    public CallUserListAdapter(int layoutResId, List<DispatcherBean> data, CallUserListAdapter.OnSelectItemListener listener) {
        super(layoutResId, data);
        mOnSelectItemListener = listener;
    }

    @Override
    protected void convert(BaseViewHolder vh, DispatcherBean item) {
        ImageView iv = vh.getView(R.id.info_window_head_image);
        String reporterImg = item.getImg();
        Glide.with(mContext).load(CommonUtils.getAbsoluteUrl(reporterImg)).placeholder(R
                .drawable.default_head_image).into(iv);
        vh.setText(R.id.info_window_name, item.getRealname());
        TextView distanceTv = vh.getView(R.id.info_window_distance);
//        MapUtils.calculateLineDistance(mContext, item.getPosition(), distanceTv);
        vh.setText(R.id.user_role_txt, item.getDeptName() + " " + item.getJobsName());
        CheckBox check = vh.getView(R.id.valid_rb);
        if (checkIndex == vh.getAdapterPosition()) {
            check.setChecked(true);
        } else {
            check.setChecked(false);
        }
        ParaHolder holder = new ParaHolder();
        holder.index = vh.getAdapterPosition();
        holder.id = item.getId();
        check.setTag(holder);
        check.setOnCheckedChangeListener(myCheckChangelistener);
    }

    private CompoundButton.OnCheckedChangeListener myCheckChangelistener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            //设置TextView的内容显示CheckBox的选择结果
            if (isChecked) {
                mOnSelectItemListener.onCheckItemClick(((ParaHolder) buttonView.getTag()).id, isChecked);
                checkIndex = ((ParaHolder) buttonView.getTag()).index;
                if (lastIndex >= 0) {
                    notifyItemChanged(lastIndex);
                }
                lastIndex = checkIndex;
            }
        }
    };

    //设置接口回调
    private CallUserListAdapter.OnSelectItemListener mOnSelectItemListener;

    public interface OnSelectItemListener {
        void onCheckItemClick(String id, boolean check);
    }

    class ParaHolder {
        public int index;
        public String id;
    }
}
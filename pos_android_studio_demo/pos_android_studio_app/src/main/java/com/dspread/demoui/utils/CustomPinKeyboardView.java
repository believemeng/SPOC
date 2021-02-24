package com.dspread.demoui.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.dspread.demoui.R;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Qianmeng Chen
 * @date 2020/07/15
 *  自定义支付密码组件
 */

public class CustomPinKeyboardView extends RelativeLayout {
    private AppCompatActivity mContext;//上下文
    private GridView mGridView; //支付键盘
    private String strPass="";//保存密码
    private TextView[]  mTvPass;//密码数字控件
    private ImageView mImageViewClose;//关闭
    private TextView mTvForget;//忘记密码
    private TextView mTvHint;//提示 (提示:密码错误,重新输入)
    private List<Integer> listNumber;//1,2,3---0
    private View mPassLayout;//布局
    private boolean isRandom;

    /**
     * 按钮对外接口
     */
    public static interface OnPayClickListener {
        void onPassFinish(String password);
        void onPayClose();
    }
    private OnPayClickListener mPayClickListener;
    public void setPayClickListener(OnPayClickListener listener) {
        mPayClickListener = listener;
    }

    public CustomPinKeyboardView(Context context) {
        super(context);
        this.mContext = (AppCompatActivity) context;

        initView();//初始化
        this.addView(mPassLayout); //将子布局添加到父容器,才显示控件
    }
    //在布局文件中使用的时候调用,多个样式文件
    public CustomPinKeyboardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    //在布局文件中使用的时候调用
    public CustomPinKeyboardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = (AppCompatActivity) context;

        initView();//初始化
        this.addView(mPassLayout); //将子布局添加到父容器,才显示控件
    }

    /**
     * 初始化
     */
    private void initView() {
        mPassLayout = LayoutInflater.from(mContext).inflate( R.layout.view_pin_keyboard_layout, null);
        mImageViewClose  = (ImageView) mPassLayout.findViewById(R.id.iv_close);//关闭
        mTvForget   = (TextView) mPassLayout.findViewById(R.id.tv_forget);//忘记密码
        mTvHint     = (TextView) mPassLayout.findViewById(R.id.tv_passText);//提示文字
        mTvPass     = new TextView[1];                                  //密码控件
        mTvPass[0]  = (TextView) mPassLayout.findViewById(R.id.tv_pass1);
        mGridView   = (GridView) mPassLayout.findViewById(R.id.gv_pass);

        mImageViewClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cleanAllTv();
                mPayClickListener.onPayClose();
            }
        });
        //初始化数据
        initData();
    }

    /**
     * isRandom是否开启随机数
     */
    private void initData(){
        if(isRandom){
            listNumber = new ArrayList<>();
            listNumber.clear();
            for (int i = 0; i <= 10; i++) {
                listNumber.add(i);
            }
            //此方法是打乱顺序
            Collections.shuffle(listNumber);
            for(int i=0;i<=10;i++){
                if(listNumber.get(i)==10){
                    listNumber.remove(i);
                    listNumber.add(9,10);
                }
            }
            listNumber.add(R.drawable.ic_pay_del0);
        }else {
            listNumber = new ArrayList<>();
            listNumber.clear();
            for (int i = 1; i <= 9; i++) {
                listNumber.add(i);
            }
            listNumber.add(10);
            listNumber.add(0);
            listNumber.add(R.drawable.ic_pay_del0);
        }
        if(mGridView != null && adapter != null) {
            mGridView.setAdapter(adapter);
        }
    }


    /**
     *   GridView的适配器
     */
    String s = "";
    BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return listNumber.size();
        }
        @Override
        public Object getItem(int position) {
            return listNumber.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.view_pin_keyboard_gridview_item, null);
                holder = new ViewHolder();
                holder.btnNumber = (TextView) convertView.findViewById(R.id.btNumber);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            //-------------设置数据----------------
            holder.btnNumber.setText(listNumber.get(position)+"");
            if (position == 9) {
                holder.btnNumber.setText("√");
                holder.btnNumber.setBackgroundColor(mContext.getResources().getColor(R.color.graye3));
            }
            if (position == 11) {
                holder.btnNumber.setText("");
                holder.btnNumber.setBackgroundResource(listNumber.get(position));
            }
            //监听事件----------------------------
            if(position==11) {
                holder.btnNumber.setOnTouchListener(new OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (position == 11) {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    holder.btnNumber.setBackgroundResource(R.drawable.ic_pay_del1);
                                    break;
                                case MotionEvent.ACTION_MOVE:
                                    holder.btnNumber.setBackgroundResource(R.drawable.ic_pay_del0);
                                    break;
                                case MotionEvent.ACTION_UP:
                                    holder.btnNumber.setBackgroundResource(R.drawable.ic_pay_del0);
                                    break;
                            }
                        }
                        return false;
                    }
                });
            }
            holder.btnNumber.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position < 11 &&position!=9) {//0-9按钮
                        if(strPass.length() >= 12){
                            return;
                        }
                        else {
                            strPass=strPass+listNumber.get(position);//得到当前数字并累加
                            Log.i("test","strPass add---"+strPass);
                            s += "*";
                            mTvPass[0].setText(s); //设置界面*
                        }
                    }
                    else if(position == 11) {//删除
                        if(strPass.length()>0){
                            strPass=strPass.substring(0,strPass.length()-1);//删除一位
                            Log.i("test","strPass del---"+strPass);
                            s = s.substring(0,s.length() - 1);
                            mTvPass[0].setText(s);//去掉界面*
                        }
                    }
                    if(position==9){//空按钮
                        if(strPass.length()<4){
                            return;
                        }else{
                            //输入完成
                            mPayClickListener.onPassFinish(strPass);//请求服务器验证密码
                        }
                    }
                }
            });

            return convertView;
        }
    };
    static class ViewHolder {
        public TextView btnNumber;
    }

    /***
     * 设置随机数
     * @param israndom
     */
     public CustomPinKeyboardView setRandomNumber(boolean israndom){
        isRandom=israndom;
        initData();
        adapter.notifyDataSetChanged();
        return this;
     }
    /**
     * 关闭图片
     * 资源方式
     */
    public CustomPinKeyboardView setCloseImgView(int resId) {
        mImageViewClose.setImageResource(resId);
        return this;
    }
    /**
     * 关闭图片
     * Bitmap方式
     */
    public CustomPinKeyboardView setCloseImgView(Bitmap bitmap) {
        mImageViewClose.setImageBitmap(bitmap);
        return this;
    }
    /**
     * 关闭图片
     * drawable方式
     */
    public CustomPinKeyboardView setCloseImgView(Drawable drawable) {
        mImageViewClose.setImageDrawable(drawable);
        return this;
    }


    /**
     * 设置忘记密码文字
     */
    public CustomPinKeyboardView setForgetText(String text) {
        mTvForget.setText(text);
        return this;
    }
    /**
     * 设置忘记密码文字大小
     */
    public CustomPinKeyboardView setForgetSize(float textSize) {
        mTvForget.setTextSize(textSize);
        return this;
    }
    /**
     * 设置忘记密码文字颜色
     */
    public CustomPinKeyboardView setForgetColor(int textColor) {
        mTvForget.setTextColor(textColor);
        return this;
    }

    /**
     * 设置提醒的文字
     */
    public CustomPinKeyboardView setHintText(String text) {
        mTvHint.setText(text );
        return this;
    }
    /**
     * 设置提醒的文字大小
     */
    public CustomPinKeyboardView setTvHintSize(float textSize) {
        mTvHint.setTextSize(textSize);
        return this;
    }
    /**
     * 设置提醒的文字颜色
     */
    public CustomPinKeyboardView setTvHintColor(int textColor) {
        mTvHint.setTextColor(textColor);
        return this;
    }
    /**
     * 清楚所有密码TextView
     */
    public CustomPinKeyboardView cleanAllTv() {
        strPass="";
        mTvPass[0].setText("");
        return this;
    }
}

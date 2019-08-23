package com.shang.todolist.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shang.todolist.event.EditTodoEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public abstract class BaseFragment extends Fragment {
    protected final String TAG = this.getClass().getSimpleName();

    /**
     * 需持有引用 并 手动赋值
     * 防止getActivity()等于null的情况
     */
    protected Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(setLayout(), null);
        return view;
    }

    /**
     * 绑定布局
     *
     * @return
     */
    protected abstract int setLayout();

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    /**
     * 初始化组件
     */
    protected abstract void initView();

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    /**
     * 设置数据等逻辑代码
     */
    protected abstract void initData();

    /**
     * 此方法适用于 show hide 方式，replace不需要使用
     *
     * @param hidden
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!isAdded()) {
            return;
        }
        if (hidden) {
            //不在最前端界面显示，相当于调用了onPause()
        } else {
            //重新显示到最前端 ,相当于调用了onResume()
        }
    }

    /**
     * 简化findViewById
     *
     * @param resId
     * @param <T>
     * @return
     */
    protected <T extends View> T find(int resId) {
        return (T) getView().findViewById(resId);
    }

    /**
     * intent跳转
     *
     * @param context
     * @param clazz
     */
    protected void toClass(Context context, Class<? extends Activity> clazz) {
        toClass(context, clazz, null);
    }

    /**
     * intent带值跳转
     *
     * @param context
     * @param clazz
     * @param bundle
     */
    protected void toClass(Context context, Class<? extends Activity> clazz, Bundle bundle) {
        Intent intent = new Intent(context, clazz);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    /**
     * 带返回值的跳转
     *
     * @param context
     * @param clazz
     * @param bundle
     * @param requestCode
     */
    protected void toClass(Context context, Class<? extends Activity> clazz, Bundle bundle, int requestCode) {
        Intent intent = new Intent(context, clazz);
        intent.putExtras(bundle);
        getActivity().startActivityForResult(intent, requestCode);
    }

    protected boolean isNetworkConnected() {
        if (mContext != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EditTodoEvent event) {

    }

    @Override
    public void onDetach() {
        EventBus.getDefault().unregister(this);
        super.onDetach();
    }
}

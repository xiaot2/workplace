package com.toplion.cplusschool.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.ab.http.AbHttpUtil;
import com.ab.http.AbRequestParams;
import com.ab.util.AbDialogUtil;
import com.ab.util.AbJsonUtil;
import com.ab.view.pullview.AbPullToRefreshView;
import com.ab.view.pullview.AbPullToRefreshView.OnFooterLoadListener;
import com.ab.view.pullview.AbPullToRefreshView.OnHeaderRefreshListener;
import com.toplion.cplusschool.Activity.PayOrderActivity;
import com.toplion.cplusschool.Adapter.MyOrderListAdapter;
import com.toplion.cplusschool.Bean.OrderBean;
import com.toplion.cplusschool.Bean.OrderListBeanModel;
import com.toplion.cplusschool.Common.CacheConstants;
import com.toplion.cplusschool.Common.Constants;
import com.toplion.cplusschool.R;
import com.toplion.cplusschool.Utils.ReturnUtils;
import com.toplion.cplusschool.Utils.SharePreferenceUtils;
import com.toplion.cplusschool.Utils.ToastManager;
import com.toplion.cplusschool.dao.CallBackParent;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 我的订单  待支付
 * 显示待支付的订单
 * 下拉刷新
 * 显示暂无订单
 *
 * @author liyb
 * @version 1.0
 */
public class MyOrderWaitFragment extends Fragment implements OnHeaderRefreshListener,
        OnFooterLoadListener {
    private AbPullToRefreshView mAbPullToRefreshView = null;
    private ListView listView;                        // 显示订单的ListView
    private SharePreferenceUtils share;
    private List<Map<String, String>> orders;          // 订单数据
    private LinearLayout my_order_datas;              // 存在数据
    private LinearLayout my_order_nodatas;            // 暂无订单
    private AbHttpUtil abHttpUtil;//w网络请求工具
    private MyOrderListAdapter myOrderListAdapter;//订单列表适配器
    private List<OrderBean> orderList = new ArrayList<OrderBean>();//订单列表


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // 获取自定义下拉刷新View
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_order_wait, container, false);      // 获取本View
        share = new SharePreferenceUtils(getActivity());
        abHttpUtil = AbHttpUtil.getInstance(getActivity());
        listView = (ListView) view.findViewById(R.id.my_order_wait_list);            // 订单集合
        // 隐藏页面
        my_order_nodatas = (LinearLayout) view.findViewById(R.id.my_order_wait_nodatas);
        my_order_datas = (LinearLayout) view.findViewById(R.id.my_order_wait_datas);
        my_order_nodatas.setVisibility(View.GONE);
        my_order_datas.setVisibility(View.GONE);
        mAbPullToRefreshView = (AbPullToRefreshView) view
                .findViewById(R.id.mPullRefreshView);
        // 设置监听器
        mAbPullToRefreshView.setOnHeaderRefreshListener(this);
        mAbPullToRefreshView.setOnFooterLoadListener(this);

        // 设置进度条的样式
        mAbPullToRefreshView.getHeaderView().setHeaderProgressBarDrawable(
                this.getResources().getDrawable(R.drawable.progress_circular));
        mAbPullToRefreshView.getFooterView().setFooterProgressBarDrawable(
                this.getResources().getDrawable(R.drawable.progress_circular));
        myOrderListAdapter = new MyOrderListAdapter(getActivity(),orderList,Constants.not_pay);
        listView.setAdapter(myOrderListAdapter);
        setListener();
        // 初始化加载页面数据
        getOrderList();
        return view;
    }

    @Override
    public void onFooterLoad(AbPullToRefreshView view) {
        mAbPullToRefreshView.onFooterLoadFinish();
    }

    @Override
    public void onHeaderRefresh(AbPullToRefreshView view) {
        getOrderList();
    }
    /**
     * 获取订单列表
     */
    private void getOrderList(){
        String url = Constants.BASE_URL+"?rid=" + ReturnUtils.encode("getUserOrderInfo")+Constants.BASEPARAMS;
        AbRequestParams requestParams = new AbRequestParams();
        requestParams.put("username", share.getString("username", ""));
        requestParams.put("type",Constants.ORDER_NOPAY);
        abHttpUtil.post(url,requestParams,new CallBackParent(getActivity(),"正在加载,请稍后...") {
            @Override
            public void Get_Result(String content) {
                AbDialogUtil.removeDialog(getActivity());
                Log.e("orderList====",content+"");
                OrderListBeanModel orderListBeanModel = AbJsonUtil.fromJson(content,OrderListBeanModel.class);
                if(orderListBeanModel.getCode().equals(CacheConstants.LOCAL_SUCCESS) || orderListBeanModel.getCode().equals(CacheConstants.SAM_SUCCESS)){
                    if(orderListBeanModel.getData()!=null&&orderListBeanModel.getData().getUserOrderInfo()!=null){
                        orderList = orderListBeanModel.getData().getUserOrderInfo();//订单列表
                        if(orderList.size()<1){
                            my_order_datas.setVisibility(View.GONE);
                            my_order_nodatas.setVisibility(View.VISIBLE);
                        }else{
                            my_order_datas.setVisibility(View.VISIBLE);
                            my_order_nodatas.setVisibility(View.GONE);
                            myOrderListAdapter.setMlist(orderList);
                            myOrderListAdapter.notifyDataSetChanged();
                        }
                    }
                }else{
                    my_order_datas.setVisibility(View.GONE);
                    my_order_nodatas.setVisibility(View.VISIBLE);
                    ToastManager.getInstance().showToast(getActivity(), orderListBeanModel.getMsg());
                }
            }



            @Override
            public void onFinish() {
                AbDialogUtil.removeDialog(getActivity());
                mAbPullToRefreshView.onHeaderRefreshFinish();
            }

            @Override
            public void onFailure(int statusCode, String content, Throwable error) {
                AbDialogUtil.removeDialog(getActivity());
                mAbPullToRefreshView.onHeaderRefreshFinish();
            }
        });
    }
    //设置点击事件
    private void setListener(){
        // 点击具体的订单
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String num = orderList.get(position).getOrderId();// 订单编号
                String pkgName = orderList.get(position).getOrderPackageName();// 选择的套餐
                String orderPayString = orderList.get(position).getOrderPayString();//订单支付字符串
                // 根据状态的不同跳转到不同的Activity
                Intent intent = new Intent(getActivity(), PayOrderActivity.class);
                intent.putExtra("pkgName", pkgName);
                intent.putExtra("orderPayString",orderPayString);
                //传值到订单详情Activity
                intent.putExtra("orderNum",num);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getActivity().getClass().getSimpleName()); //统计页面
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getActivity().getClass().getSimpleName());
    }
}

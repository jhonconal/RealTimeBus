package graduate.txy.com.realtimebus.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.core.TaxiInfo;
import com.baidu.mapapi.search.core.VehicleInfo;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;

import java.util.ArrayList;
import java.util.List;

import graduate.txy.com.realtimebus.R;
import graduate.txy.com.realtimebus.adapter.PassInfoAdapter;
import graduate.txy.com.realtimebus.domain.PassInfo;
import graduate.txy.com.realtimebus.globalApp.MyApplication;
import graduate.txy.com.realtimebus.utils.SharePreferenceUtils;

/**
 * Created by lenovo on 2016/3/17.
 */
public class PassFragment extends BaseFragment {

    public Activity mActivity;

    public PassFragment() {
        title = "换乘查询";
    }

    private static final String TAG = "MyMapActivity";

    private RoutePlanSearch mSearch = null;
    private OnGetRoutePlanResultListener listener = null;
    private List<TransitRouteLine> transitRouteLineList = null;
    private List<PassInfo> passInfoList = new ArrayList<PassInfo>();
    private String cityName = null;
    private EditText et_start;
    private EditText et_end;
    private Button bt_convert;
    private Button bt_select;
    private View view;
    private TextView tv_pass_info;
    private ListView lv_pass_info;
    private PassInfoAdapter pia;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SDKInitializer.initialize(MyApplication.getInstance());
        view = inflater.inflate(R.layout.pass_fragment, container, false);
        init();
        return view;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = getActivity();
    }

    //初始化
    private void init() {
        cityName = SharePreferenceUtils.getSPStringValue(mActivity, "city", "北京");
        et_start = (EditText) view.findViewById(R.id.et_start);
        et_end = (EditText) view.findViewById(R.id.et_end);
        bt_convert = (Button) view.findViewById(R.id.bt_convert);
        bt_select = (Button) view.findViewById(R.id.bt_select);
        tv_pass_info = (TextView) view.findViewById(R.id.tv_pass_info);
        lv_pass_info = (ListView) view.findViewById(R.id.lv_pass);
        pia = new PassInfoAdapter(mActivity, passInfoList);
        lv_pass_info.setAdapter(pia);
        Log.i(TAG,pia.getCount()+"");
        bt_convert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String start = et_end.getText().toString().trim();
                String end = et_start.getText().toString().trim();
                et_start.setText(start);
                et_end.setText(end);
                Log.i(TAG, "convert");

            }
        });
        bt_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String start = et_start.getText().toString().trim();
                String end = et_end.getText().toString().trim();
                Log.i(TAG, start + "--" + end + "---" + cityName);
                selectRoute(start, end, cityName);


            }
        });

        mSearch = RoutePlanSearch.newInstance();
        initListener();
        mSearch.setOnGetRoutePlanResultListener(listener);
        Log.i(TAG, "监听器添加");
        bt_select.setEnabled(true);


    }

    /**
     * 查询公交换乘
     *
     * @param start 起点
     * @param end   终点
     * @param city  城市
     */
    public void selectRoute(String start, String end, String city) {
        Log.i(TAG, "select");
        PlanNode stNode = PlanNode.withCityNameAndPlaceName(city, start);
        PlanNode enNode = PlanNode.withCityNameAndPlaceName(city, end);
        //TransitRoutePlanOption可以进行策略选择
        boolean a = mSearch.transitSearch((new TransitRoutePlanOption())
                .from(stNode).policy(TransitRoutePlanOption.TransitPolicy.EBUS_WALK_FIRST)
                .city(city)
                .to(enNode));
        Log.i(TAG, String.valueOf(a));
        tv_pass_info.setVisibility(View.INVISIBLE);
    }

    public void initListener() {
        Log.i(TAG, "initListener");
        listener = new OnGetRoutePlanResultListener() {
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
            }

            @Override
            public void onGetTransitRouteResult(TransitRouteResult result) {
                transitRouteLineList = result.getRouteLines();
                passInfoList.clear();//先清除数据
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Log.i(TAG, "抱歉，未找到结果");
                    Toast.makeText(MyApplication.getInstance(), "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                }
                if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                    //TODO 智能提醒位置
                    // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                    com.baidu.mapapi.search.route.SuggestAddrInfo si = result.getSuggestAddrInfo();
                    Log.i(TAG, "SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR");
                    return;
                }
                if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                    TaxiInfo ti = result.getTaxiInfo();
                    PassInfo info;

                    Log.i(TAG, ti.getDesc() + "描述" + ti.getTotalPrice() + "总价格");
                    for (TransitRouteLine tr : transitRouteLineList) {//排序查询条件
                        info = new PassInfo();
                        int num = 0;
                        List<TransitRouteLine.TransitStep> stepList = tr.getAllStep();
                        info.setTotalLength(tr.getDistance());
                        info.setTotalTime(tr.getDuration());
                        List<PassInfo.PassItemInfo> itemInfoList = new ArrayList<PassInfo.PassItemInfo>();
                        StringBuffer routeName = new StringBuffer();
                        PassInfo.PassItemInfo itemInfo;
                        for (TransitRouteLine.TransitStep ts : stepList) {//计算换乘次数
                            itemInfo = (info.new PassItemInfo());
                            VehicleInfo vi = ts.getVehicleInfo();
                            if (vi != null) {
                                num += vi.getPassStationNum();
                                itemInfo.setStationNum(vi.getPassStationNum());
                                routeName.append(vi.getTitle());
                                routeName.append("|");
                            }
                            itemInfo.setPassMethod(ts.getInstructions());
                            itemInfo.setTransport((ts.getStepType()).toString());
                            itemInfoList.add(itemInfo);
                        }
                        routeName.deleteCharAt(routeName.length() - 1);
                        info.setTotalStationNum(num);
                        info.setPassItemInfoList(itemInfoList);
                        info.setRouteName(routeName.toString());
                        passInfoList.add(info);
                    }

                    Log.i(TAG, "-----------------------输出-----------------------------");
                    for (PassInfo pi : passInfoList) {
                        Log.i(TAG, pi.toString());
                        Log.i(TAG, "********************");
                    }

                    pia.notifyDataSetChanged();
                    Log.i(TAG,pia.getCount()+"");
                    //notifyDataSetChanged();
                    //pia.updateListView(passInfoList);

                }
            }

            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
            }

            @Override
            public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {
            }
        };
    }

    @Override
    public void onDestroyView() {
        mSearch.destroy();
        super.onDestroyView();
    }
}

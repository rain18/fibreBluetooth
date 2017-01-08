package rain.fibreoblue.activity;

import java.security.cert.TrustAnchor;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import rain.fibreoblue.R;
import rain.fibreoblue.adapter.DeviceAdapter;
import rain.fibreoblue.entity.EntityDevice;
import rain.fibreoblue.service.BLEService;
import rain.fibreoblue.utils.BluetoothController;
import rain.fibreoblue.utils.ConstantUtils;
import rain.fibreoblue.utils.CtrlProperty;
import rain.fibreoblue.utils.QueueController;
import rain.fibreoblue.utils.SharePrefenceConfig;

/**
 * 主页面
 */
public class MainActivity extends Activity {

    private static final String TAG = "fibre_Bluetooth";
    public static QueueController queueController;

    static {
        Integer blueQLength = Integer.valueOf(CtrlProperty.getDefaultProperties().getProperty("BlueQLength"));
        Log.i(TAG, "blueQLength: "+blueQLength);
        queueController = new QueueController(blueQLength);//定时拿到的蓝牙信息
    }

    private enum Model {CHECK, WORK, INIT};
    private static Model fibreModel=Model.INIT; //记录板子的状态,初始状态

    private ListView listview;
    private ArrayList<EntityDevice> list = new ArrayList<EntityDevice>();
    private DeviceAdapter adapter;
    private Intent intentService;
    private MsgReceiver receiver;

    private TextView connectedDevice;
    private TextView receivedMessage;

    private EditText editSend;
    private Button btnSend, btnDesChart, btnCheck, search, btnWork,disconnect;
    BluetoothController controller = BluetoothController.getInstance();
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_main);
        initView();
        initService();
        initData();
        addListener();
        registerReceiver();

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void addListener() {
        listview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index,
                                    long arg3) {
                BluetoothController.getInstance().connect(list.get(index));
                btnCheck.setVisibility(View.VISIBLE);
                btnWork.setVisibility(View.VISIBLE);
                btnDesChart.setVisibility(View.VISIBLE);
            }
        });

        search.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                list.clear();//清空上次搜索的结果
                connectedDevice.setText("");
                adapter.notifyDataSetChanged();
                if (!BluetoothController.getInstance().initBLE()) {//手机不支持
                    Toast.makeText(MainActivity.this, "您的手机不支持蓝牙",
                            Toast.LENGTH_SHORT).show();
                    return;//手机不支持蓝牙就啥也不用干了，关电脑睡觉去吧
                }
                if (!BluetoothController.getInstance().isBleOpen()) {
                    Toast.makeText(MainActivity.this, "请打开蓝牙",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                new GetDataTask().execute();//搜索
            }
        });

        btnSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String str = editSend.getText().toString();
                if (str != null && str.length() > 0) {
                    Log.i(TAG, str.getBytes().toString());
                    controller.write(str.getBytes());
                } else {
                    toast("请填上发送的内容");
                }
            }
        });
        //自检状态
        btnCheck.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = "#";
                controller.write(str.getBytes());
                fibreModel = Model.CHECK;
            }
        });
        //工作状态
        btnWork.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //开启工作模式
                String str = "$";
                controller.write(str.getBytes());
                fibreModel = Model.WORK;
            }
        });
        //画出波形图
        btnDesChart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //查看波形图
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, ChartActivity.class);
                startActivity(intent);
            }
        });
        disconnect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //查看波形图
                BluetoothController.getInstance().disconnect();
            }
        });
    }

    private void initData() {
        adapter = new DeviceAdapter(this, list);
        listview.setAdapter(adapter);
    }

    /**
     * 开始服务，初始化蓝牙
     */
    private void initService() {
        //开始服务
        intentService = new Intent(MainActivity.this, BLEService.class);
        startService(intentService);
        //初始化蓝牙
        BluetoothController.getInstance().initBLE();
    }

    /**
     * findViewById
     */
    private void initView() {
        connectedDevice = (TextView) findViewById(R.id.connected_device);
        receivedMessage = (TextView) findViewById(R.id.received_message);
        listview = (ListView) findViewById(R.id.list_devices);
        editSend = (EditText) findViewById(R.id.edit_send);
        btnSend = (Button) findViewById(R.id.btn_send);
        search = (Button) findViewById(R.id.btn_search);
        btnCheck = (Button) findViewById(R.id.self_check);
        btnDesChart = (Button) findViewById(R.id.deschart);
        btnWork = (Button) findViewById(R.id.work);
        disconnect = (Button) findViewById(R.id.disconnect);
    }

    private void registerReceiver() {
        receiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConstantUtils.ACTION_UPDATE_DEVICE_LIST);
        intentFilter.addAction(ConstantUtils.ACTION_CONNECTED_ONE_DEVICE);
        intentFilter.addAction(ConstantUtils.ACTION_RECEIVE_MESSAGE_FROM_DEVICE);
        intentFilter.addAction(ConstantUtils.ACTION_STOP_CONNECT);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private class GetDataTask extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... params) {
            if (BluetoothController.getInstance().isBleOpen()) {
                BluetoothController.getInstance().startScanBLE();
            }
            ;//开始扫描
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
        }
    }

    /**
     * 广播接收器
     */
    public class MsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(
                    ConstantUtils.ACTION_UPDATE_DEVICE_LIST)) {
                String name = intent.getStringExtra("name");
                String address = intent.getStringExtra("address");
                boolean found = false;//记录该记录是否在list中
                for (EntityDevice device : list) {
                    if (device.getAddress().equals(address)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    EntityDevice temp = new EntityDevice();
                    temp.setName(name);
                    temp.setAddress(address);
                    list.add(temp);
                    adapter.notifyDataSetChanged();
                }
            } else if (intent.getAction().equalsIgnoreCase(ConstantUtils.ACTION_CONNECTED_ONE_DEVICE)) {
                connectedDevice.setText("连接的蓝牙是：" + intent.getStringExtra("address"));
            } else if (intent.getAction().equalsIgnoreCase(ConstantUtils.ACTION_STOP_CONNECT)) {
                connectedDevice.setText("");
                toast("连接已断开");
            } else if (intent.getAction().equalsIgnoreCase(ConstantUtils.ACTION_RECEIVE_MESSAGE_FROM_DEVICE)) {
                receivedMessage.append("\n\r" + intent.getStringExtra("message"));
                String msg = intent.getStringExtra("message"); //得到信息
                String[] egList = msg.split("1003");
                //将得到的String 解析得到蓝牙数据
                switch (fibreModel) {
                    case CHECK:
                        String checkStr = egList[0];
                        if (checkStr.length()<12) {
                            break;
                        }
                        String data_result = checkStr.substring(8, checkStr.length() - 4);
                        Log.i(TAG, "onReceive check: "+data_result);
                        if (data_result.equals("FF")) {
                            toast("自检失败");
                            btnWork.setEnabled(false); //setClickable 让按键按一下
                            btnDesChart.setEnabled(false);
                        } else {
                            btnWork.setEnabled(true);
                            btnDesChart.setEnabled(true);
                        }
                        break;
                    case WORK:
                        //两帧数据只取第一帧的波形
                        Log.i(TAG, "onReceive wave: "+egList[0]);
                        String wave = egList[0];
                        if (wave.length()>12) {
                            String shou = wave.substring(8, wave.length() - 4);
                            Pattern pattern = Pattern.compile("(.{2})");
                            Matcher matcher = pattern.matcher(shou);
                            while (matcher.find()) {
                                queueController.insert(matcher.group());
                            }
                        }
                        break;
                    default:
                        Log.i(TAG, "onReceive: "+egList);
                }
            }
        }
    }


    private void toast(String str) {
        Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(intentService);
        unregisterReceiver(receiver);
    }
}

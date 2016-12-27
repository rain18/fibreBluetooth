package rain.fibreoblue.activity;

import java.util.ArrayList;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import rain.fibreoblue.utils.QueueController;

/**
 * 主页面
 */
public class MainActivity extends Activity {

	static QueueController queueController = new QueueController(240);//定时拿到的蓝牙信息
	private Button search;
	private ListView listview;
	private ArrayList<EntityDevice> list = new ArrayList<EntityDevice>();
	private DeviceAdapter adapter;
	private Intent intentService;
	private MsgReceiver receiver;
	private static final String ACTIVITY_TAG = "fibre_Bluetooth";

	private TextView connectedDevice;
	private TextView receivedMessage;

	private EditText editSend;
	private Button btnSend,desChart,selfTest;
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

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
	}

	private void addListener() {
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int index,
									long arg3) {
				BluetoothController.getInstance().connect(list.get(index));
				selfTest.setVisibility(View.VISIBLE);
				desChart.setVisibility(View.VISIBLE);
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
					Log.i(ACTIVITY_TAG, str.getBytes().toString());
					controller.write(str.getBytes());
				} else {
					toast("请填上发送的内容");
				}
			}
		});
		//自检状态
		selfTest.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String str = "1002031100001003";
				if (controller.write(str.getBytes())) {
					Toast.makeText(MainActivity.this,"success",Toast.LENGTH_LONG).show();
				}
			}
		});
		//工作状态
		desChart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, ChartActivity.class);
				startActivity(intent);
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
		selfTest = (Button) findViewById(R.id.self_test);
		desChart = (Button) findViewById(R.id.deschart);
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
/*
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client.connect();
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Main Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app URL is correct.
				Uri.parse("android-app://rain.fibreoblue.activity/http/host/path")
		);
		AppIndex.AppIndexApi.start(client, viewAction);*/
	}

	@Override
	public void onStop() {
		super.onStop();
/*
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Main Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app URL is correct.
				Uri.parse("android-app://rain.fibreoblue.activity/http/host/path")
		);
		AppIndex.AppIndexApi.end(client, viewAction);
		client.disconnect();*/
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
				}// for
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
				String msg = intent.getStringExtra("message");
				queueController.insert(msg);
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

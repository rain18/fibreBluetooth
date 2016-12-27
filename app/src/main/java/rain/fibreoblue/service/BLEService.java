package rain.fibreoblue.service;


import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import rain.fibreoblue.utils.BluetoothController;
import rain.fibreoblue.utils.ConstantUtils;

public class BLEService extends Service {
    BluetoothController bleCtrl;
    private static final String TAG = "BLEService";

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case ConstantUtils.WM_BLE_CONNECTED_STATE_CHANGE://连接到某个设备
                    Bundle bundle = (Bundle) msg.obj;
                    String address = bundle.getString("address");
                    String name = bundle.getString("name");
                    // 连接状态改变广播
                    Bundle bundle1 = new Bundle();
                    bundle1.putString("address", address);
                    bundle1.putString("name", name);
                    Intent intentDevice = new Intent(
                            ConstantUtils.ACTION_CONNECTED_ONE_DEVICE);
                    intentDevice.putExtras(bundle1);
                    sendBroadcast(intentDevice);
                    break;

                case ConstantUtils.WM_STOP_CONNECT:
                    Intent stopConnect = new Intent(
                            ConstantUtils.ACTION_STOP_CONNECT);
                    sendBroadcast(stopConnect);
                    break;

                case ConstantUtils.WM_STOP_SCAN_BLE://搜索5s后停止搜索
                    bleCtrl.stopScanBLE();
                    break;
                case ConstantUtils.WM_UPDATE_BLE_LIST://回调发来的列表信息
                    // 更新蓝牙列表
                    Intent intent = new Intent(
                            ConstantUtils.ACTION_UPDATE_DEVICE_LIST);
                    BluetoothDevice device = (BluetoothDevice) msg.obj;
                    intent.putExtra("name", device.getName());
                    intent.putExtra("address", device.getAddress());
                    sendBroadcast(intent);
                    break;

                case ConstantUtils.WM_RECEIVE_MSG_FROM_BLE:// 接收蓝牙发送的信息
                    String mes = (String) msg.obj;
                    Intent mesDevice = new Intent(
                            ConstantUtils.ACTION_RECEIVE_MESSAGE_FROM_DEVICE);
                    mesDevice.putExtra("message", mes);
                    Log.i(TAG, "handleMessage: " + mes);
                    sendBroadcast(mesDevice);
                    break;
            }
        }
    };

    public void onStart(Intent intent, int startId) {
        bleCtrl = BluetoothController.getInstance();
        bleCtrl.setServiceHandler(handler);
    }

    ;

}

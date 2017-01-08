package rain.fibreoblue.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by rain on 2017/1/6.
 */
public class SharePrefenceConfig {
    /**
     * Max 是接收蓝牙数据的长度, 默认是 3000
     * M 是画图队列的大小，默认是800
     * n' 是每秒读多少
     * m 是读取的频率， 默认是24
     * n 是画图的长度
      */
    private Context context;

    public SharePrefenceConfig(Context context) {
        this.context = context;
    }

    //存储配置值
    public void saveConfig (String key, Integer value) {
        SharedPreferences config = context.getSharedPreferences("config", 0);
        SharedPreferences.Editor editor =  config.edit();
        //存放数据
        editor.putInt(key, value);
        editor.commit();
    }

    //读取配置的值
    public Integer getConfig (String key, Integer defaultValue) {
        SharedPreferences config = context.getSharedPreferences("config", 0);
        Integer value = config.getInt(key, defaultValue);
        return value;
    }
}
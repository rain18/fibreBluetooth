package rain.fibreoblue.utils;

import android.content.Context;
import android.content.res.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import rain.fibreoblue.App;
import rain.fibreoblue.R;
import rain.fibreoblue.activity.MainActivity;

/**
 * Created by rain on 2017/1/6.
 */
public class CtrlProperty {
    /**
     * 得到配置文件
     */
    public static Properties getDefaultProperties() {
        Properties props = new Properties();
        try {
            Resources res = App.getContext().getResources();
            InputStream in = res.openRawResource(R.raw.defaultsetting);
            props.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }
}

package rain.fibreoblue.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.logging.LogRecord;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import rain.fibreoblue.R;
import rain.fibreoblue.utils.CtrlProperty;
import rain.fibreoblue.utils.QueueController;
import rain.fibreoblue.utils.SharePrefenceConfig;


public class ChartActivity extends Activity{

    private static final String TAG = "ChartActivity";
    private LineChartView lineChart;
    private static Integer blueQLength = Integer.valueOf(CtrlProperty.getDefaultProperties().getProperty("ChartQLenth")); //缓存区的长度
    private static Integer dChartFreq = Integer.valueOf(CtrlProperty.getDefaultProperties().getProperty("ChartFreq")); //刷新频率
    private static Integer dChartLength = Integer.valueOf(CtrlProperty.getDefaultProperties().getProperty("ChartLength")); //默认画图点数
    private static Integer chartFreq;//实际刷新频率
    private static Integer chartLength;//实际的点数
    private SharePrefenceConfig shaConfig = new SharePrefenceConfig(this);
    private QueueController queueController;
    private Button btnStart,btnStop;
    private DrawLineThread chartThread;
    private Thread drawThread;
    private ArrayAdapter<String> chartadapter,reqadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        lineChart = (LineChartView) findViewById(R.id.line_chart);
        queueController = new QueueController(blueQLength);
        chartFreq = shaConfig.getConfig("ChartFreq",dChartFreq);
        chartLength = shaConfig.getConfig("ChartLength",dChartLength);
        init();
        chartThread = new DrawLineThread(chartFreq,chartLength,lineChart,queueController);
        drawThread = new Thread(chartThread);
        drawThread.start();
    }


    public void init() {
        btnStart = (Button) findViewById(R.id.btn_start);
        btnStop = (Button) findViewById(R.id.btn_stop);
        Spinner chartspinner = (Spinner) findViewById(R.id.chartspinner);
        Spinner reqspinner = (Spinner) findViewById(R.id.reqspinner);
        final String chartlen[] = getResources().getStringArray(R.array.chartlen);
        final String req[] = getResources().getStringArray(R.array.req);
        // simple_spinner_item，support_simple_spinner_dropdown_item系统默认的样式
        chartadapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, chartlen);
        chartadapter.setDropDownViewResource(R.layout.spinner_drop_item);
        chartspinner.setAdapter(chartadapter);
        reqadapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, req);
        reqadapter.setDropDownViewResource(R.layout.spinner_drop_item);
        reqspinner.setAdapter(reqadapter);

        //为抽样点spinner绑定监听器
        chartspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.w(TAG, "onItemSelected: "+ chartadapter.getItem(position));
                chartLength = Integer.valueOf(chartadapter.getItem(position));
                shaConfig.saveConfig("ChartLength",chartLength);
                chartThread.setLength(chartLength);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //为频率spinner绑定监听器
        reqspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.w(TAG, "onItemSelected: "+ reqadapter.getItem(position));
                chartFreq = Integer.valueOf(reqadapter.getItem(position));
                shaConfig.saveConfig("chartFreq",chartFreq);
                chartThread.setmFreq(chartFreq);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        //重启图表
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawThread.start();
            }
        });
        //停止刷新
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chartThread.stopHandler();
                drawThread.interrupt();
            }
        });
    }

}

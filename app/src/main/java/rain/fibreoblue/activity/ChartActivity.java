package rain.fibreoblue.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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
import rain.fibreoblue.utils.QueueController;


public class ChartActivity extends Activity{

    private static final String TAG = "ChartActivity";
    private LineChartView lineChart;
    private static Integer length = 24;
    private QueueController queueController;
    private Button btn1,btn2,btnStart,btnStop;
    private DrawLineThread chartThread;
    private Thread drawThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        lineChart = (LineChartView) findViewById(R.id.line_chart);
        queueController = new QueueController(48);
        init();
        chartThread = new DrawLineThread(length, lineChart,queueController);
        drawThread = new Thread(chartThread);
        drawThread.start();
    }


    public void init() {
        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        btnStart = (Button) findViewById(R.id.btn_start);
        btnStop = (Button) findViewById(R.id.btn_stop);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                length = 24;
                chartThread.setLength(length);
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                length = 20;
                chartThread.setLength(length);
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

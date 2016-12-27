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


public class ChartActivityOld extends Activity{

    private static final String TAG = "ChartActivity";
    private LineChartView lineChart;
    String[] date = {"5-23", "5-22", "6-22", "5-23", "5-22", "2-22", "5-22", "4-22", "9-22", "10-22", "11-22", "12-22", "1-22", "6-22", "5-23", "5-22", "2-22", "5-22", "4-22", "9-22", "10-22", "11-22", "12-22", "4-22", "9-22", "10-22", "11-22", "zxc"};//X轴的标注
    int[] score = {74, 22, 18, 79, 20, 74, 20, 74, 42, 90, 74, 42, 90, 50, 42, 90, 33, 10, 74, 22, 18, 79, 20, 74, 22, 18, 79, 20};//图表的数据
    private static Integer length = 24;
    private List<PointValue> mPointValues = new ArrayList<PointValue>();
    private List<AxisValue> mAxisXValues = new ArrayList<AxisValue>();

    private QueueController queueController;
    private Button btn1,btn2,btnStop;
    DrawLineThread chartThread;
    //定时任务
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        lineChart = (LineChartView) findViewById(R.id.line_chart);
        queueController = new QueueController(48);
        init();
        chartThread = new DrawLineThread(length, lineChart,queueController);
        Thread test = new Thread(chartThread);
        test.start();


        /*getAxisXLables();//获取x轴的标注
        getAxisPoints();//获取坐标点
        initLineChart();//初始化*/
    }

    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            getAxisPoints();//获取坐标点
            initLineChart();//初始化
            handler.postDelayed(this,40);
        }
    };

    public void init() {
        getAxisXLables();//获取x轴的标注
        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
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
        //停止刷新
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(runnable);
            }
        });
    }

    /**
     * 初始化LineChart的一些设置
     */
    private void initLineChart() {
        Line line = new Line(mPointValues).setColor(Color.parseColor("#FFCD41"));  //折线的颜色
        List<Line> lines = new ArrayList<Line>();
        line.setShape(ValueShape.CIRCLE);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.SQUARE）
        line.setCubic(false);//曲线是否平滑
	    line.setStrokeWidth(1);//线条的粗细，默认是3
        line.setFilled(false);//是否填充曲线的面积
        line.setHasLabels(true);//曲线的数据坐标是否加上备注
//		line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
        line.setHasLines(true);//是否用直线显示。如果为false 则没有曲线只有点显示
        line.setHasPoints(true);//是否显示圆点 如果为false 则没有原点只有点显示
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);

        //坐标轴
        Axis axisX = new Axis(); //X轴
        axisX.setHasTiltedLabels(true);  //X轴下面坐标轴字体是斜的显示还是直的，true是斜的显示
//	    axisX.setTextColor(Color.WHITE);  //设置字体颜色
        axisX.setTextColor(Color.parseColor("#D6D6D9"));//灰色

//	    axisX.setName("未来几天的天气");  //表格名称
        axisX.setTextSize(11);//设置字体大小
        axisX.setMaxLabelChars(length); //最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisValues.length
        axisX.setValues(mAxisXValues);  //填充X轴的坐标名称
        data.setAxisXBottom(axisX); //x 轴在底部
//	    data.setAxisXTop(axisX);  //x 轴在顶部
        axisX.setHasLines(true); //x 轴分割线


        Axis axisY = new Axis();  //Y轴
        axisY.setName("");//y轴标注
        axisY.setTextSize(11);//设置字体大小
        data.setAxisYLeft(axisY);  //Y轴设置在左边
        //data.setAxisYRight(axisY);  //y轴设置在右边
        //设置行为属性，支持缩放、滑动以及平移
        lineChart.setInteractive(true);
        lineChart.setZoomType(ZoomType.HORIZONTAL);  //缩放类型，水平
        lineChart.setMaxZoom((float) 3);//缩放比例
        lineChart.setLineChartData(data);
        lineChart.setVisibility(View.VISIBLE);
        /**注：下面的7，10只是代表一个数字去类比而已
         * 尼玛搞的老子好辛苦！！！见（http://forum.xda-developers.com/tools/programming/library-hellocharts-charting-library-t2904456/page2）;
         * 下面几句可以设置X轴数据的显示个数（x轴0-7个数据），当数据点个数小于（29）的时候，缩小到极致hellochart默认的是所有显示。当数据点个数大于（29）的时候，
         * 若不设置axisX.setMaxLabelChars(int count)这句话,则会自动适配X轴所能显示的尽量合适的数据个数。
         * 若设置axisX.setMaxLabelChars(int count)这句话,
         * 33个数据点测试，若 axisX.setMaxLabelChars(10);里面的10大于v.right= 7; 里面的7，则
         刚开始X轴显示7条数据，然后缩放的时候X轴的个数会保证大于7小于10
         若小于v.right= 7;中的7,反正我感觉是这两句都好像失效了的样子 - -!
         * 并且Y轴是根据数据的大小自动设置Y轴上限
         * 若这儿不设置 v.right= 7; 这句话，则图表刚开始就会尽可能的显示所有数据，交互性太差
         */
        Viewport v = new Viewport(lineChart.getMaximumViewport());
        v.left = 0;
        v.right = length;
        lineChart.setCurrentViewport(v);
    }

    /**
     * X 轴的显示
     */
    private void getAxisXLables() {
        for (Integer i = 0; i < length; i++) {
            mAxisXValues.add(new AxisValue(i).setLabel(i.toString()));
        }
    }

    /**
     * 图表的每个点的显示
     */
    private void getAxisPoints() {
        Queue<String> queue = queueController.getQueue();
        ArrayList<String> tempList = new ArrayList<>();
        for (String strTemp : queue) {
            tempList.add(strTemp);
        }
        for (int i = 0; i < length; i++) {
            Log.i(TAG, "getAxisPoints: "+length);
            mPointValues.add(new PointValue(i, Float.valueOf(tempList.get(i))));
        }
    }
}

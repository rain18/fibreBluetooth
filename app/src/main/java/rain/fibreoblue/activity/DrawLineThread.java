package rain.fibreoblue.activity;

import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.View;

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
import rain.fibreoblue.utils.CtrlProperty;
import rain.fibreoblue.utils.QueueController;

/**
 * Created by rain on 2016/12/26.
 */
public class DrawLineThread implements Runnable{
    private static final String TAG = "DrawLineThread";
    private static Integer mFreq; //刷新的频率
    private static Integer nlength; //图的点数
    private static Integer blueFilter = Integer.valueOf(CtrlProperty.getDefaultProperties().getProperty("BlueFilter")); //drop的点数
    private LineChartView lineChart;
    private QueueController queueController;
    private Handler handler = new Handler();

    private List<PointValue> mPointValues = new ArrayList<PointValue>();
    private List<AxisValue> mAxisXValues = new ArrayList<AxisValue>();

    public DrawLineThread(Integer mFreq, Integer nlength,LineChartView lineChart,QueueController queueController) {
        this.mFreq = mFreq;
        this.nlength = nlength;
        this.lineChart = lineChart;
        this.queueController = queueController;
    }

    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            getAxisPoints();//获取坐标点
            initLineChart();//初始化
            handler.postDelayed(this,mFreq);
        }
    };

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

	    axisX.setName("光纤检测");  //表格名称
        axisX.setTextSize(5);//设置字体大小
        axisX.setMaxLabelChars(nlength); //最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisValues.length
        axisX.setValues(mAxisXValues);  //填充X轴的坐标名称
        data.setAxisXBottom(axisX); //x 轴在底部
//	    data.setAxisXTop(axisX);  //x 轴在顶部
        axisX.setHasLines(true); //x 轴分割线


        Axis axisY = new Axis();  //Y轴
        axisY.setName("");//y轴标注
        axisY.setTextSize(5);//设置字体大小
        data.setAxisYLeft(axisY);  //Y轴设置在左边
        //data.setAxisYRight(axisY);  //y轴设置在右边
        //设置行为属性，支持缩放、滑动以及平移
        lineChart.setInteractive(true);
        lineChart.setZoomType(ZoomType.HORIZONTAL);  //缩放类型，水平
        lineChart.setMaxZoom((float) nlength);//缩放比例
        lineChart.setLineChartData(data);
        lineChart.setVisibility(View.VISIBLE);
//        Viewport v = new Viewport(lineChart.getMaximumViewport());
//        v.left = 0;
//        v.right = nlength;
//        lineChart.setCurrentViewport(v);
    }

    /**
     * X 轴的显示
     */
    private void getAxisXLables() {
        for (Integer i = 0; i < nlength; i++) {
            mAxisXValues.add(new AxisValue(i).setLabel(i.toString()));
        }
    }

    /**
     * 图表的每个点的显示
     */
    private void getAxisPoints() {
        Queue<String> queue = queueController.getQueue();

        mPointValues.clear(); //手动清除，就不会用数据残留了
//        for (int i=0; i<nlength; i++) {
//            mPointValues.add(new PointValue(i, 3));
//        }
        Integer strtemp = 0;
        for (String str:queue) {
            if (strtemp < nlength) {
//                Log.w(TAG, "getAxisPoints: "+str );
                mPointValues.add(new PointValue(strtemp, Float.valueOf(str)));
                strtemp++;
            } else {
                break;
            }
        }
        String filterStr="0"; //16进制的字符串
        for (int i=0;i<blueFilter;i++) {
            filterStr = MainActivity.queueController.poolElement();
            Log.w(TAG, "filter: " + filterStr);
        }
        if (null==filterStr) {
            filterStr = "0";
        }
        Integer hex_result = Integer.parseInt(filterStr,16);
        queueController.insert(hex_result.toString());
    }

    //设置新的长度，并且刷新
    public void setLength(Integer length) {
        this.nlength = length;
        getAxisXLables();
    }
    //设置刷新频率
    public void setmFreq(Integer mFreq) {
        this.mFreq = mFreq;
    }

    //启动线程
    public void run() {
        getAxisXLables();//获取x轴的标注
        getAxisPoints();//获取坐标点
        initLineChart();//初始化
        handler.postDelayed(runnable,mFreq);
    }

    //可能为空
    public void stopHandler() {
        handler.removeCallbacks(runnable);
    }
}

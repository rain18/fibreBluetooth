package rain.fibreoblue.utils;

import android.os.Handler;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by rain on 2016/12/25.
 */
public class QueueController {

    private static final String TAG="queueController:";
    private Queue<String> queue = new LinkedList<>();
    private Integer length;
    private Handler handler = new Handler();
    public QueueController(){}

    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            String temp = Double.toString(Math.random()*10);
            insert(temp);
            handler.postDelayed(this,400);
        }
    };

    public QueueController(Integer length) {
        this.length = length;
//        for (Integer i=0;i<48;i++) {
//            String temp = Double.toString(Math.random()*10);
//            Log.i(TAG, temp);
//            insert(temp);
//        }
//        handler.postDelayed(runnable,400);
    }

    //队列中插入新的元素
    public void insert(String str) {
        if (queue.size()==length) {
            queue.poll();
        }
        queue.offer(str);
    }

//    public String filter(Integer pnum) {
//        Integer poolSum = 0;
//        for (int i=0;i<pnum;i++) {
//            poolSum+=Integer.valueOf(queue.element());
//        }
//        return String.valueOf(poolSum/pnum);
//    }

    //返回队列的第一个
    public String elementFirst() {
        return queue.element();
    }

    //队列是否为空
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public void LogQueue() {
        for (String temp:queue) {
            Log.i(TAG, temp);
        }
    }

    //更改队列的长度
    public void extentLength(Integer newLength) {
        this.length = newLength;
    }

    public Queue<String> getQueue() {
        return queue;
    }

    public void setQueue(Queue<String> queue) {
        this.queue = queue;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }
}
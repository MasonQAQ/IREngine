package com.yangyue;

import com.yangyue.crawler.TaskSchedule;

/**
 * Created by yangyue on 2016/11/1.
 */
public class Init {
    public static void main(String[] args) {
        TaskSchedule taskSchedule = TaskSchedule.getInstance();// 计划任务
        Thread thread = new Thread(taskSchedule);// 创建线程
        TaskSchedule.changeStatus(true);
        thread.start();//运行
    }
}

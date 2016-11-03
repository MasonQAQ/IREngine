package com.yangyue;


import com.yangyue.crawler.SinaNewsCrawler;
import com.yangyue.crawler.SohuNewsCrawler;
import com.yangyue.crawler.TaskSchedule;

import java.util.Scanner;

/**
 * Created by yangyue on 2016/11/1.
 */
public class Init {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入要执行的任务:\n\t1:计划任务(需要semiAgent(port8000)支持);\n\t2:抓取新浪300页历史记录(也需要semiAgent);\n\t3:抓取搜狐新闻数据(不需要SemiAgent)");
        switch (scanner.nextInt()){
            case 1:
                /**
                 * 新浪新闻计划任务,每5分钟抓一次
                 */
                TaskSchedule taskSchedule = TaskSchedule.getInstance();// 计划任务
                Thread thread = new Thread(taskSchedule);// 创建线程
                TaskSchedule.changeStatus(true);
                thread.start();//运行
                break;
            case 2:
                /**
                 * 抓取新浪277页内容
                 */
                for (int i=277;i>=0;i--){
                    String url="http://roll.news.sina.com.cn/s/channel.php#col=89&spec=&type=&ch=&k=&offset_page=0&offset_num=0&num=60&asc=&page="+i;
                    SinaNewsCrawler sinaNewsCrawller1 = new SinaNewsCrawler(url);
                    sinaNewsCrawller1.getNews(sinaNewsCrawller1.proxy());
                }
                break;
            case 3:
                /**
                 * 抓取搜狐277页内容
                 */
                for (int i=1001;i<1030;i++){
                    String url="http://news.sohu.com/_scroll_newslist/2016"+i+"/news.inc";
                    SohuNewsCrawler sohuNewsCrawler = new SohuNewsCrawler(url);
                    sohuNewsCrawler.getNews();
                }
                break;
            default:
                System.out.println("输入有误");
        }
    }
}

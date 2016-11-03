package com.yangyue.crawler;

import com.alibaba.fastjson.JSONArray;
import com.yangyue.dao.bean.NewsBean;
import com.yangyue.dao.imp.NewsDao;
import com.yangyue.dao.inf.NewsDaoInf;
import com.yangyue.tools.HttpRequest;
import com.yangyue.tools.NewsCrawlerHelper;

/**
 * Created by yangyue on 2016/11/2.
 */
public class SohuNewsCrawler {
    private NewsDaoInf dao = new NewsDao("sohunews");// 引用dao进行对数据库的操作
    private String urlPage;
    public SohuNewsCrawler(String url){
        this.urlPage=url;
    }
    public void getNews(){
        String str = HttpRequest.sendGet(urlPage,"");
        int linkbegin = str.indexOf(",item:");// 截取<a>链接字符串起始位置
        int linkend = str.indexOf("}");// 截取<a>链接字符串结束位置
        String subString = str.substring(linkbegin + ",item:".length(), linkend);
        JSONArray jsonArray =JSONArray.parseArray(subString);
        for (Object obj:jsonArray){
            String strTemp = obj.toString();
            JSONArray jsonArrayTemp=JSONArray.parseArray(strTemp);
            String type=jsonArrayTemp.get(0).toString();
            System.out.println("抓取类型："+type);
            String title=jsonArrayTemp.get(1).toString();
            System.out.println("抓取标题："+title);
            String url=jsonArrayTemp.get(2).toString();
            System.out.println("抓取链接："+url);
            String newsdate=jsonArrayTemp.get(3).toString();
            System.out.println("抓取时间："+newsdate);
            String content= NewsCrawlerHelper.cleanNewsBody(url,"contentText");
            System.out.println("抓取内容："+content);
            NewsBean newsBean = new NewsBean(0,title,content,url,newsdate,type);
            dao.add(newsBean);
            break;
        }
    }

}

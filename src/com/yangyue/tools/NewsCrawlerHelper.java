package com.yangyue.tools;

import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * 新闻爬虫工具类
 * Created by yangyue on 2016/11/3.
 */
public class NewsCrawlerHelper {
    /**
     *
     * @param origin
     *              原始字符串
     * @param begin
     *              初始标志位
     * @param end
     *              结束标志位
     * @return
     *              截取后的字符串
     */
    public static String doSubString(String origin,String begin,String end){
        int beginIndex = origin.indexOf(begin);
        int endIndex = origin.indexOf(end);
        String body = "";
        if (beginIndex < 0 || beginIndex >= endIndex) {
            System.out.println("i am sorry!!我是doSubString 方法,我不能处理的过滤规则");
            body = origin;
        } else {
            body = origin.substring(beginIndex, endIndex);
        }
        return body.replaceAll("[\n\r]","");
    }

    /**
     *
     * @param url
     *              请求的URL
     * @param contentId
     *              新闻正文所载的div的ID名称
     * @return
     */
    public static String cleanNewsBody(String url,String contentId){
        String newstextstr = getNewsContent(url,contentId);
        // 只保留正文内容，保留P标签以保持其排版
        return cleanNewsBody(newstextstr);
    }

    /**
     *
     * @param content
     *          新闻正文信息
     * @return
     *          处理后的页面
     */
    public static String cleanNewsBody(String content){
        return doSubString(content,"<p>","</p>");
    }

    /**
     * 获取新闻正文所有信息
     * @param url
     *              请求的URL
     * @param contentId
     *              新闻正文所载的div的ID名称
     * @return
     */
    public static String getNewsContent(String url,String contentId){
        org.htmlparser.NodeFilter bodyfilter = new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("id", contentId));
        Parser bodyparser = new Parser();
        NodeList bodylist = null;
        try {
            bodyparser.setURL(url);// 地址url
            bodyparser.setEncoding("UTF-8");
            bodylist = bodyparser.extractAllNodesThatMatch(bodyfilter);
        } catch (ParserException e) {
            System.out.println("抓取信息子页面出错，出错信息为：");
            e.printStackTrace();
            return "";
        }
        // 新闻内容字符串
        if (bodylist.elementAt(0) == null) {
            System.out.println("【新闻无内容】");
            return "";
        }
        return bodylist.elementAt(0).toHtml().trim();
    }

}

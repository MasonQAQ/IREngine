package com.yangyue.crawler;

import com.yangyue.dao.bean.NewsBean;
import com.yangyue.dao.imp.NewsDao;
import com.yangyue.dao.inf.NewsDaoInf;
import com.yangyue.tools.NewsCrawlerHelper;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import java.io.IOException;


/**
 * <爬虫程序> 从新浪新闻中爬取新闻分类、标题及内容 (htmlparser.jar)
 *
 * @author yangyue
 */
public class SinaNewsCrawler {

	private String URL;//新闻链接
	private String proxyURL = "http://localhost:8000/doload";//新闻链接
	private String proxyTime="5000";
	private String ENCODING = "utf-8";
	private String LINK_TO_WORD = "\" target=\"_blank\">";//A标签 “href内容”到“文本”之间的部分
	private String TYPE_WORD=";return false;\">";//类型标识符
	private NewsDaoInf dao = new NewsDao("sinanews");// 引用dao进行对数据库的操作


	/**
	 * 构造器,传入要爬取的页面
 	 * @param url
	 * 			目标url
	 */
	public SinaNewsCrawler(String url){
		this.URL=url;
	}

	/**
	 * 通过semiAgent代理出目标URL的页面(js执行后的)
	 * @return
	 * 			目标url完整页面
	 */
	public String proxy(){
		String result="";
		HttpClient client = new HttpClient();
		PostMethod post_method = new PostMethod(proxyURL);
		NameValuePair[] data = {
				new NameValuePair("url", URL),
				new NameValuePair("renderTime ", proxyTime),
		};
		post_method.setRequestBody(data);

		int statusCode = 0;
		try {
			statusCode = client.executeMethod(post_method);
			if (statusCode != HttpStatus.SC_OK) {
				System.err.println("Method failed: " + post_method.getStatusLine());
			}
			result=new String(post_method.getResponseBody());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 获取新闻并存入数据库
	 * @param html
	 * 			目标完整页面
	 */
	public void getNews(String html) {

		NodeFilter filter = new TagNameFilter("ul");
		Parser parser = new Parser();
		NodeList list = null;
		try {
			parser.setInputHTML(html);
			parser.setEncoding(ENCODING);
			list = parser.extractAllNodesThatMatch(filter);
		} catch (ParserException e) {
			System.out.println("抓取信息出错，出错信息为：" + e.getMessage());
			e.printStackTrace();
		}
		for (int i = 0; i < list.size(); i++) {

			Tag node = (Tag) list.elementAt(i);
			for (int j = 0; j < node.getChildren().size(); j++) {
				NodeList temp = node.getChildren().elementAt(j).getChildren();
				String strType=temp.elementAt(0).toHtml().trim();
				String textStr = temp.elementAt(1).toHtml().trim()+temp.elementAt(2).toHtml().trim();
				String type=getNewsType(strType);
				System.out.println("新闻类型："+type);
				String link = getLink(textStr);// 获取链接
				System.out.println("新闻链接："+link);
				String title = getTitle(textStr);// 获取标题
				System.out.println("新闻标题："+title);
				String body = getNewsBody(link);// 获取内容
				if (!"".equals(link)&& !"".equals(title) && !"".equals(body)) {
					/** 写入数据库 */
					NewsBean newsBean = new NewsBean(0, title, body, link,
							link.substring(link.lastIndexOf("/") - 10,
									link.lastIndexOf("/")), type);
					dao.add(newsBean);
				}
			}
		}
	}

	/**
	 * 获得A标签中的链接
	 *
	 * @param texrStr
	 *            抓取下来页面转换出的字符串
	 * @return 子页面链接
	 */
	private String getLink(String texrStr) {
		// 链接字符串
		String link = "";
		if (texrStr.length() > 0) {
			String sublink= NewsCrawlerHelper.doSubString(texrStr,"href=\"",LINK_TO_WORD);
			if (sublink.indexOf("target") != -1) {
				link = sublink.substring(0, sublink.indexOf("\""));
			} else {
				link = sublink;// 链接字符串
			}
		}
		return link;
	}

	/**
	 * 获取A标签中的文本内容
	 *
	 * @param textStr
	 *            抓取下来页面转换出的字符串
	 * @return 标题
	 */
	private String getTitle(String textStr) {
		if (textStr.length() <= 0) {
			return "";
		}
		String title = NewsCrawlerHelper.doSubString(textStr,LINK_TO_WORD,"</a>").trim();
		// 通过标题判断该新闻是否已经存在
		if (title.contains("视频:") || title.contains("视频：")) {
			System.out.println("【无法获得视频新闻】");
			return "";
		}
		if (title.contains("(图)")) {
			title = title.replace("(图)", "");
		}
		if (dao.hasNews(title)) {
			System.out.println("【该记录已经存在】");
			return "";
		}

		return title;
	}

	/**
	 * 新闻内容处理
	 *
	 * @param link
	 *            内容的链接
	 * @return 内容
	 */
	private String getNewsBody(String link) {
		String newstextstr=NewsCrawlerHelper.getNewsContent(link,"artibody");
		// 只保留正文内容，保留P标签以保持其排版
		String body=NewsCrawlerHelper.doSubString(newstextstr,"<p>","</p>");
		body=NewsCrawlerHelper.doSubString(body,"<div class=\"img_wrapper\">","<span class=\"img_descr\">");
		body=NewsCrawlerHelper.doSubString(body,"<div id=\"news_like\"","<p class=\"fr\">");
		return body;
	}


	/**
	 * 获取新闻类型
	 * @param text
	 * 				内容
	 * @return
	 */
	private String getNewsType(String text){
		if (text.length() <= 0) {
			return "";
		}
		if(text.contains("其他")){
			return "其他";
		}
		return NewsCrawlerHelper.doSubString(text,TYPE_WORD,"</a>");
	}

}
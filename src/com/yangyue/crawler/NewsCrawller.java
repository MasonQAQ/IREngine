package com.yangyue.crawler;

import com.yangyue.dao.bean.NewsBean;
import com.yangyue.dao.imp.NewsDao;
import com.yangyue.dao.inf.NewsDaoInf;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import java.io.IOException;


/**
 * <爬虫程序> 从新浪新闻中爬取新闻分类、标题及内容 (htmlparser.jar)
 *
 * @author yangyue
 */
public class NewsCrawller {

	private final static String URL = "http://roll.news.sina.com.cn/s/channel.php?ch=01";//新闻链接
	private final static String proxyURL = "http://localhost:8000/doload";//新闻链接
	private final static String proxyTime="5000";
	private final static String ENCODING = "utf-8";
	private final static String LINK_TO_WORD = "\" target=\"_blank\">";//A标签 “href内容”到“文本”之间的部分
	private final static String TYPE_WORD=";return false;\">";//类型标识符
	private static NewsDaoInf dao = new NewsDao();// 引用dao进行对数据库的操作

	public static void main(String[] args) {
		getNews(proxy());
	}
	public static String proxy(){
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

	public static void getNews(String html) {

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
				String link = getLink(textStr);// 获取链接
				System.out.println("抓取链接："+link);
				String title = getTitle(textStr);// 获取标题
				System.out.println("抓取标题："+title);
				String body = getNewsBody(link);// 获取内容
				System.out.println("抓取内容："+body);
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
	private static String getLink(String texrStr) {
		// 链接字符串
		String link = "";
		if (texrStr.length() > 0) {
			int linkbegin = texrStr.indexOf("href=\"");// 截取<a>链接字符串起始位置
			int linkend = texrStr.indexOf(LINK_TO_WORD);// 截取<a>链接字符串结束位置
			String sublink = texrStr.substring(linkbegin + "href=\"".length(),
					linkend);

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
	private static String getTitle(String textStr) {
		if (textStr.length() <= 0) {
			return "";
		}
		int titlebegin = textStr.indexOf(LINK_TO_WORD);
		int titleend = textStr.indexOf("</a>]</span>");
		String title = textStr.substring(titlebegin + LINK_TO_WORD.length(),
				titleend).trim();
		System.out.println("正在抓取: " + title);
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
	private static String getNewsBody(String link) {
		NodeFilter bodyfilter = new AndFilter(new TagNameFilter("div"),
				new HasAttributeFilter("id", "artibody"));
		Parser bodyparser = new Parser();
		NodeList bodylist = null;
		try {
			bodyparser.setURL(link);// 地址url
			bodyparser.setEncoding(ENCODING);
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
		String newstextstr = bodylist.elementAt(0).toHtml().trim();
		// 只保留正文内容，保留P标签以保持其排版
		int bodybegin = newstextstr.indexOf("<p>");
		int bodyend = newstextstr.lastIndexOf("</p>") + 4;

		String body = "";
		if (bodybegin < 0 || bodybegin >= bodyend) {
			body = newstextstr;
		} else {
			body = newstextstr.substring(bodybegin, bodyend);
		}
		int bodyimgbegin = newstextstr.indexOf("<div class=\"img_wrapper\">");
		int bodyimgend = newstextstr.lastIndexOf("<span class=\"img_descr\">");
		if (bodyimgbegin >= 0 && bodyimgbegin < bodyimgend) {
			body = newstextstr.substring(bodyimgbegin, bodyimgend) + "</div>"
					+ body;
		}
		int bodyremovebegin = body.indexOf("<div id=\"news_like\"");
		int bodyremoveend = body.lastIndexOf("<p class=\"fr\">");
		if (bodyremovebegin > 0 && bodyremovebegin < bodyremoveend) {
			body = body.replace(body.substring(bodyremovebegin, bodyremoveend),
					"");
		}
		return body;

	}


	/**
	 * 获取新闻类型
	 * @param text
	 * 				内容
	 * @return
	 */
	private static String getNewsType(String text){
		if (text.length() <= 0) {
			return "";
		}
		int titlebegin = text.indexOf(TYPE_WORD);
		int titleend = text.indexOf("</a>");
		String title = text.substring(titlebegin + TYPE_WORD.length(),
				titleend).trim();
		System.out.println("抓取类型: " + title);

		return title;
	}

}
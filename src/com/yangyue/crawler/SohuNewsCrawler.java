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
            String content= NewsCrawlerHelper.getNewsContent(url,"articleBody");
            System.out.println("抓取内容："+content);
            /**
             <div class="text clear" id="contentText">
             <div itemprop="articleBody">
             <p>　　北京时间1月1日消息，元旦大战，斯蒂芬-马布里真急了，他甚至在次节用肩膀顶撞裁判，结果被吹了技术犯规。末节老马开启个人攻击模式，结果5投3中单节砍下7分。整场比赛，“马政委”13投5中交出17分6助攻2抢断1篮板的数据。</p>
             <p>　　2016年首战，老马打得很郁闷，他在第三节甚至没有1次运动战出手，首节也只在外线投了2球。比赛进行期间，老马总是在场上抱怨。“这是老马着急的表现，”著名评论员孟晓琦说道。</p>
             <p>　　次节还剩8分30秒，深圳队大外援麦克奎尔在1次进攻中造成莫里斯犯规。在一旁的老马对这一判罚颇为不满，在申辩过程中，老马有一个用肩膀顶撞裁判的动作。于是，裁判果断给了老马一个T。也就是在麦克奎尔执行完2次罚球后，又追加给深圳队1罚1掷。</p>
             <p>　　“老马的这个动作有些不应该，不管是有意还是无意，裁判这个T没有任何问题，”孟晓琦说道。</p>
             <p>　　前三节北京队落后18分，按照常规末节伊始应该是莫里斯上场。但今晚，闵鹿蕾做出调整，老马第四节一开始就在场上。这标志着，北京队展开最后一搏。是的，老马开启个人攻击模式--他先是连续2次强突上篮得分，紧接着在外线命中三分。但之后，老马又在外线2次出手，结果都没命中。老马连砍7分并没有帮助北京队缩小分差，反倒是深圳队确立20+优势。</p>
             <p>　　在这样的背景下，老马末节只打了不到半节便被换下场。今晚，老马出手13次，其中三分10投仅3中。显然，马布里和北京队都需要调整。本周日，卫冕冠军将客战老对手广东队，这又将是一场硬仗。（jimmy）</p>
             </div>
             <div style="display:none;">
             <span id="url" itemprop="url">http://sports.sohu.com/20160101/n433249559.shtml</span>
             <span id="indexUrl" itemprop="indexUrl">sports.sohu.com</span>
             <span id="isOriginal" itemprop="isOriginal">true</span>
             <span id="sourceOrganization" itemprop="sourceOrganization" itemscope itemtype="http://schema.org/Organization"><span itemprop="name">搜狐体育</span></span>
             <span id="author" itemprop="author" itemscope itemtype="http://schema.org/Organization"><span itemprop="name">jimmy</span></span>
             <span id="isBasedOnUrl" itemprop="isBasedOnUrl">http://sports.sohu.com/20160101/n433249559.shtml</span>
             <span id="genre" itemprop="genre">report</span>
             <span id="wordCount" itemprop="wordCount">684</span>
             <span id="description" itemprop="description">北京时间1月1日消息，元旦大战，斯蒂芬-马布里真急了，他甚至在次节用肩膀顶撞裁判，结果被吹了技术犯规。末节老马开启个人攻击模式，结果5投3中单节砍下7分。整场比</span>
             </div>
             </div>
             */
            NewsBean newsBean = new NewsBean(0,title,content,url,newsdate,type);
            dao.add(newsBean);
        }
    }
}

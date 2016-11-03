package com.yangyue.dao.inf;


import com.yangyue.dao.bean.NewsBean;

import java.util.List;

public interface NewsDaoInf {
	void add(NewsBean newsBean);
	void delete();
	boolean hasNews(String title);
	List<NewsBean> query();
	NewsBean queryOne(String id);
	
}

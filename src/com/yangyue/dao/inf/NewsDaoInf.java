package com.yangyue.dao.inf;


import com.yangyue.dao.bean.NewsBean;

import java.util.List;

public interface NewsDaoInf {
	public void add(NewsBean newsBean);
	public void delete();
	public boolean hasNews(String title);
	public List<NewsBean> query();
	public NewsBean queryOne(String id);
	
}

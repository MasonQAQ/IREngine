# 信息检索爬虫程序


## 简介
> 本爬虫程序目前可爬新浪、搜狐网站的新闻<br />
搜狐新闻：http://news.sohu.com/scroll/ <br />
新浪新闻：http://roll.news.sina.com.cn/s/channel.php

## 环境需求
* MySQL 环境，数据库：news, 表：sinanews 、sohunews 表结构如下<br />
`CREATE TABLE `sohunews` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `title` varchar(512) NOT NULL,
  `content` text,
  `url` varchar(128) NOT NULL,
  `newsdate` varchar(128) NOT NULL,
  `type` varchar(128) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=136 DEFAULT CHARSET=utf8;`

* 对于新浪新闻的爬取需要SeimiAgent支持，确保端口号为8000




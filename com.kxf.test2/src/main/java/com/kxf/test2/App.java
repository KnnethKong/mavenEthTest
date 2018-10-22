package com.kxf.test2;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSONObject;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		getCoins();
	}

	public static void getCoins() {
		try {
			String html = SimpleHttpUtils.get(
					"https://etherscan.io/tokenholdingsHandler.ashx?&a=0x897081cb98b838b4fff8a1fceec4fad188ca9281&q=&p=1&f=0&h=0&sort=total_price_usd&order=desc&pUsd24hrs=&pBtc24hrs=&pUsd=&fav=");
			JSONObject jsonObject = JSONObject.parseObject(html);
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("<table>").append(jsonObject.getString("layout"));
			Document document = Jsoup.parse(stringBuffer.toString());
			Elements document2 = document.select("tr");
			for (Element element3 : document2) {
				Elements addreEles = element3.getElementsByClass("addresstag");
				Element addreEle = addreEles.get(0).child(0);
				String src = addreEle.childNode(0).attr("src");
				String tokenCode = null, tokenName = null, tokenNum = null, tokenImg = null;
				if (src == null || src.equals("")) {
					Element element5 = addreEle.child(0);
					tokenImg = element5.attr("href");
					tokenName = element5.html();
				} else {// eth
					String htmls = addreEle.html();
					tokenName = htmls.substring(htmls.indexOf(">") + 1, htmls.length());
					tokenImg=src;
				}
				Element element = (Element) element3.child(3);
				if (element != null) {
					tokenNum = element.html();
				}
				tokenCode=tokenName.substring(tokenName.indexOf("(")-1, tokenName.indexOf(")"));
				System.out.println( tokenName +"   "+tokenCode+"    "+tokenImg+"    "+tokenNum);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}

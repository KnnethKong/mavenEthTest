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
		
	}
	
	private void getCoins() {
		try {
			String html = SimpleHttpUtils.get(
					"https://etherscan.io/tokenholdingsHandler.ashx?&a=0x897081cb98b838b4fff8a1fceec4fad188ca9281&q=&p=1&f=0&h=0&sort=total_price_usd&order=desc&pUsd24hrs=&pBtc24hrs=&pUsd=&fav=");
			JSONObject jsonObject = JSONObject.parseObject(html);
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("<table>").append(jsonObject.getString("layout"));
			Document document = Jsoup.parse(stringBuffer.toString());
			Elements document2 = document.select("tr");
			for (Element element3 : document2) {

				String tokenAddress = element3.getElementsByClass("hex address-tag").select("a").html();
				String tokenName = element3.getElementsByClass("rounded-x").get(0).html();
				String tokenImage =		element3.select("img").get(0).attr("src");
				String tokenNum=element3.getElementsByAttribute("position:relative").html();
				
				//String tokenNum = element3.select("td").get(3).html();
				System.err.println(tokenAddress);
				System.err.println(tokenName);
				System.err.println(tokenImage);
				System.err.println(tokenNum);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
}

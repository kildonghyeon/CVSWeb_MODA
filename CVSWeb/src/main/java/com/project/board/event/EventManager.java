package com.project.board.event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

// 추후에 void를 list 형태로 리턴해서 컨트롤러에서 DB에 넣을 수 있도록 수정 예정
// i값, j값, 선택자, 변수명 등은 사이트별로 맞게 조절해서 사용할 것
// 얻어와야할 값: 제목, 내용, 날짜 => EventboardVO 참고 
public class EventManager {
	
	String targetSite = "";
	Document doc = null;
	
	public ArrayList<EventboardVO> getGS25Event() {
	ArrayList<EventboardVO> list = new ArrayList<EventboardVO>();
		
		
		try {
			int page = 0;
			while(true) {
				page++;
				targetSite = "http://gs25.gsretail.com/board/boardList?CSRFToken=8f35eb2a-1562-44be-9ff0-390b02611142";
				// gs 행사페이지 접속
				Thread.sleep(500); 
				Connection.Response response = Jsoup.connect(targetSite)
						.timeout(3000)
						.data("pageNum", page+"")
						.data("modelName", "event")
						.data("parameterList", "brandCode:GS25@!@eventFlag:CURRENT")
						.ignoreContentType(true)
						.method(Connection.Method.GET)
						.execute();
				
				// 페이지에 접속했으면 파싱 후 값을 doc에 전달한다.
				doc = response.parse();
				
				// text() 메소드를 이용해서 내용을 문자열로 옮겨준다.
				String str = doc.select("body").text();
				
//				========================== 23.02.20 코드 수정 ===================================
//				String str = doc.select("body").text().replaceAll("\\\\", "").substring(1); // "\" 제거, 첫번째 글자 제거
//				
//				str = str.substring(0, str.length() - 1); // 마지막 글자 제거
//				========================== 23.02.20 코드 수정 ===================================
				
				// JSON데이터 형태로 만든 문자열 str을 넣어 JSON Object 로 만들어 준다.
				JSONObject jsonObj = (JSONObject) new JSONParser().parse(str);
				
				// JSON Object에서 필요한 정보가 있는, 즉 results 배열을 추출한다.
				JSONArray jsonArr = (JSONArray) jsonObj.get("results");	
				
				if(jsonArr.size() == 0) {
					break;
				}
				
				// 배열의 사이즈만큼 배열을 돌려준다.
				for(int i=0; i<jsonArr.size(); i++) {
					JSONObject gs25Object = (JSONObject) jsonArr.get(i);
					
					String eventCode = gs25Object.get("eventCode").toString();
					
					targetSite = 
							String.format("http://gs25.gsretail.com/gscvs/ko/customer-engagement/event/detail/"
									+ "publishing?pageNum=%d&eventCode=%s", page, eventCode);
					Thread.sleep(500); 
					doc = Jsoup.connect(targetSite).get();
					
					if(doc.text().trim().length() == 0) {
						targetSite = 
								String.format("http://gs25.gsretail.com/gscvs/ko/customer-engagement/event/detail/"
										+ "stamp?pageNum=%d&eventCode=%s", page, eventCode);
						Thread.sleep(500); 
						doc = Jsoup.connect(targetSite).get();
					}
					
					String subject = doc.select("h3.tit > strong").text(); // 행사제목
					
					Elements ps = doc.select("span.event-web-contents > p");
					String image = ps.select("img").attr("src"); // 행사이미지
					
					if(image.trim().length() == 0) {
						ps = doc.select("div.visible > p");
						image = ps.select("img").attr("src"); // 행사이미지
					}
					
					if(image != null && !image.equals("")) { 
						EventboardVO eventboardVO = new EventboardVO();
						eventboardVO.setEv_sellcvs("GS25");
						eventboardVO.setEv_subject(subject);
						eventboardVO.setEv_content(".");
						eventboardVO.setEv_filename(image);
						eventboardVO.setId("admin1");
						eventboardVO.setNickname("관리자1");
						eventboardVO.setEv_notice("no");
						
						list.add(eventboardVO);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	   public ArrayList<EventboardVO> getCUEvent() {
		      ArrayList<EventboardVO> list = new ArrayList<EventboardVO>();
		      
				try {
					int page = 0;
					while (true) {
						targetSite = "https://cu.bgfretail.com/brand_info/news_listAjax.do";
						page++;
						Map<String, String> data = new HashMap<String, String>();
						data.put("pageIndex", page + "");
						data.put("idx", 0 + "");
						data.put("search2", "");
						data.put("searchKeyword", "");
						data.put("searchCondition", "");

						Thread.sleep(500);
						doc = Jsoup.connect(targetSite)
								.data(data)
								.post();
						
			            Date date = new Date();
			            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM");

			            Elements dds = doc.select("dl > dd"); // 행사 날짜
			            String stop = "n"; // while 반복을 끝낼 때 사용
			            
			            Elements elements = doc.select("tbody > tr");
			            
						for (int i = 0; i < dds.size(); i++) {
							if (!dds.get(i).text().trim().substring(0, 7).equals(sdf.format(date))) {
								stop = "y"; // 현재 월과 사이트에 등록된 월이 같지 않으면 while문을 멈추기 위해 stop의 값을 y로 바꿔준다.
								break;
							} else {
								String idx[] = elements.get(i).select("h3 > a").attr("href").split("'");
								targetSite = "https://cu.bgfretail.com/brand_info/news_view.do?category=brand_info&depth2=5&idx="+idx[1];
								
								Thread.sleep(500);

								doc = Jsoup.connect(targetSite)
										.header("Origin", "https://cu.bgfretail.com")
										.header("Referer", "https://cu.bgfretail.com/brand_info/news_list.do?category=brand_info&depth2=5&sf=N")
										.cookie("JSESSIONID", "Z1dGjLYJpky11TnymspbhCGBSJZyy5cNyYv1GXJjL1MGLMjgLycS!-578416747")
										.data("pageIndex", page + "")
										.data("search2", "")
										.data("searchKeyword", "")
										.data("searchCondition", "")
										.post();

								Elements tbody = doc.select("tbody");
								
								String subject = doc.select("thead th").get(0).text();
								String image = tbody.select("img").attr("src");
								String content = tbody.select("span").text();
								
								if (image != null && !image.equals("")) {
									EventboardVO eventboardVO = new EventboardVO();
									eventboardVO.setEv_sellcvs("CU");
									eventboardVO.setEv_subject(subject);
									eventboardVO.setEv_content(content);
									eventboardVO.setEv_filename(image);
									eventboardVO.setId("admin1");
									eventboardVO.setNickname("관리자1");
									eventboardVO.setEv_notice("no");

									list.add(eventboardVO);
								}   
							}
						}
						if(stop.equals("y")) {
							break;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
		      return list;
		   }
	   
	public ArrayList<EventboardVO> getSevenElevenEvent() {
		ArrayList<EventboardVO> list = new ArrayList<EventboardVO>();
		targetSite = "https://www.7-eleven.co.kr/event/eventList.asp";
		
		try {
			targetSite = "https://www.7-eleven.co.kr/event/eventList.asp";
			doc = Jsoup.connect(targetSite)
					.data("intPageSize", "100")
					.data("state", "Y")
					.post();
			
			Elements lis = doc.select("ul#listUl > li");
			
			for(int i=0; i<lis.size(); i++) {
				String subject = lis.select("dt").get(i).text(); // 행사제목
				
				String seqNo = lis.select("a.event_img").get(i).attr("href");
				int cut = seqNo.indexOf("'");
				seqNo = seqNo.substring(cut+1, cut+4);
				
				Thread.sleep(500);
				targetSite = "https://www.7-eleven.co.kr/event/eventView.asp";
				doc = Jsoup.connect(targetSite)
						.data("seqNo", seqNo)
						.post();
				
				String image = "https://www.7-eleven.co.kr" + doc.select("div.gallery_view > img").attr("src"); // 행사이미지
				
				EventboardVO eventboardVO = new EventboardVO();
				eventboardVO.setEv_sellcvs("세븐일레븐");
				eventboardVO.setEv_subject(subject);			
				eventboardVO.setEv_content(".");
				eventboardVO.setEv_filename(image);
				eventboardVO.setId("admin1");
				eventboardVO.setNickname("관리자1");
				eventboardVO.setEv_notice("no");
				list.add(eventboardVO);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}	
	
	public ArrayList<EventboardVO> getEmart24Event() {

		ArrayList<EventboardVO> list = new ArrayList<EventboardVO>();
		targetSite = "http://emart24.co.kr/event/ing";

		try {
			while (true) {
				doc = Jsoup.connect(targetSite).get();

				Elements as = doc.select("a.eventWrap");

				for (Element a : as) {
					targetSite = "http://emart24.co.kr" + a.attr("href");
					Thread.sleep(500);
					doc = Jsoup.connect(targetSite).get();

					Elements ps = doc.select("div.titlewrap > p");

					String subject = ps.select("a").text();
					String image = doc.select("div.contentWrap > img").attr("src");

					EventboardVO eventboardVO = new EventboardVO();
					eventboardVO.setEv_sellcvs("이마트24");
					eventboardVO.setEv_subject(subject);
					eventboardVO.setEv_content(".");
					eventboardVO.setEv_filename(image);
					eventboardVO.setId("admin1");
					eventboardVO.setNickname("관리자1");
					eventboardVO.setEv_notice("no");

					list.add(eventboardVO);
				}
				break;
			}
		         
		      } catch(Exception e) {
		         e.printStackTrace();
		      }
		      
		      return list;
		      
		   }
	
	public void getMinistopEvent() {
		// 행사를 반 년째 안 함...
	}
	
	public ArrayList<EventboardVO> getCspaceEvent() {
		ArrayList<EventboardVO> list = new ArrayList<EventboardVO>();		
			// System.out.println(targetSite);
			try {
				int page = 0;
				while(true) {
					page++;
					targetSite = 
							String.format("https://www.cspace.co.kr/board_event01/list.php?"
									+ "tn=board_event01"
									+ "&list_count_s="
									+ "&G_state=Y"
									+ "&pm="
									+ "&b_category=00010000000000000000"
									+ "&cpage=%d"
									+ "&spage=1"
									, page);
					Thread.sleep(500); 
					doc = Jsoup.connect(targetSite).get();
					
					Elements elesSpan = doc.select("div.hidden-xs"); // 글의 제목
					Elements elesImg = doc.select("div.img"); // 글의 이미지
					
					if(elesSpan.size() == 0) {
						break;
					}
					
					for (int j=0; j<elesSpan.size(); j++) {
						String subject = elesSpan.select("span").get(j).text(); // 행사 제목
						String image = "https://www.cspace.co.kr"+
								elesImg.select("img").get(j).attr("src"); // 행사 이미지
						// 가져온 값을 VO에 넣어준다.
						EventboardVO eventboardVO = new EventboardVO();
						eventboardVO.setEv_sellcvs("C·SPACE");
						eventboardVO.setEv_subject(subject);
						eventboardVO.setEv_content(".");
						eventboardVO.setEv_filename(image);
						eventboardVO.setId("admin1");
						eventboardVO.setNickname("관리자1");
						eventboardVO.setEv_notice("no");
						
						list.add(eventboardVO);
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
			return list;
		}
	}

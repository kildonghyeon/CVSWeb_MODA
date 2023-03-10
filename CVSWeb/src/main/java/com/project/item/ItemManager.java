package com.project.item;

import java.util.ArrayList;
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

public class ItemManager {
	
	String targetSite = "";
	Document doc = null;
	
	public ArrayList<ItemVO> getGS25Item() {
		
		ArrayList<ItemVO> list = new ArrayList<ItemVO>();
		targetSite = "http://gs25.gsretail.com/gscvs/ko/products/event-goods-search?CSRFToken=352f92a3-decf-4ad2-8a24-3114fa712bc2";
		
		try {
			int page = 0;
			while (true) {
				page ++;
				// gs 상품페이지 접속
				Thread.sleep(500); 
				Connection.Response response = Jsoup.connect(targetSite)
						.timeout(3000)
						.data("pageNum", page+"")
						.data("pageSize", "100")
						.ignoreContentType(true)
						.method(Connection.Method.GET)
						.execute();
				
				// 페이지에 접속했으면 파싱 후 값을 doc에 전달한다.
				
				doc = response.parse();
				
				// text() 메소드를 이용해서 내용을 문자열로 옮겨준다.
				String str = doc.select("body").text();
				
//				========================== 23.02.20 코드 수정 ===================================
//				str = str.replaceAll("\\\\", ""); // 결과: "{"results\":[{"attFileId\":"MD0000000864121", ...}"
//				str = str.substring(1); // 결과: {"results\":[{"attFileId\":"MD0000000864121", ...}"
//				str = str.substring(0, str.length() - 1); // 결과: {"results\":[{"attFileId\":"MD0000000864121", ...} 완성!
//				========================== 23.02.20 코드 수정 ===================================
				
				JSONObject jsonObj = (JSONObject) new JSONParser().parse(str);
				
				// JSON Object에서 필요한 정보가 있는, 즉 results 배열을 추출한다.
				JSONArray jsonArr = (JSONArray) jsonObj.get("results");	
				if(jsonArr.size() == 0) { // jsonArr의 크기가 0이라는 것은 results 배열 안에 아무것도 없는 즉, 얻어온 정보가 없는 것
					break; // 얻어온게 없으면 while 문에서 탈출한다.
				}
				
				// 배열의 사이즈만큼 배열을 돌려준다.
				for(int i=0; i<jsonArr.size(); i++) {
					// 추출한 배열을 새로운 JSON Object에 넣어주고, 필요한 정보들을 빼온다.
					JSONObject gs25Object = (JSONObject) jsonArr.get(i);
					
					String itemName = gs25Object.get("goodsNm").toString(); // 상품명
					
					String price = gs25Object.get("price").toString().replace(".0", ""); // 가격 => ****.0 으로 값이 나와서 .0을 없애준다.
					int itemPrice = Integer.parseInt(price);
					
					String eventType = gs25Object.get("eventTypeNm").toString(); // 이벤트타입
					String itemImage = gs25Object.get("attFileNm").toString(); // 아이템 이미지
					
					// 덤증정이 아닌 행사인 경우 아래 변수에 들어갈 내용이 없기 때문에 일단 null값으로 초기화한다.
					String addItemName = null;
					String addItemImage = null;
					
					// 이벤트타입이 덤증정일 경우에만 위에 2개 변수에 값을 넣어준다.
					if(eventType.equals("덤증정")) {
						addItemName = gs25Object.get("giftGoodsNm").toString(); // 증정 상품명
						addItemImage = gs25Object.get("giftAttFileNm").toString(); // 증정 상품 이미지
					}
					
					ItemVO itemVO = new ItemVO();
					itemVO.setCategory("기타상품");
					itemVO.setItemName(itemName);
					changeCategory(itemVO); // 상품명을 받았으면 카테고리를 다시 변경해준다.
					itemVO.setItemPrice(itemPrice);
					itemVO.setSellCVS("GS25");
					itemVO.setEventType(eventType);
					itemVO.setItemImage(itemImage);
					itemVO.setAddItemName(addItemName);
					itemVO.setAddItemImage(addItemImage);
					
					list.add(itemVO);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	public ArrayList<ItemVO> getCUItem() {
		ArrayList<ItemVO> list = new ArrayList<ItemVO>();
		targetSite = "https://cu.bgfretail.com/event/plusAjax.do";
		
		try {
			int page = 0;
			while (true) {
				page++;
				Thread.sleep(500);			
				doc = Jsoup.connect(targetSite)
						.data("pageIndex", page+"")
						.data("listType", "1")
						.data("searchCondition", "")
						.post();		
				
				 Elements names = doc.select("div.name > p"); // 상품명
				 Elements images = doc.select("img.prod_img"); // 이미지
				 Elements badges = doc.select("div.badge"); // 이벤트
				 Elements prices = doc.select("div.price"); // 가격				 
				 
				 if(doc.select("li.prod_list").size() == 0) {
					 break;
				 }

	              for (int i=1; i<=names.size()-1; i++) {
	                  String itemName = names.get(i).text();
	                  String itemImage = images.get(i).attr("src").substring(2);
	                  String eventType = badges.get(i).text().trim();
	                  int itemPrice = Integer.parseInt(prices.get(i).text().replaceAll("[^0-9]","").trim());
		                  
	                  ItemVO itemVO = new ItemVO();
	                  itemVO.setCategory("기타상품"); // 카테고리 분류가 없음
	                  itemVO.setItemName(itemName);
	                  changeCategory(itemVO); // 상품명을 받았으면 카테고리를 다시 변경해준다.
	                  itemVO.setItemPrice(itemPrice);
	                  itemVO.setSellCVS("CU");
	                  itemVO.setEventType(eventType);
	                  itemVO.setItemImage(itemImage);
	                  
	                  list.add(itemVO);		                  
	              }
				 
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
   public ArrayList<ItemVO> getSevenElevenItem() {
	      ArrayList<ItemVO> list = new ArrayList<ItemVO>();
	      targetSite = "https://www.7-eleven.co.kr/product/listMoreAjax.asp";

	      try {
	          for (int i = 1; i <= 4; i++) {
	             int page = 0;
	             
	             while(true) {
	                page++;
	                Thread.sleep(500);
	                doc = Jsoup.connect(targetSite)
	                		.data("intPageSize", "13")
	                		.data("intCurrPage", page + "")
	                		.data("pTab",  i+"")
	                		.post();
	                Elements name = doc.select("div.name"); // 상품명
	                Elements price = doc.select("div.price"); // 가격
	                Elements img = doc.select(".pic_product > img"); // 상품 이미지
	                Elements event = doc.select(".tag_list_01 > li"); // 이벤트 종류
	               
	               
	               if(name.size() == 0) {
	            	   break;
	               }
	               
	               String itemName = "";
	               int itemPrice = 0;
	               String itemImage = "";
	               String eventType = "";
	               String addItemName = "";
	               String addItemImage = "";
	               
	               if (i == 3) {
	            	   for (int j=0; j<name.size(); j+=2) {
	            		   itemName = name.get(j).text();
	            		   itemPrice = Integer.parseInt(price.get(j).text().replaceAll("[^0-9]",""));
	            		   itemImage = "https://www.7-eleven.co.kr" + img.get(j).attr("src");
	            		   eventType = "덤증정";
	            		   addItemName = name.get(j+1).text();
	            		   addItemImage = "https://www.7-eleven.co.kr" + img.get(j+1).attr("src");
	            		   
	 	                  ItemVO itemVO = new ItemVO();
	 	                  itemVO.setItemName(itemName);
	 	                  itemVO.setItemPrice(itemPrice);
	 	                  itemVO.setSellCVS("세븐일레븐");
	 	                  itemVO.setEventType(eventType);
	 	                  itemVO.setItemImage(itemImage);
	 	                  changeCategory(itemVO); // 상품명을 받았으면 카테고리를 다시 변경해준다.
	 	                  
	 	                  list.add(itemVO);
	            	   }
	               } else {
						for (int j=0; j<name.size(); j++) {
							itemName = name.get(j).text();
							itemPrice = Integer.parseInt(price.get(j).text().replaceAll("[^0-9]", ""));
							itemImage = "https://www.7-eleven.co.kr" + img.get(j).attr("src");
							eventType = event.get(j).text();

							ItemVO itemVO = new ItemVO();
							itemVO.setItemName(itemName);
							itemVO.setItemPrice(itemPrice);
							itemVO.setSellCVS("세븐일레븐");
							itemVO.setEventType(eventType);
							itemVO.setItemImage(itemImage);
							changeCategory(itemVO); // 상품명을 받았으면 카테고리를 다시 변경해준다.
							
							list.add(itemVO);

						}
	               	}
	             }
	          }

		      } catch (Exception e) { 
		         e.printStackTrace();
		      }
		      
		      return list;
		   }
	public ArrayList<ItemVO> getEmart24Item() {
		// 2023/02 기준 https를 http로 바꾸지 않으면 오류 발생
		// + 사이트 구조 변경
		ArrayList<ItemVO> list = new ArrayList<ItemVO>();
		
		try {
			int page = 0;
			while (true) {
				page++;
				targetSite = "http://emart24.co.kr/goods/event?page="+page;
				Thread.sleep(500);
				doc = Jsoup.connect(targetSite)
						.get();

				Elements divs = doc.select("div.itemWrap");
				
				if(divs.size() == 0) {
					break;
				}
				
				for(Element element : divs) {
					Elements itemTxtWrap = element.select("div.itemTxtWrap");
					Elements itemTit = element.select("div.itemTit");
					Elements itemImg = element.select("div.itemImg");

					String itemName = itemTxtWrap.select("a").get(0).text();

					int itemPrice = 0;
					if (itemTxtWrap.select("a").size() == 3) {
						itemPrice = Integer
								.parseInt(itemTxtWrap.select("a").get(2).text().replaceAll("[^0-9]", "").trim());
					} else {
						try {
							itemPrice = Integer
									.parseInt(itemTxtWrap.select("a").get(1).text().replaceAll("[^0-9]", "").trim());
						} catch (Exception e) {
							itemPrice = Integer
									.parseInt(itemTxtWrap.select(".price").text().replaceAll("[^0-9]", "").trim());
						}
					}
					
					String eventType = itemTit.select("span.floatR").text();
					if(eventType.equals("1 + 1")) {
						eventType = "1+1";
					} else if(eventType.equals("2 + 1")) {
						eventType = "2+1";
					} else if(eventType.equals("세일")) {
						eventType = "할인";
					} else {
						eventType = "기타행사";
					}
					
					String itemImage = itemImg.select("img").attr("src");
					
 				   	ItemVO itemVO = new ItemVO();
	 				itemVO.setItemName(itemName);
	 				itemVO.setItemPrice(itemPrice);
	 				itemVO.setSellCVS("이마트24");
	 				itemVO.setEventType(eventType);
	 				itemVO.setItemImage(itemImage);
	 				changeCategory(itemVO);
	 				
	 				list.add(itemVO);
	 				
				}
				
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return list;

	}

	   public ArrayList<ItemVO> getMinistopItem() {
		      ArrayList<ItemVO> list = new ArrayList<ItemVO>();
		      Map<String, String> data = new HashMap<String, String>();

		      targetSite = "https://www.ministop.co.kr/MiniStopHomePage/page/querySimple.do";
		      for (int i = 1; i <= 4; i++) {
		    	 int page = 0;
		         while(true) {
		        	page++;
		            String s = "/plus" + i;
		            if (i == 3) {
		               s = "/add";
//		               continue;
		            } else if (i == 4) {
		               s = "/sale";
		            }

		            data.put("pageId", "event" + s); // plus뒤에 1,2,add, sale 붙어야함.(1+1/1+2/덤)
		            data.put("sqlnum", "1"); // 1 고정
		            data.put("paramInfo", i + ""); // 1:: 2:: 3:: 4::
		            data.put("pageNum", +page + ""); // 더보기페이지번호 8 16 1
		            data.put("sortGu", ""); // 없음
		            data.put("tm", "1673329171875");

		            try {
		               Thread.sleep(500);
		               doc = Jsoup.connect(targetSite).data(data)
		            		   .ignoreContentType(true)
		            		   .post();
		               String str = doc.select("body").text();

		               JSONObject jsonObject = (JSONObject) new JSONParser().parse(str);
		               JSONArray jsonArray = (JSONArray) jsonObject.get("recordList");
		               
		               if(jsonArray.size() == 0) {
		            	   break;
		               }

		               for (int k = 0; k < jsonArray.size(); k++) {
		                  JSONObject msObject = (JSONObject) jsonArray.get(k);
		                  String item = msObject.get("fields").toString();

		                  item = item.replaceAll("\"", "");
		                  item = item.substring(1);
		                  if (item.startsWith("6")) {
		                     if (i == 3) {
		                        item = item.substring(4);
		                     } else {
		                        item = item.substring(5);
		                     }
		                  }
		                  item = item.substring(0, item.length() - 1);
		                  

		                  String itemArr[] = item.split(",");
		                  String itemName = itemArr[0];
		                  int itemPrice = Integer.parseInt(itemArr[1]);
		                  String eventType = "";
		                  String itemImage = "";
		                  String addItemName = ""; // 증정품명
		                  String addItemImage = ""; // 덤증정 이미지
		                  if (i == 1) {
		                     eventType = "1+1";
		                     itemImage = "https://www.ministop.co.kr/MiniStopHomePage/page/pic.do?n=event1plus1."
		                           + itemArr[4];
		                  } else if (i == 2) {
		                     eventType = "2+1";
		                     itemImage = "https://www.ministop.co.kr/MiniStopHomePage/page/pic.do?n=event2plus1."
		                           + itemArr[4];
		                  } else if (i == 3) {
		                     eventType = "덤증정";
		                     itemImage = "https://www.ministop.co.kr/MiniStopHomePage/page/pic.do?n=eventmore."
		                           + itemArr[4];
		                     addItemName = itemArr[2].replace(" 증정", "");
		                     addItemImage = "https://www.ministop.co.kr/MiniStopHomePage/page/pic.do?n=eventmore."
		                           + itemArr[5];
		                  } else {
		                     eventType = "할인";
		                     itemImage = "https://www.ministop.co.kr/MiniStopHomePage/page/pic.do?n=eventsale."
		                           + itemArr[4];
		                     itemPrice = Integer.parseInt(itemArr[2]);
		                  }

		                  ItemVO itemVO = new ItemVO();
		                  itemVO.setCategory("기타상품");
		                  itemVO.setItemName(itemName);
		                  changeCategory(itemVO); // 상품명을 받았으면 카테고리를 다시 변경해준다.
		                  itemVO.setItemPrice(itemPrice);
		                  itemVO.setSellCVS("ministop");
		                  itemVO.setEventType(eventType);
		                  itemVO.setItemImage(itemImage);
		                  itemVO.setAddItemName(addItemName);
		                  itemVO.setAddItemImage(addItemImage);
		                  
		                  list.add(itemVO);
		               }

		            } catch (Exception e) {

		               e.printStackTrace();

		            }
		         }
		      }
		      return list;
		   }
	
	public ArrayList<ItemVO> getCspaceItem() {
		ArrayList<ItemVO> list = new ArrayList<ItemVO>();
		try {
			int page = 0;
			while(true) {
				page++;
				targetSite = 
						String.format("https://www.cspace.co.kr/service/product.html?cpage=%d", page);
				Thread.sleep(500); 
				doc = Jsoup.connect(targetSite).get();
				
				Elements elements = doc.select("ul.box");
				
				if(elements.select("dt").size() == 0) {
					break;
				}
				
				for (int j=0; j<elements.size(); j++) {
					Elements dts = elements.select("dt"); // 상품명
					Elements dds = elements.select("dd"); // 가격
					Elements lis = elements.select("li"); // 이벤트타입
					Elements imgs = elements.select("img"); // 상품 이미지
					
					for (int k=0; k<dts.size(); k++) {
						String itemName = dts.get(k).text();
						
						String won = dds.get(k).text().replaceAll("[^0-9]", "");
						int itemPrice = Integer.parseInt(won);
						
						String event = lis.get(k).attr("class");
						String eventType = "";
						if (event.equals("twoPlus")) {
							eventType = "2+1";
						} else if (event.equals("onePlus")) {
							eventType = "1+1";
						} else if (event.equals("sale")){ 
							eventType = "할인";
						} else {
							eventType = "덤증정";							
						}
						
						String itemImage = "https://www.cspace.co.kr/"+imgs.get(k).attr("src");
						
						// 가져온 정보들을 itemVO에 넣어준다.
						ItemVO itemVO = new ItemVO();
						itemVO.setCategory("기타상품");
						itemVO.setItemName(itemName);
						itemVO.setItemPrice(itemPrice);
						itemVO.setSellCVS("C·SPACE");
						itemVO.setEventType(eventType);
						itemVO.setItemImage(itemImage);
						
						System.out.println(itemVO);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
			return list;
		}
	
	public void changeCategory(ItemVO itemVO) {
		
		if((itemVO.getItemName().contains("샌드") && !itemVO.getItemName().contains("햄"))|| itemVO.getItemName().contains("카스타드") || itemVO.getItemName().contains("고메포테토") || itemVO.getItemName().contains("우유찰떡") || itemVO.getItemName().contains("크런치바")
				|| (itemVO.getItemName().contains("얼라이브") && itemVO.getItemName().contains("젤리"))  || itemVO.getItemName().contains("크라운") || itemVO.getItemName().contains("1500") || itemVO.getItemName().contains("(1000)")
				|| itemVO.getItemName().contains("(1200)") || itemVO.getItemName().contains("(2500)") || (itemVO.getItemName().contains("오리온") && !itemVO.getItemName().contains("용암수") && !itemVO.getItemName().contains("단백질"))
				|| itemVO.getItemName().contains("스낵") || itemVO.getItemName().contains("로아카") || itemVO.getItemName().contains("로아커") || itemVO.getItemName().contains("에너지바") || itemVO.getItemName().contains("프로틴바")
				|| itemVO.getItemName().contains("젤리") || itemVO.getItemName().contains("호빵") || itemVO.getItemName().contains("타르트") || itemVO.getItemName().contains("프리첼") || itemVO.getItemName().contains("모찌")
				|| (itemVO.getItemName().contains("고기") && (itemVO.getItemName().contains("빵") && itemVO.getItemName().contains("부리또") && itemVO.getItemName().contains("뿌셔뿌셔"))) || (itemVO.getItemName().contains("구미") && !itemVO.getItemName().contains("주스"))
				|| itemVO.getItemName().contains("캔디") || itemVO.getItemName().contains("롤리팝") || itemVO.getItemName().contains("소보로") || itemVO.getItemName().contains("피자")
				) {
			itemVO.setCategory("과자/빵"); 
		} else if (itemVO.getItemName().contains("후랑크") || itemVO.getItemName().contains("맵사이신") || itemVO.getItemName().contains("화끈구이") || itemVO.getItemName().contains("비바크") || itemVO.getItemName().contains("인포켓치즈") 
				|| itemVO.getItemName().contains("볶음밥")|| (itemVO.getItemName().contains("고메") && !itemVO.getItemName().contains("고메포테토")) || itemVO.getItemName().contains("비비고") || itemVO.getItemName().contains("떡볶이")
				|| itemVO.getItemName().contains("닭가슴살") || itemVO.getItemName().contains("우유껌") || itemVO.getItemName().contains("스트링") || itemVO.getItemName().contains("요거밀") || itemVO.getItemName().contains("진주햄")
				|| (itemVO.getItemName().contains("비요뜨")) && !itemVO.getItemName().contains("홈") && !itemVO.getItemName().contains("아이스크림") && !itemVO.getItemName().contains("파인트") || itemVO.getItemName().contains("꼬치")
				|| (itemVO.getItemName().contains("얼라이브") && itemVO.getItemName().contains("원스데일리")) || itemVO.getItemName().contains("꼬꼬봉") || itemVO.getItemName().contains("요플레토핑") || itemVO.getItemName().contains("컵밥")
				|| itemVO.getItemName().contains("햄") || itemVO.getItemName().contains("질러") || (itemVO.getItemName().contains("파스타") && !itemVO.getItemName().contains("스낵")) || itemVO.getItemName().contains("크랩") || itemVO.getItemName().contains("핫바") 
				|| itemVO.getItemName().contains("얇은피") || (itemVO.getItemName().contains("바프") || !itemVO.getItemName().contains("와플콘")) || itemVO.getItemName().contains("국수") || itemVO.getItemName().contains("곤약")
				|| itemVO.getItemName().contains("누들") || (itemVO.getItemName().contains("고기") && !itemVO.getItemName().contains("빵") && !itemVO.getItemName().contains("부리또") && !itemVO.getItemName().contains("뿌셔뿌셔"))  
				|| itemVO.getItemName().contains("컵반") || itemVO.getItemName().contains("군밤") || itemVO.getItemName().contains("자장") || itemVO.getItemName().contains("요거톡") || itemVO.getItemName().contains("소시지")
				|| (itemVO.getItemName().contains("죽") && !itemVO.getItemName().contains("죽염")) || itemVO.getItemName().contains("육포") || itemVO.getItemName().contains("오징어") || itemVO.getItemName().contains("부각") || itemVO.getItemName().contains("튀각")
				|| itemVO.getItemName().contains("맛밤") || itemVO.getItemName().contains("라면") || itemVO.getItemName().contains("사발") || itemVO.getItemName().contains("큰컵") || itemVO.getItemName().contains("폰타나") || itemVO.getItemName().contains("샘표")
				|| itemVO.getItemName().contains("덮밥") || itemVO.getItemName().contains("국밥") || itemVO.getItemName().contains("스팸") || itemVO.getItemName().contains("햇반") || itemVO.getItemName().contains("밥")
				|| itemVO.getItemName().contains("소컵") || itemVO.getItemName().contains("보노") || itemVO.getItemName().contains("핫도그") || itemVO.getItemName().contains("구이") || itemVO.getItemName().contains("안주야")
				|| itemVO.getItemName().contains("봉지") || itemVO.getItemName().contains("만두") || itemVO.getItemName().contains("두부") || itemVO.getItemName().contains("어묵") || itemVO.getItemName().contains("찌개") || itemVO.getItemName().contains("참치")
				) {
			itemVO.setCategory("식품"); 	
		} else if ((itemVO.getItemName().contains("우유") && !itemVO.getItemName().contains("우유찰떡") && !itemVO.getItemName().contains("우유껌")) || (itemVO.getItemName().contains("요구르트") && !itemVO.getItemName().contains("젤리"))
				|| itemVO.getItemName().contains("에이드") || itemVO.getItemName().contains("아메리카노") || itemVO.getItemName().contains("카라멜마") || (itemVO.getItemName().contains("더단백")) && !itemVO.getItemName().contains("크런치바")
				|| itemVO.getItemName().contains("테이크핏") || itemVO.getItemName().contains("프렌치카페") || itemVO.getItemName().contains("불가리스") || itemVO.getItemName().contains("남양") || itemVO.getItemName().contains("올데이")
				|| (itemVO.getItemName().contains("비요뜨") && (itemVO.getItemName().contains("홈") || itemVO.getItemName().contains("아이스크림") || itemVO.getItemName().contains("파인트"))) || itemVO.getItemName().contains("주스")
				|| itemVO.getItemName().contains("에스프레소") || itemVO.getItemName().contains("TOP") || itemVO.getItemName().contains("모카") || itemVO.getItemName().contains("썬업") || itemVO.getItemName().contains("바리스타")
				|| itemVO.getItemName().contains("두유") || itemVO.getItemName().contains("어메이징") || itemVO.getItemName().contains("엔요") || itemVO.getItemName().contains("드링킹") || itemVO.getItemName().contains("산양")
				|| (itemVO.getItemName().contains("얼라이브") && !itemVO.getItemName().contains("원스데일리") && !itemVO.getItemName().contains("젤리")) || itemVO.getItemName().contains("칠성") || itemVO.getItemName().contains("듀오안")
				|| itemVO.getItemName().contains("딸기타임") || itemVO.getItemName().contains("초코타임") || itemVO.getItemName().contains("닥터캡슐") || itemVO.getItemName().contains("액티비아") || itemVO.getItemName().contains("나뚜루")
				|| itemVO.getItemName().contains("하겐") || (itemVO.getItemName().contains("오리온") && (itemVO.getItemName().contains("용암수") || itemVO.getItemName().contains("단백질")) || itemVO.getItemName().contains("파스쿠찌"))
				|| itemVO.getItemName().contains("삼우") || itemVO.getItemName().contains("빵또아") || itemVO.getItemName().contains("시모나") || itemVO.getItemName().contains("광동") || itemVO.getItemName().contains("탄산수")
				|| itemVO.getItemName().contains("코카") || itemVO.getItemName().contains("펩시") || itemVO.getItemName().contains("홍차") || itemVO.getItemName().contains("담터") || itemVO.getItemName().contains("백산수")
				|| itemVO.getItemName().contains("콤부차") || itemVO.getItemName().contains("캔") || itemVO.getItemName().contains("페트") || itemVO.getItemName().contains("PET") || itemVO.getItemName().contains("아이스티")
				|| itemVO.getItemName().contains("끌레") || (itemVO.getItemName().contains("750ML") && !itemVO.getItemName().contains("볼륨")) || itemVO.getItemName().contains("375ML") || itemVO.getItemName().contains("하이트")
				|| itemVO.getItemName().contains("스파클링") || itemVO.getItemName().contains("티오피") || itemVO.getItemName().contains("맥스웰")
				
				) {
			itemVO.setCategory("음료/아이스크림"); 							 
		} else if (itemVO.getItemName().contains("피죤") || itemVO.getItemName().contains("애경") || itemVO.getItemName().contains("순수한면") || itemVO.getItemName().contains("샤프란") || itemVO.getItemName().contains("엘라스틴")
				 || itemVO.getItemName().contains("워시") || itemVO.getItemName().contains("솔루션") || itemVO.getItemName().contains("LG") || itemVO.getItemName().contains("마스크") || itemVO.getItemName().contains("흡수") || itemVO.getItemName().contains("좋은느낌")
				 || itemVO.getItemName().contains("깨나라") || itemVO.getItemName().contains("유한") || itemVO.getItemName().contains("스프레이") || itemVO.getItemName().contains("핸드") || itemVO.getItemName().contains("워시")
				 || itemVO.getItemName().contains("치약") || itemVO.getItemName().contains("칫솔") || itemVO.getItemName().contains("치솔") || itemVO.getItemName().contains("2080") || itemVO.getItemName().contains("클렌징") || itemVO.getItemName().contains("가그린")
				 || itemVO.getItemName().contains("죽염") || itemVO.getItemName().contains("에너자이저") || itemVO.getItemName().contains("니베아") || itemVO.getItemName().contains("바디피트") || itemVO.getItemName().contains("존슨즈")
				 || itemVO.getItemName().contains("리스테린") || itemVO.getItemName().contains("면도") || itemVO.getItemName().contains("뉴트로지나") || itemVO.getItemName().contains("밴드") || itemVO.getItemName().contains("로션") || itemVO.getItemName().contains("컨디셔너")
				 || itemVO.getItemName().contains("린스") || itemVO.getItemName().contains("샴푸") || itemVO.getItemName().contains("립밤") || itemVO.getItemName().contains("왁스") || itemVO.getItemName().contains("양말")
				 || itemVO.getItemName().contains("베이킹") || itemVO.getItemName().contains("섬유") || itemVO.getItemName().contains("쏘피") || itemVO.getItemName().contains("깨끗") || itemVO.getItemName().contains("티슈")
				 || itemVO.getItemName().contains("페브리즈") || itemVO.getItemName().contains("크리넥스") || itemVO.getItemName().contains("화장") || itemVO.getItemName().contains("샤워") || itemVO.getItemName().contains("다우니")
				 || itemVO.getItemName().contains("세제") || itemVO.getItemName().contains("바세린") || itemVO.getItemName().contains("세정") || itemVO.getItemName().contains("테크") || itemVO.getItemName().contains("액츠")
				 || itemVO.getItemName().contains("대형") || itemVO.getItemName().contains("소형") || itemVO.getItemName().contains("NAVIYA")
				) {
			itemVO.setCategory("잡화"); 							 						
		}
	}
	
}

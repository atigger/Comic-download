import com.alibaba.fastjson.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Main {
    public static Proxy proxy;//设置代理
    static String DIR = "D:/漫画/";//文件保存路径，没有会自动创建
    public static int START_CHAPTER = 0; //起始章节
    public static int START_PAGE = 1; //起始页码
    public static int START_CHAPTER_CODE = 0; //起始页码

    //static int CHAPTER = 91436;//章节代码

    public static void main(String[] args) throws InterruptedException {
        //创建Scanner对象，接受从控制台输入
        int inter = 10826;
        String ip_add = "127.0.0.1";
        Scanner input = new Scanner(System.in);
        System.out.print("请输入代理地址(默认" + ip_add + ")：");
        String str = input.nextLine();
        if (str.equals("")) {
        } else {
            ip_add = str;
        }
        System.out.print("请输入端口(默认" + inter + ")：");
        String str1 = input.nextLine();
        if (str1.equals("")) {
        } else {
            inter = Integer.parseInt(str1);
        }
        System.out.print("请输入从倒数第几章开始(默认从最新的章节开始)：");
        String str2 = input.nextLine();
        if (str2.equals("")) {
        } else {
            START_CHAPTER = Integer.parseInt(str2) - 1;
        }
        System.out.print("请输入从第几页开始(默认从第一页开始)：");
        String str3 = input.nextLine();
        if (str3.equals("")) {
        } else {
            START_PAGE = Integer.parseInt(str3);
        }
        System.out.print("请输入保存路径(默认" + DIR + ")：");
        String str4 = input.nextLine();
        if (str4.equals("")) {
        } else {
            DIR = str4;
        }
        //输出结果
        System.out.println("操作成功！正在准备开始");
        int s11 = START_CHAPTER + 1;
        System.out.println("代理:" + ip_add + "：" + inter + " 开始章节：" + s11 + " 开始页数：" + START_PAGE + " 保存路径：" + DIR);
        proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip_add, inter));

        Document doc;
        try {
            doc = Jsoup.connect("http://www.mangabz.com/1864bz/").proxy(proxy).get();
            Elements html = doc.select("div.detail-list-form-con > a");
            for (int i = START_CHAPTER; i < html.size(); i++) {
                String html1 = String.valueOf(html.get(i));
                String chapter_string = "href=\"/m";
                int chapter_number = html1.indexOf(chapter_string, 0);
                String chapter = html1.substring(chapter_number + chapter_string.length(), html1.indexOf("/", chapter_number + chapter_string.length()));
                String title_string = "target=\"_blank\">";
                int title_number = html1.indexOf(title_string, 0);
                String title = html1.substring(title_number + title_string.length(), html1.indexOf(" <", title_number + title_string.length()));
                System.out.println("正在获取->章节代码：" + chapter + " 标题：" + title);
                if (i == START_CHAPTER) {
                    START_CHAPTER_CODE = Integer.parseInt(chapter);
                }
                CHAPTER_DOWNLOAD(chapter, title);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //章节内操作
    public static void CHAPTER_DOWNLOAD(String chapter, String title) throws InterruptedException {
        int tag = 0;
        int chapter_code = Integer.parseInt(chapter);
        if (START_CHAPTER_CODE != chapter_code) {
            START_PAGE = 1;
        }
        for (int i = START_PAGE; i < 1000; i++) {
            int PAGE = i;
            String URL;
            URL = "http://www.mangabz.com/m" + chapter + "-p" + PAGE; //漫画的第几页
            String IMG_URL = "";
            JSONObject data;
            System.out.println("正在获取->" + title + " 第" + PAGE + "页");
            data = GET_AJAX(URL);//获取所需数据
            if (data != null) {
                String CID = String.valueOf(data.getString("CID"));
                String MID = String.valueOf(data.getString("MID"));
                String DT = String.valueOf(data.getString("DT"));
                String SIGN = String.valueOf(data.getString("SIGN"));
                String GET_JPG_URL = "http://www.mangabz.com/m" + chapter + "/chapterimage.ashx?cid=" + CID + "&page=" + PAGE + "&key=&_cid=" + CID + "&_mid=" + MID + "&_dt=" + DT + "&_sign=" + SIGN; //发送数据的URL
                IMG_URL = GET_URL(GET_JPG_URL, MID, CID, PAGE);//获取图片URL
                if (tag < 10) {
                    if (IMG_URL.equals("0")) {
                        System.out.println("第" + i + "页获取失败，10秒后将重试");
                        Thread.sleep(10000);
                        i = i - 1;
                        tag++;
                    } else {
                        tag = 0;
                        Download(IMG_URL, title);//下载图片
                    }
                } else {
                    System.out.println("超时次数过多，已自动结束");
                    break;
                }
            } else {
                System.out.println("本章结束");
                break;
            }
        }
    }

    //获取后续需要传的参数
    public static JSONObject GET_AJAX(String Url) {
        JSONObject data = new JSONObject();
        Document doc;
        int CID = 0;
        int MID = 0;
        String DT = "";
        String SIGN = "";
        try {
            Connection.Response response = Jsoup.connect(Url)
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")//模拟浏览器
                    .timeout(10000)
                    .proxy(proxy)
                    .ignoreHttpErrors(true)
                    .execute();
            int statusCode = response.statusCode();
            if (statusCode == 200) {
                doc = response.parse();
                String html = String.valueOf(doc);
                //获取CID
                String cid_string = "MANGABZ_CID=";
                int cid_number = html.indexOf(cid_string, 0);
                CID = Integer.parseInt(html.substring(cid_number + cid_string.length(), html.indexOf(";", cid_number + 1)));
                //获取MID
                String mid_string = "MANGABZ_MID=";
                int mid_number = html.indexOf(mid_string, 0);
                MID = Integer.parseInt(html.substring(mid_number + mid_string.length(), html.indexOf(";", mid_number + 1)));
                //获取DT
                String dt_string = "MANGABZ_VIEWSIGN_DT=\"";
                int dt_number = html.indexOf(dt_string, 0);
                DT = html.substring(dt_number + dt_string.length(), html.indexOf("\";", dt_number + dt_string.length()));
                //获取SIGN
                String sign_string = "MANGABZ_VIEWSIGN=\"";
                int sign_number = html.indexOf(sign_string, 0);
                SIGN = html.substring(sign_number + sign_string.length(), html.indexOf("\"", sign_number + sign_string.length()));
                System.out.println("CID=" + CID + " MID=" + MID + " DT=" + DT + " SIGN=" + SIGN);
                data.put("CID", CID);
                data.put("MID", MID);
                data.put("DT", DT);
                data.put("SIGN", SIGN);
                //System.out.println(data);
                return data;
            } else {
                System.out.println("recevied error code : " + statusCode);
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String GET_URL(String Url, String MID, String CID, int PAGE) {
        Document doc;
        String END_URL = null;
        try {
            Connection.Response response = Jsoup.connect(Url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4381.8 Safari/537.36")//模拟浏览器
                    .referrer("http://www.mangabz.com/m" + CID)
                    .timeout(10000)
                    .proxy(proxy)
                    .ignoreHttpErrors(false)
                    .execute();
            int statusCode = response.statusCode();
            if (statusCode == 200) {
                doc = response.parse();
                String html = String.valueOf(doc);
                //System.out.println(html);
                String page_string = PAGE + "_";
                int page_number = html.indexOf(page_string, 0);
                if (page_number != -1) {
                    String PAGE_URL = html.substring(page_number, html.indexOf("|", page_number + 1));//正则
                    END_URL = "http://image.mangabz.com/2/" + MID + "/" + CID + "/" + PAGE_URL + ".jpg";
                    System.out.println("图片地址：" + END_URL);
                    return END_URL;
                } else {
                    return "0";
                }

            } else {
                System.out.println("recevied error code : " + statusCode);
                return null;
            }


        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //下载图片
    public static void Download(String Url, String title) {
        try {
            File sf = new File(DIR + title);
            if (!sf.exists()) {
                sf.mkdirs();
            }
            String src = Url;// 获取img中的src路径
            // 获取后缀名
            String imageName = src.substring(src.lastIndexOf("/") + 1, src.length());
            // 连接url
            URL url;
            try {
                url = new URL(src);
                URLConnection uri = url.openConnection(proxy);
                uri.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4381.8 Safari/537.36");
                // 获取数据流
                System.out.println("正在下载：" + imageName);
                InputStream is = uri.getInputStream();
                // 写入数据流
                OutputStream os = new FileOutputStream(new File(DIR + title, imageName)); //保存路径和文件名
                byte[] buf = new byte[3072];
                int i1 = 0;
                while ((i1 = is.read()) != -1) {
                    os.write(i1);
                }
                System.out.println("下载完成");
                os.close();
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }
}

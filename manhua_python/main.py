# -*- coding: utf-8 -*-
import requests
import os
from bs4 import BeautifulSoup


def GET_AJAX(URL):
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4381.8 Safari/537.36'}
    doc = requests.get(URL, headers=headers, proxies=proxies)
    if doc.status_code == 200:
        #print(doc.text)
        html = doc.text
        # 获取CID
        cid_string = "MANGABZ_CID="
        cid_num = len(cid_string)
        cid_int = int(html.find(cid_string))
        cid_end = int(html.find(";", cid_int + 1))
        CID = html[cid_int + cid_num:cid_end]
        # 获取MID
        mid_string = "MANGABZ_MID="
        mid_num = len(mid_string)
        mid_int = int(html.find(mid_string))
        mid_end = int(html.find(";", mid_int + 1))
        MID = html[mid_int + mid_num:mid_end]
        # 获取DT
        dt_string = "MANGABZ_VIEWSIGN_DT=\""
        dt_num = len(dt_string)
        dt_int = int(html.find(dt_string))
        dt_end = int(html.find("\"", dt_int + dt_num))
        DT = html[dt_int + dt_num:dt_end]
        # 获取SIGN
        sign_string = "MANGABZ_VIEWSIGN=\""
        sign_num = len(sign_string)
        sign_int = int(html.find(sign_string))
        sign_end = int(html.find("\"", sign_int + dt_num))
        SIGN = html[sign_int + sign_num:sign_end]
        print("CID:" + str(CID) + " MID: " + str(MID) + " DT:" + DT + " SIGN: " + SIGN)
        return {"CID": str(CID), "MID": str(MID), "DT": DT, "SIGN": SIGN}
    elif doc.status_code == 404:
        return 404
    else:
        return 0


def CHAPTER_DOWNLOAD(chapter_code, title,START_PAGE):
    if START_CHAPTER_CODE != chapter_code:
        START_PAGE = 1
    for i in range(int(START_PAGE), 1000):
        PAGE = i
        URL = "http://www.mangabz.com/m" + chapter_code + "-p" + str(PAGE)
        data = GET_AJAX(URL)
        if data == 404:
            print("本章已结束")
            break
        elif data == 0:
            print("出现错误，将跳过")
            break
        else:
            print("正在获取->" + title + " 第" + str(PAGE) + "页")
            GET_JPG_URL = "http://www.mangabz.com/m" + chapter_code + "/chapterimage.ashx"
            GET_URL(chapter_code, GET_JPG_URL, data, str(PAGE), title)


def GET_URL(CHAPTER, URL, DATA, PAGE, title):
    CID = DATA.get("CID")
    MID = DATA.get("MID")
    DT = DATA.get("DT")
    SIGN = DATA.get("SIGN")
    params = {'cid': CID, 'page': PAGE, 'key': "", '_cid': CID, '_mid': MID, '_dt': DT, '_sign': SIGN}  # 发送数据
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4381.8 Safari/537.36',
        'Referer': 'http://www.mangabz.com/m' + CHAPTER}
    doc = requests.get(URL, params=params, headers=headers, proxies=proxies)
    html = doc.text
    page_string = PAGE + "_";
    page_number = html.find(page_string)
    PAGE_URL = html[page_number: html.find("|", page_number + 1)];
    END_URL = "http://image.mangabz.com/2/" + MID + "/" + CID + "/" + PAGE_URL + ".jpg";
    print("图片地址：" + END_URL);
    Download(END_URL, title)


def Download(URL, title):
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4381.8 Safari/537.36'}
    r = requests.get(URL, headers=headers, stream=True, proxies=proxies)
    img_name_start = URL.rfind("/")
    img_name_end = len(URL)
    img_name = URL[img_name_start + 1:img_name_end]
    if r.status_code == 200:
        print("正在下载:" + img_name)
        isExists = os.path.exists(DIR + title)
        if not isExists:
            os.makedirs(DIR + title)
            print("已创建" + DIR + title)
        open(DIR + title + '/' + img_name, 'wb').write(r.content)  # 将内容写入图片
        print("下载完成")
    del r


DEF_DIR = "D:/漫画/"
def_ip_add = "127.0.0.1"
def_ip_proxy = "10826"
START_CHAPTER_CODE = "0"
# DIR = DEF_DIR
# CHAPTER = "91436"
# proxies = {
#     'http': 'http://127.0.0.1:10826',
# }
START_PAGE = 1
if (0 == 0):
    ip_add = input("请输入代理IP(默认127.0.0.1)：")
    if ip_add == "":
        ip_add = def_ip_add
    ip_proxy = input("请输入代理端口(默认10826)：")
    if ip_proxy == "":
        ip_proxy = def_ip_proxy
    proxies = {
        'http': 'http://' + ip_add + ':' + ip_proxy,
    }
    START_CHAPTER = input("请输入从倒数第几章开始(默认从最新的章节开始)：")
    if START_CHAPTER != "":
        START_CHAPTER = int(START_CHAPTER) - 1
    else:
        START_CHAPTER = 0
    START_PAGE = input("请输入从第几页开始(默认从第一页开始)：")
    if START_PAGE == "":
        START_PAGE = 1
    DIR = input("请输入保存路径(默认" + DEF_DIR + ")：")
    if DIR == "":
        DIR = DEF_DIR
    print("操作成功！正在准备开始");
    s11 = START_CHAPTER + 1;
    print("代理:" + ip_add + "：" + ip_proxy + " 开始章节：" + str(s11) + " 开始页数：" + str(START_PAGE) + " 保存路径：" + DIR)
    # 开始获取章节列表
    doc = requests.get("http://www.mangabz.com/1864bz/", proxies=proxies)
    html = doc.text
    bf = BeautifulSoup(html, features="html.parser")
    texts = bf.find_all('a', class_='detail-list-form-item')
    texts_num = len(texts)
    for i in range(START_CHAPTER, texts_num):
        html = texts[i]
        html = str(html)
        # 获取章节代码
        # print(html)
        chapter_string = "href=\"/m"
        chapter_num = len(chapter_string)
        chapter_int = int(html.find(chapter_string))
        chapter_end = int(html.find("/", chapter_int + chapter_num))
        chapter_code = html[chapter_int + chapter_num:chapter_end]
        if i == START_CHAPTER:
            START_CHAPTER_CODE = chapter_code;
        # 获取标题
        title_string = "\">"
        title_num = len(title_string)
        title_int = int(html.find(title_string))
        title_end = int(html.find("<", title_int + title_num))
        title = html[title_int + title_num:title_end]
        title = title.rstrip()
        print("正在获取->章节代码：" + chapter_code + " 标题：" + title);
        CHAPTER_DOWNLOAD(chapter_code, title,START_PAGE)

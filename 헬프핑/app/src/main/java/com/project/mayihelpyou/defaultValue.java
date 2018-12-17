package com.project.mayihelpyou;

public class defaultValue {
    // <----- APP VALUE ----->
    public static final String PACKAGE_NAME = "com.project.mayihelpyou";
    public static final String PREFERENCES_NAME = "SETTING";

    // <----- MAP DEFAULT VALUE ----->
    public static final double DEFAULT_LATITUDE = 37.566295f;
    public static final double DEFAULT_LONGITUDE = 126.977945f;
    public static final String DEFAULT_TYPE = "화장실";
    public static final String DEFAULT_GU = "전체";
    public static final String DEFAULT_DONG = "전체";

    // <----- SETTING VALUE ----->
    public static final String RESTROOM_DATA_UPDATE = "restroom_update";
    public static final String RESTROOM_DATA_CHECK = "restroom_check";
    public static final String RESTROOM_DISABLE_DATA_CHECK = "restroom_disable_check";
    public static final String RESTROOM_DB_ROW_COUNT = "restroom_row";
    public static final String SMOKING_AREA_DATA_UPDATE = "smoking_area_update";
    public static final String SMOKING_AREA_CHECK = "smoking_area_check";
    public static final String SMOKING_AREA_DB_ROW_COUNT = "smoking_area_row";
    public static final String IS_FIRST_UPDATE = "is_first";
    public static final String DONG_DB_ROW_COUNT = "dong_row";

    // <----- FILE NAME ----->
    public static final String EXCEPTION_LOG_DB = "ExceptionLog.db";
    public static final String RESTROOM_DATA_FILE = "restroom.json";
    public static final String RESTROOM_DB_FILE = "restroom.db";
    public static final String SMOKING_AREA_DATA_FILE = "smoke.json";
    public static final String SMOKING_AREA_DB_FILE = "smoke.db";
    public static final String DONG_DB_FILE = "dong.db";

    // <----- DB FIELD NAME ----->
    public static final String EXCEPTION_LOG_DB_FIELD_NAME = "exception";
    public static final String DATA_DB_FIELD_NAME = "place";

    // <----- INDEX ----->
    public static final int RESTROOM_START_INDEX = 100000;
    public static final int RESTROOM_DISABLE_START_INDEX = 200000;
    public static final int SMOKING_AREA_START_INDEX = 0;

    // <----- PUTEXTRA VALUE NAME ----->
    public static final String MY_LATITUDE = "my_latitude";             //myLatitude
    public static final String MY_LONGITUDE = "my_longitude";           //myLongitude
    public static final String IS_SELECTED_ONE = "is_selected";         //selectOne
    public static final String IS_DIRECT = "is_direct";                 //directMe
    public static final String IS_DIRECT_DISABLE = "is_direct_disable"; //isDisable

    public static final int DEFAULT_NEAR_METER = 10;


    /*
            IntroActivity 에 필요한 함수
     */
    // <----- DATA ENUM ----->
    public enum PlaceType {RESTROOM, SMOKING_AREA}
    public enum HtmlType {DOWNLOAD, WEB}

    // <----- PERMISSION ----->
    public static final String[] NEED_PERMISSION =
            {"android.permission.READ_EXTERNAL_STORAGE",
                    "android.permission.WRITE_EXTERNAL_STORAGE",
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.ACCESS_COARSE_LOCATION",
                    "android.permission.ACCESS_NETWORK_STATE"};

    // <----- HTML LINK DATA ----->
    private static final String HTML_BASE_LINK = "http://data.seoul.go.kr/dataList/datasetView.do";
    private static final String HTML_DOWNLOAD_LINK = "http://115.84.165.224/bigfile/iot/sheet/json/download.do";
    private static final String HTML_RESTROOM = "?infId=OA-13587&srvType=S&serviceKind=1&currentPageNo=1";
    private static final String HTML_SMOKING_AREA = "?infId=OA-12970&srvType=S&serviceKind=1&currentPageNo=1";

    // GetHtmlLink -> 주소 반환 함수
    public static String GetHtmlLink(PlaceType placeType, HtmlType htmlType){
        return ((htmlType==HtmlType.DOWNLOAD) ?
                HTML_DOWNLOAD_LINK:
                HTML_BASE_LINK)
                + ((placeType == PlaceType.RESTROOM) ?
                HTML_RESTROOM :
                HTML_SMOKING_AREA);
    }


    /*
순번	시군구코드	시군구명_한글	시군구명_영문	ESRI_PK	위도	경도
10	11680	강남구	Gangnam-gu	9	37.4959854	127.0664091
13	11740	강동구	Gangdong-gu	12	37.5492077	127.1464824
8	11305	강북구	Gangbuk-gu	7	37.6469954	127.0147158
11	11500	강서구	Gangseo-gu	10	37.5657617	126.8226561
23	11620	관악구	Gwanak-gu	15	37.4653993	126.9438071
14	11215	광진구	Gwangjin-gu	13	37.5481445	127.0857528
6	11530	구로구	Guro-gu	5	37.4954856	126.858121
5	11545	금천구	Geumcheon-gu	4	37.4600969	126.9001546
18	11350	노원구	Nowon-gu	18	37.655264	127.0771201
1	11320	도봉구	Dobong-gu	0	37.6658609	127.0317674
3	11230	동대문구	Dongdaemun-gu	2	37.5838012	127.0507003
4	11590	동작구	Dongjak-gu	3	37.4965037	126.9443073
15	11440	마포구	Mapo-gu	14	37.5622906	126.9087803
20	11410	서대문구	Seodaemun-gu	21	37.5820369	126.9356665
16	11650	서초구	Seocho-gu	16	37.4769528	127.0378103
24	11200	성동구	Seongdong-gu	20	37.5506753	127.0409622
17	11290	성북구	Seongbuk-gu	17	37.606991	127.0232185
19	11710	송파구	Songpa-gu	19	37.5048534	127.1144822
21	11470	양천구	Yangcheon-gu	22	37.5270616	126.8561534
22	11560	영등포구	Yeongdeungpo-gu	23	37.520641	126.9139242
25	11170	용산구	Yongsan-gu	24	37.5311008	126.9810742
2	11380	은평구	Eunpyeong-gu	1	37.6176125	126.9227004
7	11110	종로구	Jongno-gu	6	37.5990998	126.9861493
12	11140	중구	Jung-gu	11	37.5579452	126.9941904
9	11260	중랑구	Jungnang-gu	8	37.5953795	127.0939669
 */
}
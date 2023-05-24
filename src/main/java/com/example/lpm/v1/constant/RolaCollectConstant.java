package com.example.lpm.v1.constant;

public class RolaCollectConstant {

    /**
     * 通过API收集ip
     */
    public static final String ROLA_COLLECT_BY_API_QUEUE_KEY = "ROLA_COLLECT_BY_API_QUEUE";

    /**
     * ROLA API收集IP启停标志 10 11
     */
    public static final String ROLA_COLLECT_BY_API_FLAG_KEY = "rola_collect_by_api:flag";

    /**
     * ROLA API
     */
    public static final String GET_IPS_LINK = "http://list.rola.info:8088/user_get_ip_list?token=";

    public static final String IP234_URL = "http://www.ip234.in/ip.json";

    /**
     * ROLA API收集IP 成功个数
     */
    public static final String ROLA_COLLECT_BY_API_SUCCESS_NUM = "rola_collect:api:successNum";

    /**
     * ROLA API收集IP 失败个数
     */
    public static final String ROLA_COLLECT_BY_API_FAIL_NUM = "rola_collect:api:failNum";

    /**
     * ROLA API收集IP 重复个数
     */
    public static final String ROLA_COLLECT_BY_API_DUPLICATE_NUM = "rola_collect:api:duplicateNum";

    /**
     * ROLA API收集IP 重复个数
     */
    public static final String ROLA_COLLECT_BY_API_RESULT = "rola_collect:api:result";

    /**
     * 通过API收集ip
     */
    public static final String ROLA_COLLECT_BY_SID_QUEUE_KEY = "ROLA_COLLECT_BY_SID_QUEUE_KEY";

    /**
     * ROLA SID收集IP启停标志 10 11
     */
    public static final String ROLA_COLLECT_BY_SID_FLAG_KEY = "rola_collect:sid:flag";

    /**
     * ROLA SID收集IP 成功个数
     */
    public static final String ROLA_COLLECT_BY_SID_SUCCESS_NUM = "rola_collect:sid:successNum";

    /**
     * ROLA SID收集IP 失败个数
     */
    public static final String ROLA_COLLECT_BY_SID_FAIL_NUM = "rola_collect:sid:failNum";

    /**
     * ROLA SID收集IP 重复个数
     */
    public static final String ROLA_COLLECT_BY_SID_DUPLICATE_NUM = "rola_collect:sid:duplicateNum";

    /**
     * ROLA SID收集IP 重复个数
     */
    public static final String ROLA_COLLECT_BY_SID_RESULT = "rola_collect:sid:result";

    public static final String ROLA_IP_BLOOM = "rola_ip_bloom";

}

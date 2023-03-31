package com.example.lpm.constant;

public class RedisKeyConstant {

    public static final String START_PROXY_KEY = "#START_PROXY_KEY:";

    public static final String START_PROXY_DELAYED_QUEUE_KEY = "#START_PROXY_DELAYED_QUEUE_KEY:";

    public static final String LOCK_IP_KEY = "#LOCK_IP_KEY";

    public static final String LOCK_IP_ID_KEY = "#LOCK_IP_ID_KEY:";

    public static final String LOCK_FILE_NAME_KEY = "#LOCK_FILE_NAME_KEY:";

    public static final String ROLA_CURRENT_KEY = "#ROLA_CURRENT";

    /**
     * ROLA收集IP启停标志
     * 10
     * 11
     */
    public static final String ROLA_COLLECT_FLAG_KEY = "#ROLA_COLLECT_FLAG";

    public static final String ROLA_COLLECT_ERROR_KEY = "#ROLA_COLLECT_ERROR";

    public static final String ROLA_CURRENT_FAIL_KEY = "#ROLA_CURRENT_FAIL_KEY";

    public static final String ROLA_CURRENT_REPEAT_KEY = "#ROLA_CURRENT_REPEAT_KEY";

    public static final String ROLA_TOTAL_KEY = "#ROLA_TOTAL";

    /**
     * 获取数据rola ip
     */
    public static final String ROLA_FIND_LOCK = "#ROLA_FIND_LOCK";

    public static final String ROLA_USER_NUM_KEY = "#ROLA_USER_NUM_KEY";

    /**
     * 页面收集rola
     */
    public static final String ROLA_COLLECT_IP_QUEUE_KEY = "#ROLA_COLLECT_IP_QUEUE_KEY";
    public static final String ROLA_COLLECT_IP_QUEUE_KEY_V2 = "#ROLA_COLLECT_IP_QUEUE_KEY_V2";

    /**
     * 手机收集rola
     */
    public static final String ROLA_PHONE_COLLECT_IP_QUEUE_KEY = "#ROLA_PHONE_COLLECT_IP_QUEUE_KEY";




    /**
     * 页面收集Luminati
     */
    public static final String LUMINATI_COLLECT_IP_QUEUE_KEY = "#LUMINATI_COLLECT_IP_QUEUE_KEY";
    /**
     * LUMINATI收集IP启停标志
     * 10
     * 11
     */
    public static final String LUMINATI_CURRENT_KEY = "#LUMINATI_CURRENT";

    public static final String LUMINATI_COLLECT_FLAG_KEY = "#LUMINATI_COLLECT_FLAG";

    public static final String LUMINATI_COLLECT_ERROR_KEY = "#LUMINATI_COLLECT_ERROR";

    public static final String LUMINATI_CURRENT_FAIL_KEY = "#LUMINATI_CURRENT_FAIL_KEY";

    public static final String LUMINATI_CURRENT_REPEAT_KEY = "#LUMINATI_CURRENT_REPEAT_KEY";

    public static final String LUMINATI_TOTAL_KEY = "#LUMINATI_TOTAL";
}

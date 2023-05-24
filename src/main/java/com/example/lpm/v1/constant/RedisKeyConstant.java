package com.example.lpm.v1.constant;

public class RedisKeyConstant {

    /**
     * 收集任务标志位，用来启动停止
     */
    public static final String COLLECTION_TASK_FLAG = "collectiont_task_flag_";
    /**
     * 需要收集的数量
     */
    public static final String COLLECTION_TASK_CURRENT = "collectiont_task_current_";
    /**
     * 收集任务 失败原因
     */
    public static final String COLLECTION_TASK_ERROR = "collectiont_task_error_";
    public static final String COLLECTION_TASK_TOTAL = "collectiont_task_total_";
    /**
     * 当天收集入库数量
     */
    public static final String COLLECTION_TASK_TODAY = "collectiont_task_today_";
    public static final String COLLECTION_TASK_FAIL = "collectiont_task_fail_";
    public static final String COLLECTION_TASK_REPEAT = "collectiont_task_repeat_";
    /**
     * 收录ip 任务队列
     */
    public static String COLLECTION_TASK_TOPIC = "collectiont_task_";
    /**
     * lua添加IP 任务队列
     */
    public static String ADD_PROXY_IP_TASK_TOPIC = "add_proxy_id_";
}

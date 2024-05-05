package com.yunji.titanrtx.common;

public class GlobalConstants {

    private static final String ROOT_PATH = "/titanrtx";
    public static final String AGENT_PATH = ROOT_PATH + "/agent";
    public static final String BARRIER_PATH = ROOT_PATH + "/barrier";
    private static final String LOCK_PATH = ROOT_PATH + "/lock";
    public static final String LOCK_TASK_DOWN_PATH = LOCK_PATH + "/taskDown";
    public static final String LOCK_TASK_REPORT_PATH = LOCK_PATH + "/taskReport";

    /**
     * cig -agent config listener path
     */
    public static final String CIA_PATH = ROOT_PATH + "/cia";
    public static final String CIA_RULES_PATH = CIA_PATH + "/rules";
    /**
     * manager auto create scene with link
     */
    public static final String MANAGER_PATH = ROOT_PATH + "/manager";
    public static final String MANAGER_SCENE_LINK_PATH = MANAGER_PATH + "/scene_link";
    public static final String MANAGER_AUTO_TEST_SCENE = MANAGER_PATH + "/top_test";
    public static final String AUTO_PATH = MANAGER_PATH + "/auto";
    /**
     * 自动化压测路径
     */
    public static final String AUTO_DEPLOYS_PATH = "/titanrtx/manager/auto/deploys";
    /**
     * 自动top300任务redis开关缓存key
     */
    public static final String AUTO_TOP_SWITCH_KEY = "auto_top_switch";


    public static final String PARAMS_HEADER_SEGMENT = "%%%";
    public static final String PARAMS_PAIR_SEGMENT = "=";
    public static final String PARAMS_SEGMENT = "&";
    public static final String UNDERLINE = "_";
    public static final String ALL = "*";
    public static final String LINE = "-";

    public static final String TASK_PREFIX = "task-";
    public static final String TABEL_PREFIX = "param_";
    public static final int PARAMS_EXPIRE = 2592000;

    public static final String URL_DECODER = "UTF-8";

    public static final String TITAN_RTX_PRESSURE_AGENT = "titan_rtx_pressure_agent";
    public static final String TITAN_RTX_PRESSURE_STATISTICS = "titan_rtx_pressure_statistics";

    public static final int HTTP_ERROR_CODE = 1000;
    public static final int HTTP_SUCCESS_CODE = 200;
    public static final int YUNJI_SUCCESS_CODE = 0;
    public static final int YUNJI_ERROR_CODE = 1000;


    public static final Integer GLOBAL_PARAMS_PAGE_SIZE = 10;
    public static final String TOP_LINK_MATE_DB_NAME = "topLink";
    public static final String CQ_TOP_LINK_MATE_MEASUREMENT_NAME = "cqTopLink";
    public static final String TOP_LINK_MATE_MEASUREMENT_NAME = "mate";
    public static final String TOP_LINK_MATE_CQ1H_MEASUREMENT_NAME = "mate_1h";
    public static final String TOP_LINK_MATE_CQ1D_MEASUREMENT_NAME = "mate_24h";


    public static final String DEFAULT_PARAM_FLAG = "titanrtx=true";


    public static final String TASK_ID = "task_id";
    public static final String TASK_TYPE = "task_type";


    public static final String ECS_ID_PATH = "/etc/hostname";
    public static final String CVM_ID_PATH = "http://metadata.tencentyun.com/latest/meta-data/instance-id";
    public static final String HOSTS_PATH = "/etc/hosts";
    public static final String RESTART_SH_PATH = "./restart.sh";
    public static final String DEFAULT_ECS_ID = "i-bp1207lqhyga222d09ti";
    public static final String DEFAULT_VCM_ID = "ins-g5mhcwkv";


    public static final String AGENTQPS_COLLECT_QPS_BD_NAME = "collectQps";
    public static final String AGENTQPS_SHARD_DURATION = "7d";
    public static final String AGENTQPS_COLLECT_QPS_MEASUREMENT_NAME = "qps";
    public static final String INFLUX_TAG_AGENT_IP = "agent_ip";
    public static final String INFLUX_TAG_PATH = "path";
    public static final String INFLUX_FILED_2XX = "2xx";
    public static final String INFLUX_FILED_3XX = "3xx";
    public static final String INFLUX_FILED_4XX = "4xx";
    public static final String INFLUX_FILED_5XX = "5xx";
    public static final String INFLUX_FILED_OTHER = "other";
    public static final String INFLUX_FILED_EXPIRED = "expired";
    public static final String INFLUX_FILED_RECEIVED = "received";
    public static final String INFLUX_FILED_ERROR = "error";
    public static final String INFLUX_FILED_SEND = "send";

    public static final String DATA_FACTORY_EXECUTE = "/task/execute";
    public static final String DATA_FACTORY_RESETTASK = "/task/resetsocket";

    public static final String JOB_DATA_DEFAULT_KEY = "default";
    public static final String JOB_DATA_SCENE_OPERATING_CENTER = "scene_operating_center";
    public static final String JOB_DATA_NOTIFY_CONFIG = "notify_config";
    public static final String JOB_DATA_BATCH_CENTER = "batch_center";


    public static final int BATCH_PROCESS_PARAM_SIZE = 20000;
//    public static final int BATCH_PROCESS_PARAM_SIZE = 2;


    /**
     * 参数fetcher 模式分界线,低于 2000 的参数,统一全部查询出来.
     */
    public static final int PARAM_TRANSMIT_BOUNDARY = 2000;

}

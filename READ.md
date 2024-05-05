# EEL 项目文档

eel 是一款支持多协议，高并发，多场景全链路压测工具

---

## 3.0版本新特性

- 更换http异步发送请求客户端，解决agent流量打不上去的BUG
- 增加agent数据采集功能
- 新增自动化工具模块


## 部署步骤
- 项目依赖 zookeeper 需要启动zookeeper,并修改agent,commander,manager等项目的属性文件zk地址
- 初始启动需要执行初始化init.sql文件,修改manager属性配置文件，修改管理员账户密码
- 分别启动manager，commander，agent 无先后循序规定


## Dubbo 集群压测

### dubbo集群压测,从zk上获取地址
请在 dubbo 链路管理的 dubbo 服务地址处填写 `N/A`

### dubbo 指定address 压测
在链路dubbo地址栏填写,可以填写一个地址或多个地址，以 , 区分.

- 10.0.0.1:21001
- 10.0.0.1:21001,10.0.0.2:21001


### link_params
- `link_params` 细分为 10 个表,这样再保存数据的时候，不用集中在一张表中了。




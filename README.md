# earth-frost

earth-frost是一个轻量级分布式任务调度框架。


## 介绍
- 调度模块和执行模块分离
- 使用redis作为数据库
- 基于订阅模式实现服务注册和发现

## 环境
- Angular: v1.x
- JDK: 1.8+
- Maven: 3+
- Redis: 2.8+
- Spring-boot: 2.x
- Thymeleaf: 3

## 功能
- 简单易上手，支持web界面进行任务的CRUD … ok
- 支持动态修改任务的开始，停止  … ok
- 调度中心支持集群部署，将任务的调度进行封装，支持分配各种不同的任务 … ok
- 执行器支持分布式，支持集群部署，可进行相应分组，在调度中心界面查看 … ok
- 支持伸缩扩展，调度中心和执行器都是基于redis订阅模式进行服务注册发现和任务调度，服务上下线发现及时 … ok
- 支持失败重试 … ok
- 任务监控和报警  … ok
- 动态编译任务，支持web界面编辑任务源码，创建任务  … ok
- 支持父子任务  … ok
- 运行报表  … coding

## 开发
	
	frost-api
		对外实体和接口
	frost-core
		定义调度、执行、注册发现、通知等核心功能的接口和抽象
	frost-support-redis
		以redis实现调度、执行、注册发现等接口的支持包
	frost-center
		调度中心服务，包含安全认证和UI展示，依赖core实现调度逻辑
	frost-executor
		执行器服务，依赖core实现任务执行逻辑，包含开发脚本任务的部分实例以及配置样例	
	frost-client
		客户端api，第三方项目可依赖client实现与调度中心交互
	
## 快速入门

### 启动Redis数据库
请下载并安装2.8+版本的Redis，单机或集群模式皆可。

调度中心和执行器均支持集群模式部署，集群模式下各节点需配置相同的Redis服务

### 导入源码
解压源码，按maven格式将源码导入IDE，源码结构如上述 [开发] 章节。

### 配置部署调度中心
#### 1.配置内容说明

```
# 登录账号
spring.boot.auth.enabled=true
spring.security.user.name=frost
spring.security.user.password=frost

# 报警通知发送邮件
spring.mail.host=smtp.mail.com
spring.mail.username=
spring.mail.password=

# 邮件通知
frost.notifier.mail.enabled=true
frost.notifier.mail.from=${spring.mail.username}
frost.notifier.mail.to=
frost.notifier.mail.subject=#{job.name} (#{job.id}) throws an exception
frost.notifier.mail.text=#{job.name} (#{job.id}) \n #{event.message}

# redis配置
# 0:单机模式， 1：集群模式
redisson.mode=0
redisson.address=redis://localhost:6379

```

#### 2.部署项目
正确进行上述配置，可将项目编译打包 mvn package(jar或war，相关打包操作参照Spring boot)，调度中心访问地址：http://localhost:20000/center，访问可进入登录界面
![登录页面](https://gitee.com/justlive1/earth-frost/raw/master/images/login.jpeg)

#### 3.集群模式
支持集群模式部署，提供调度系统可用性。

集群模式需要注意：保持 登录账号保持一致，保持使用redis配置相同。建议使用Nginx为调度中心做负载均衡

### 配置部署执行器

提供的frost-executor项目可直接使用，也可集成到现有业务项目中使用

#### 1.依赖jar
在执行器项目中依赖如下jar包

```
<dependency>
	<groupId>justlive.earth.breeze.frost</groupId>
	<artifactId>frost-support-redis</artifactId>
</dependency>
```

#### 2.配置说明

```
# 每个job支持并行处理数
frost.job.parallel=2

# 执行器名称
frost.job.executor.name=${spring.application.name}
# 执行器Key
frost.job.executor.key=executor-demo
frost.job.executor.ip=
frost.job.executor.port=${server.port}
# 是否支持执行脚本任务
frost.job.executor.scriptJobEnabled=true

# redis配置
# 0:单机模式， 1：集群模式
redisson.mode=0
redisson.address=redis://localhost:6379

```

#### 3.部署执行器
使用frost-executor项目开发的执行器直接mvn package即可，集成到现有项目按原有项目打包部署，部署后打开调度中心页面查看执行器页面
![执行器列表](https://gitee.com/justlive1/earth-frost/raw/master/images/executor.jpeg)
列表中出现部署的执行器则说明部署成功

### 开发一个简单的任务
该案例使用脚本任务模式，比使用实例模式只需要部署调度中心和执行器即可，脚本任务可在调度中心在线维护

#### 1.新增任务
登录调度中心，在任务管理界面点击新增任务，任务类型选择脚本模式，输入相应参数，点击保存。
![任务列表](https://gitee.com/justlive1/earth-frost/raw/master/images/job.jpeg)
![添加脚本任务](https://gitee.com/justlive1/earth-frost/raw/master/images/addScriptJob.jpeg)

#### 2.脚本模式开发
在任务管理列表的操作栏，点击刚新建任务的脚本按钮，进入脚本脚本编辑页面。系统已经初始化了示例脚本任务，可按需进行修改。
![修改脚本](https://gitee.com/justlive1/earth-frost/raw/master/images/script.jpeg)

#### 3.触发执行
点击任务右侧“触发一次”按钮可触发一次任务(通常是配置cron定时触发)

#### 4.查看结果
进入调度记录页面可查看调度和执行结果
![调度记录](https://gitee.com/justlive1/earth-frost/raw/master/images/record.jpeg)


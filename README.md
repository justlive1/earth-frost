# earth-frost

earth-frost是一个轻量级分布式任务调度框架。


## 介绍
- 调度模块和执行模块分离
- 使用redis作为数据库
- 基于订阅模式实现服务注册和发现

![登录页面](https://raw.githubusercontent.com/justlive1/earth-frost/master/images/login.png)
![执行器列表](https://raw.githubusercontent.com/justlive1/earth-frost/master/images/executor.png)
![任务列表](https://raw.githubusercontent.com/justlive1/earth-frost/master/images/job.png)
![添加任务](https://raw.githubusercontent.com/justlive1/earth-frost/master/images/addjob.png)
![调度记录](https://raw.githubusercontent.com/justlive1/earth-frost/master/images/record.png)



## 功能
- 简单易上手，支持web界面进行任务的CRUD … coding
- 支持动态修改任务的开始，停止  … ok
- 调度中心支持集群部署，将任务的调度进行封装，支持分配各种不同的任务 … ok
- 执行器支持分布式，支持集群部署，可进行相应分组，在调度中心界面查看 … coding
- 支持伸缩扩展，调度中心和执行器都是基于redis订阅模式进行服务注册发现和任务调度，服务上下线发现及时 … ok
- 支持失败重试 … coding
- 任务监控和报警  … coding
- 动态编译任务，支持web界面编辑任务源码，创建任务  … coding
- 支持父子任务  … coding
- 运行报表  … coding

## 开发

	frost-core
		核心包，包括通用实体，配置和接口定义
		
	frost-center
		调度中心
	
	frost-client
		客户端api
	
	frost-executor
		执行器
		
	frost-support-redis
		以redis实现接口的支持包
	
## 部署
[Release](https://gitee.com/justlive1/earth-frost/releases)

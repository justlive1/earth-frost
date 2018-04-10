# earth-frost

earth-frost是一个轻量级分布式任务调度框架。


## 介绍
- 调度模块和执行模块分离
- 使用redis作为数据库
- 基于订阅模式实现服务注册和发现

## 功能
- 简单
- 调度中心
- 执行器

## 开发

	frost-core
		核心包
		
	frost-center
		调度中心
	
	frost-client
		客户端api
	
	frost-executor
		执行器
	
## 部署
[Release](https://gitee.com/justlive1/earth-frost/releases)
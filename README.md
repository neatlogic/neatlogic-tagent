中文 / [English](README.en.md)

## tagent注册

### 1、配置
（1）tagent端配置：<br>
进入/opt/tagent/run/root/conf目录<br>
编辑tagent.conf文件<br>
关键参数说明：<br>

|           参数           |               备注                | 是否必填  |
|:----------------------:|:-------------------------------:|:-----:|
|       credential       |             加密后的密码串             |   是   |
|      listen.port       |            tagent的端口            |   是   |
|      proxy.group       |         runner组ip:port          |   否   |
|     proxy.group.id     |            runner组id            |   否   |
| proxy.registeraddress  | tagent在runner的注册地址，还需带上租户的uuid  |   是   |
|       tagent.id        |            tagent id            |   否   |
|         tenant         |             租户uuid              |   是   |


以安装在192.168.0.25的tagent、192.168.0.21的runner（服务端口为8084，心跳端口为8888）、192.168.0.25的neatlogic（租户为test）为例：
```
credential={ENCRYPTED}19chdeh34c738cb575fef816607
exec.timeout=900
listen.addr=0.0.0.0
listen.backlog=16
listen.port=3939
proxy.group=192.168.0.21:8888
proxy.group.id=
proxy.registeraddress=http://192.168.0.21:8084/autoexecrunner/public/api/rest/tagent/register?tenant=test
read.timeout=5
tagent.id=123
tenant=test
```

（2）neatlogic端配置<br>
在执行器组管理页面添加runner组，网段的范围必须包含tagent的ip地址。
![img.png](README_IMAGES/img.png)
![img.png](README_IMAGES/img1.png)

### 2、启动tagent服务

**1、启动tagent服务命令**
```
service tagent start
或者：
/bin/systemctl start tagent.service
```
**2、停止tagent服务命令**
```
service tagent stop
或者：
/bin/systemctl stop tagent.service 
```

### 3、注册时三端流程图

![img.png](README_IMAGES/img2.png)

注：<br>
（1）在neatlogic注册时，先是判断tagent的ip在哪一个runner组的网段内，匹配到runner组，再使用组内的runner；<br>
（2）tagent注册到neatlogic后，tagent和runner之间的心跳正常就会更新tagent的信息到neatlogic。<br>
（3）在neatlogic端一系列对tagent的操作，都会通过runner端执行tagent的命令
<br>
<br>
### 4、注册时id、ip相关逻辑
![img_1.png](README_IMAGES/img4.png)
注：<br>
（1）两个tagent的包含ip可以重叠，但不可以包含其他tagent的主ip，注册时出现此情况会抛异常【包含了其他tagent主ip】<br>
（2）通过tagent主ip找到两个及两个以上tagent，抛异常【当前主ip被多个tagent包含】<br>
（3）注册发现tagent仍在连接状态，抛异常【ip冲突】<br>
（4）port相同时，注册ip被已存在的tagent所包含，视为同一个tagent<br>
（5）ip相同、port不相同视为另一个tagent<br>
（6）port相同时，每一个ip都会对应一个tagent账号，包含ip重叠时，一个ip只会对应一个账号<br>
中文 / [English](README.en.md)
<p align="left">
    <a href="https://opensource.org/licenses/Apache-2.0" alt="License">
        <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" /></a>
<a target="_blank" href="https://join.slack.com/t/neatlogichome/shared_invite/zt-1w037axf8-r_i2y4pPQ1Z8FxOkAbb64w">
<img src="https://img.shields.io/badge/Slack-Neatlogic-orange" /></a>
</p>

---

## 关于

neatlogic-agent是[Tagent](../../../neatlogic-tagent-client/blob/master/README.md)的维护平台，主要用于管理tagent的状态。

## Tagent注册

### 配置

#### Tagent端配置

进入/opt/tagent/run/root/conf目录，编辑tagent.conf文件， 关键参数说明：

| 参数                    | 备注                             | 是否必填 |
|:----------------------|:-------------------------------|:-----|
| credential            | 加密后的密码串                        | 是    |
| listen.port           | tagent的端口                      | 是    |
| proxy.group           | runner组ip:port                 | 否    |
| proxy.group.id        | runner组id                      | 否    |
| proxy.registeraddress | tagent在runner的注册地址，还需带上租户的uuid | 是    |
| tagent.id             | tagent id                      | 否    |
| tenant                | 租户uuid                         | 是    |

以安装在192.168.0.25的tagent、192.168.0.21的runner（服务端口为8084，心跳端口为8888）、192.168.0.25的neatlogic（租户为test）为例：

``` properties
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

#### Neatlogic端配置

在执行器组管理页面添加runner组，网段的范围必须包含tagent的ip地址。
![img.png](README_IMAGES/img.png)
![img.png](README_IMAGES/img1.png)

#### 启动tagent服务

**1、启动tagent服务命令**

``` shell
service tagent start
或者：
/bin/systemctl start tagent.service
```

**2、停止tagent服务命令**

``` shell
service tagent stop
或者：
/bin/systemctl stop tagent.service 
```

## 注册时三端流程图

![img.png](README_IMAGES/img2.png)

注意：

+ 在neatlogic注册时，先是判断tagent的ip在哪一个runner组的网段内，匹配到runner组，再使用组内的runner。
+ tagent注册到neatlogic后，tagent和runner之间的心跳正常就会更新tagent的信息到neatlogic。
+ 在neatlogic端一系列对tagent的操作，都会通过runner端执行tagent的命令

## 注册时id、ip相关逻辑

![img_1.png](README_IMAGES/img3.png)
注意：

+ 两个tagent的包含ip可以重叠，但不可以包含其他tagent的主ip，注册时出现此情况会抛异常【包含了其他tagent主ip】。
+ 通过tagent主ip找到两个及两个以上tagent，抛异常【当前主ip被多个tagent包含】。
+ 注册发现tagent仍在连接状态，抛异常【ip冲突】。
+ port相同时，注册ip被已存在的tagent所包含，视为同一个tagent。
+ ip相同、port不相同视为另一个tagent。
+ port相同时，每一个ip都会对应一个tagent账号，包含ip重叠时，一个ip只会对应一个账号。
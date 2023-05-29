[中文](README.md) / English
<p align="left">
    <a href="https://opensource.org/licenses/Apache-2.0" alt="License">
        <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" /></a>
    <a target="_blank" href="https://join.slack.com/t/neatlogichome/shared_invite/zt-1w037axf8-r_i2y4pPQ1Z8FxOkAbb64w">
        <img src="https://img.shields.io/badge/Slack-Neatlogic-orange" /></a>
</p>

---

## About

neatlogic-agent is the maintenance platform for [Tagent](../../../neatlogic-tagent-client/blob/master/README.en.md),
mainly used to manage the status of tagent.

## Tagent Registration

### Configuration

#### Tagent-side Configuration

Go to the /opt/tagent/run/root/conf directory and edit the tagent.conf file. Key parameter details:

| Parameter             | Remarks                                                         | Required |
|:----------------------|:----------------------------------------------------------------|:---------|
| credential            | Encrypted password string                                       | Yes      |
| listen.port           | Port of the tagent                                              | Yes      |
| proxy.group           | runner group ip:port                                            | No       |
| proxy.group.id        | runner group id                                                 | No       |
| proxy.registeraddress | Registration address of tagent in runner, including tenant UUID | Yes      |
| tagent.id             | tagent id                                                       | No       |
| tenant                | Tenant UUID                                                     | Yes      |

Example for a tagent installed on 192.168.0.25, runner on 192.168.0.21 (service port 8084, heartbeat port 8888), and
neatlogic on 192.168.0.25 (tenant: test):

```properties
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

#### Neatlogic-side Configuration

Add the runner group on the Executor Group Management page, and the network range must include the tagent's IP address.
![img.png](README_IMAGES/img.png)
![img.png](README_IMAGES/img1.png)

#### Start Tagent Service

**1. Start the Tagent service command**

```shell
service tagent start
or:
/bin/systemctl start tagent.service
```

**2. Stop the Tagent service command**

```shell
service tagent stop
or:
/bin/systemctl stop tagent.service 
```

## Registration Flow Among the Three Parties

![img.png](README_IMAGES/img2.png)

Note:

+ During the registration in neatlogic, the IP of the tagent is first determined to be within which runner group's
  network range. Once matched with a runner group, the runner within the group will be used.
+ After tagent registers with neatlogic, the information of tagent will be updated to neatlogic as long as the heartbeat
  between tagent and runner is normal.
+ A series of operations on tagent in neatlogic will be executed through the runner.

## Logic of Registration ID and IP

![img_1.png](README_IMAGES/img3.png)

Note:

+ Overlapping IPs of two tagents are allowed, but they cannot contain the primary IP of

other tagents. An exception will be thrown during registration if this situation occurs【Contains the primary IP of other
tagents】.

+ If two or more tagents are found through the primary IP of tagent, an exception will be thrown【The current primary IP
  is included in multiple tagents】.
+ If tagent is still in a connected state during registration, an exception will be thrown【IP conflict】.
+ When the port is the same, if the registration IP is contained by an existing tagent, it is considered the same
  tagent.
+ If the IP is the same but the port is different, it is considered a different tagent.
+ When the port is the same, each IP will correspond to a tagent account. In the case of overlapping IPs, one IP will
  only correspond to one account.
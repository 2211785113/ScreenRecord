# 录屏推流

## 测试

方法一：斗鱼测试

第一步：注册并实名认证

第二步：[斗鱼网址](https://www.douyu.com/) 鼠标触下头像-点个人中心-鼠标再触下头像-点直播设置-打开直播开关-下方进入直播间（如果不能开播，手机下载斗鱼点头像-点我要开播）

第三步：进入直播间-鼠标触下头像-会看到推流码，复制推流码到项目MainActivity代码中间用/连接

第四步：运行代码-开始录屏-刷新斗鱼直播间

方法二：本地IP地址+端口测试,需搭建nginx服务器，随后补充

## 项目介绍

screenPush模块：子module，录屏推流代码

app：为demo代码，参考MainActivity集成进项目即可。

方式一：开启子线程进行录屏(废弃)

方式二：开启服务进行录屏

## 技术要点

[多线程和服务组件Service的基本使用](https://www.jianshu.com/p/6754a030f662)

[录屏的三种实现方案](https://www.jianshu.com/p/8b313692ac85)

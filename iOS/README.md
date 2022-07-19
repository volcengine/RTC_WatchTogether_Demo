一起看是火山引擎实时音视频提供的一个开源示例项目。本文介绍如何快速跑通该示例项目，体验一起看效果。

## 应用使用说明

使用该工程文件构建应用后，即可使用构建的应用体验一起看。
你和你的同事必须加入同一个房间，才能共同体验一起看。

## 前置条件

- [Xcode](https://developer.apple.com/download/all/?q=Xcode) 12.0+
	

- iOS 12.0+ 真机
	

- 有效的 [AppleID](http://appleid.apple.com/)
	

- 有效的 [火山引擎开发者账号](https://console.volcengine.com/auth/login)
	

- [CocoaPods](https://guides.cocoapods.org/using/getting-started.html#getting-started) 1.10.0+
	

## 操作步骤

### **步骤 1：获取 AppID 和 AppKey**

在火山引擎控制台->[应用管理](https://console.volcengine.com/rtc/listRTC)页面创建应用或使用已创建应用获取 AppID 和 AppAppKey

### **步骤 2：获取 AccessKeyID 和 SecretAccessKey**

在火山引擎控制台-> [密钥管理](https://console.volcengine.com/iam/keymanage/)页面获取 **AccessKeyID 和 SecretAccessKey**

### 步骤 3：获取 Partner 和 Category

关于 **Partner** 和 **Category** 的获取，参看[获取个性化内容](https://www.volcengine.com/docs/6392/75762)。

### 步骤 4：构建工程

1. 打开终端窗口，进入 `RTC_Feedshare_Demo-master/iOS/veRTC_Demo_iOS` 根目录
	

<img src="https://lf3-volc-editor.volccdn.com/obj/volcfe/sop-public/upload_c62f65eae748ffbe919548b1a392424d" width="500px" >

2. 执行 `pod install` 命令构建工程
	

<img src="https://lf6-volc-editor.volccdn.com/obj/volcfe/sop-public/upload_74efd4057f0bbd5530e88f042794919d" width="500px" >

3. 进入 `RTC_Feedshare_Demo-master/iOS/veRTC_Demo_iOS` 根目录，使用 Xcode 打开 `veRTC_Demo.xcworkspace`
	

<img src="https://lf3-volc-editor.volccdn.com/obj/volcfe/sop-public/upload_eb7482010702eeaefdb73e8f6d8eca8c" width="500px" >

4. 在 Xcode 中打开 `Pods/Development Pods/Core/BuildConfig.h` 文件
	

<img src="https://lf3-volc-editor.volccdn.com/obj/volcfe/sop-public/upload_5b00fece3b4f69c114412384b25750a8" width="500px" >

5. 填写 **LoginUrl**
	

当前你可以使用 **`http://rtc-test.bytedance.com/rtc_demo_special/login`** 作为测试服务器域名，仅提供跑通测试服务，无法保障正式需求。

<img src="https://lf3-volc-editor.volccdn.com/obj/volcfe/sop-public/upload_9bc1de9e70cfff092955c291750111cc" width="500px" >

6. **填写 APPID、APPKey、AccessKeyID 和 SecretAccessKey**
	

使用在控制台获取的 **APPID、APPKey、AccessKeyID 和 SecretAccessKey** 填写到 `BuildConfig.h`文件的对应位置**。** 

<img src="https://lf6-volc-editor.volccdn.com/obj/volcfe/sop-public/upload_3067e73eb3569f78d58e609f4f95435c" width="500px" >

7. 填写 **Partner** 和 **Category**
	

打开`Pods/Development Pods/FeedShareDemo/FeedShareDemoConstants.h` 文件，使用获取的 **Partner** 和 **Category** 填写到对应位置。

<img src="https://lf3-volc-editor.volccdn.com/obj/volcfe/sop-public/upload_aa5c385bd7c87602423e2ec1679531d0" width="500px" >

### **步骤 5：配置开发者证书**

1. 将手机连接到电脑，在 `iOS Device` 选项中勾选您的 iOS 设备。
	

<img src="https://lf6-volc-editor.volccdn.com/obj/volcfe/sop-public/upload_daf82d2f631ec62ea4dd23821acb3fa8" width="500px" >
<br>

2. 登录 Apple ID。
	

2.1 选择 Xcode 页面左上角 **Xcode** > **Preferences**，或通过快捷键 **Command** + **,**  打开 Preferences。
2.2 选择 **Accounts**，点击左下部 **+**，选择 Apple ID 进行账号登录。

<img src="https://lf6-volc-editor.volccdn.com/obj/volcfe/sop-public/upload_fef715bb86ed67f87b0eb72c825b6814" width="500px" >

3. 配置开发者证书。
	

3.1 单击 Xcode 左侧导航栏中的 `VeRTC_Demo` 项目，单击 `TARGETS` 下的 `VeRTC_Demo` 项目，选择 **Signing & Capabilities** > **Automatically manage signing** 自动生成证书

<img src="https://lf6-volc-editor.volccdn.com/obj/volcfe/sop-public/upload_77848428bd8cd7351ee9674c6ff15dca" width="500px" >

3.2 在 **Team** 中选择 Personal Team。

<img src="https://lf6-volc-editor.volccdn.com/obj/volcfe/sop-public/upload_9e8c2433636e1e542fd6dde26cb082bf" width="500px" >

3.3 **修改 Bundle** **Identifier****。** 

默认的 `vertc.veRTCDemo.ios` 已被注册， 将其修改为其他 Bundle ID，格式为 `vertc.xxx`。

<img src="https://lf3-volc-editor.volccdn.com/obj/volcfe/sop-public/upload_e12ccd39f6fdb55e8fab48472ab2400a" width="500px" >

### **步骤 6：编译运行**

选择 **Product** > **Run**， 开始编译。编译成功后你的 iOS 设备上会出现新应用。若为免费苹果账号，需先在`设置->通用-> VPN与设备管理 -> 描述文件与设备管理`中信任开发者 APP。

<img src="https://lf6-volc-editor.volccdn.com/obj/volcfe/sop-public/upload_b80500f67cc5a86a4caebc04daa98b36" width="500px" >

运行开始界面如下：

<img src="https://lf6-volc-editor.volccdn.com/obj/volcfe/sop-public/upload_d30af5d9dac293cc2da1025218067c8c" width="200px" >

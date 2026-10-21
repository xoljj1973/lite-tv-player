# Lite TV Player（轻量直播播放器）

极简 IPTV 播放器：填一个 M3U 地址即可看直播。手机 / Android TV 双端可用。

## 使用

1. 安装 APK 后打开，点击右上角「设置源」
2. 填入 M3U/M3U8 播放列表地址（http/https 均支持）
3. 点选频道播放；TV 上用遥控器**上下键或频道键**换台

## 构建

- 本地：`gradle assembleDebug`（需 JDK 17 + Android SDK）
- 云端：推送到 GitHub 后 Actions 自动打包，在 Artifacts 下载 APK

## 技术栈

Kotlin + Media3/ExoPlayer（含 HLS 支持），minSdk 21（Android 5.0+）。

注意：若你的直播源是 http 明文地址，应用已开启 `usesCleartextTraffic`，无需额外配置。

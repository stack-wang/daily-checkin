# 每日打卡

一款 Android 打卡习惯追踪应用，支持自定义打卡项目、每日提醒、补卡和数据统计。

## 功能

- **打卡项目管理** — 新建、编辑、删除打卡项目，自定义图标、颜色和提醒时间
- **每日打卡** — 首页展示今日待打卡列表，一键打卡
- **智能提醒** — 每个项目可独立设置每日提醒时间，到点推送通知
- **补卡** — 可补最近 N 天内的漏打卡（默认 7 天，可配置）
- **数据统计** — 打卡热力图、连续天数、打卡率、月度统计

## 技术栈

| 技术 | 用途 |
|------|------|
| Kotlin + Jetpack Compose | UI 框架 |
| Room | 本地数据库 |
| Hilt | 依赖注入 |
| WorkManager | 定时提醒 |
| Navigation Compose | 页面导航 |
| MPAndroidChart | 图表库 |

## 构建

用 Android Studio 打开项目，Sync Gradle 后 Build → Build APK(s)。

或通过 GitHub Actions 自动构建：推送代码后，在 Actions 标签页下载 `app-debug.apk`。

## 待实现

- [ ] 数据备份/恢复
- [ ] 数据导出（JSON/CSV）
- [ ] 桌面小组件（Widget）
- [ ] 深色模式

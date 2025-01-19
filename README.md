# Notion Accounting App

一个基于 Notion API 的 Android 记账应用，可以将你的收支记录同步到 Notion 数据库中。

## 功能特点

- 📱 原生 Android 应用，使用 Jetpack Compose 构建现代化 UI
- 💰 支出记录
- 📊 可视化统计展示（日/周/月度支出趋势）
- 🗂️ 多种支出分类
- 📅 日历视图查看每日支出
- ↕️ 下拉刷新同步数据
- 🌙 支持浅色/深色主题
- 🔄 实时同步到 Notion 数据库

## 开始使用

### 前置要求

1. 一个 Notion 账号
2. Notion API Key
3. Android 8.0 (API 26) 或更高版本的设备

### 获取 Notion API Key

1. 访问 [Notion Developers](https://developers.notion.com/)
2. 点击 "View my integrations"
3. 创建一个新的 integration
4. 复制生成的 API Key

### 设置 Notion 数据库

1. 在 Notion 中创建三个数据库：
   - 交易记录数据库
   - 月度总结数据库
   - 周度总结数据库

2. 为每个数据库添加必要的属性：
   - （等我找时间出个图文教程或者整理个模版出来吧）

3. 分享数据库给你的 integration（点击右上角的 Share 按钮，选择你创建的 integration）

4. 复制各个数据库的 ID（从数据库 URL 中获取）

### 配置应用

1. 打开应用，进入"设置"页面
2. 填入以下信息：
   - Notion API Key
   - 交易记录数据库 ID
   - 月度总结数据库 ID
   - 周度总结数据库 ID
3. 点击"保存设置"

## 使用说明

### 记录交易

1. 点击底部导航栏的"添加"按钮
2. 选择交易类型（收入/支出）
3. 输入金额
4. 选择分类
5. 添加备注（可选）
6. 点击"记一笔"按钮保存

### 查看统计

1. 点击底部导航栏的"统计"按钮
2. 可以查看：
   - 今日支出
   - 本周支出
   - 本月支出
   - 月度支出趋势图
   - 日历视图

### 管理交易记录

1. 点击底部导航栏的"记录"按钮
2. 向左滑动某条记录可以删除
3. 下拉可以刷新同步数据

## 隐私说明

- 所有数据都存储在你的 Notion 数据库中
- API Key 仅保存在本地设备
- 不收集任何用户个人信息

## 技术栈

- Kotlin
- Jetpack Compose
- Material Design 3
- Hilt（依赖注入）
- Retrofit（网络请求）
- Coroutines（异步处理）
- Vico（图表库）


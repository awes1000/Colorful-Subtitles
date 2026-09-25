# Changelog

## 1.10.0

### 适配
- 迁移到 Minecraft 26.3 / Fabric Loader 0.19.5 / Fabric Loom 1.18.2 / Fabric API 0.161.0 / Cloth Config 26.3.159 / Mod Menu 21.0.0。

## 1.9.2

### 新增
- 配置界面可直接编辑默认文字颜色(default_color.text),不再只能手动改 JSON。

### 修复
- 消除 PaletteColorEntry 对 Cloth Config 已废弃 API 的编译警告。

## 1.9.1

### 修复
- 背景色注入点(`modifyBackgroundDrawColor`)补充空值保护,消除渲染早期 `iterationEntry` 未赋值时的 NPE 风险。
- GUI 保存时不再把默认文字颜色硬编码为白色 — 现保留配置文件中的 `default_color.text` 原值,手动编辑 JSON 设置的默认文字色不会再被 GUI 保存覆盖。
- 所有 Mixin `@Unique` 字段统一加上 `colorfulsubtitles$` 前缀,避免与原版未来同名字段冲突时被 Mixin 静默重命名。

### 文档
- 补记 1.8.1 / 1.9.0 版本日志;README 依赖版本与 26.2 实际构建配置同步。
- 新增 `AGENT.md`,固化项目架构与开发规范。

## 1.9.0

### 适配
- 迁移到 Minecraft 26.2 / Fabric Loader 0.19.3 / Fabric API 0.152.2 / Cloth Config 26.2.155 / Mod Menu 20.0.0-beta.3。

### 优化
- 重做背景色控制选项与全局配置逻辑:全局默认背景启用时覆盖所有分类背景,GUI 中分类背景控件随之隐藏,三处逻辑(着色决策、显示条件、tooltip 文案)保持自洽。

## 1.8.1

### 新增
- 兼容 Modern UI:文字颜色改写按是否加载 Modern UI 分流两条注入路径,互不干扰。
- 配置界面新增自绘 HSV 调色板与 alpha 滑条(`PaletteColorEntry`),可视化调整颜色。

### 修复
- 首次运行未生成完整配置文件的问题 — 现启动即物化全量默认配置。
- 配置文件出错时无法自动修复的问题 — 解析失败自动回退默认并回写,缺键自动补全。

## 1.8.0

### 适配
- 迁移到 Minecraft 26.1.2 / Fabric Loader 0.19.2 / Fabric Loom 1.16.2 / Java 25。
- `fabric.mod.json` 依赖收紧:`fabricloader >=0.19.0`、`minecraft >=26.1.2`、新增 `java >=25`。
- Mixin 兼容级别从 `JAVA_8` 升至 `JAVA_25`。

### 新增
- 默认配置首次启动会写入 `config/colorfulsubtitles.json`,用户可直接编辑文件查看所有可用键。
- 集成 Mod Menu 与 Cloth Config:从 Mods 列表打开 Colorful Subtitles 即可在 GUI 内为每个 `SoundSource` 自定义文字色与背景色。
- 背景色支持 8 位 hex(`#AARRGGBB`),实现真正的透明 / 半透明背景;6 位 hex(`#RRGGBB`)向后兼容,自动补 alpha=FF。

### 修复
- 配置文件已存在但内容为空时,默认值不会被写入 — 现已在文件不存在或为空时一并物化默认配置。
- 背景色覆盖原本通过 `color | backgroundColor` 与原生颜色按位或叠加,导致用户设置的 alpha 不生效;现改为直接覆盖,alpha 真实生效。
- `#FFFFFF` 等数值与"无覆盖"哨兵 `-1` 冲突,导致不透明白色被误判为未设置;现以独立布尔字段记录是否覆盖,任意 ARGB 值都能正确生效。
- 8 位 hex 解析顺序与 Cloth Config GUI 不一致(早期实现按 `#RRGGBBAA`),导致 `#FFFFFF00` 显示为黄色;现统一为 `#AARRGGBB`。

# Changelog

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

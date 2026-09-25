# AGENT.md — Colorful Subtitles 开发约束

本文档面向后续参与开发的所有人和 AI 代理,描述项目架构、编码规范与开发流程。**修改代码前请先通读本文档;修改行为与本文档冲突时,以本文档为准,除非同时更新本文档。**

## 1. 项目概述

- **定位**:Fabric 客户端 mod,按声音分类(`SoundSource`)为字幕(subtitle)着色,支持文字颜色与可选背景颜色,提供 Mod Menu + Cloth Config 的 GUI 配置界面(含自绘调色板)。
- **环境**:纯客户端(`"environment": "client"`),不含任何服务端逻辑,新代码不得引入服务端依赖。
- **本仓库为个人用途 fork**(上游 haykam821/awes1000),发布前必须实测。

## 2. 技术栈与版本单一来源

所有版本号**只在 `gradle.properties` 中定义**,其他地方(`fabric.mod.json` 的 `depends`、README)必须与其保持一致或通过 `processResources` 展开。

| 组件 | 当前值 | 定义位置 |
| --- | --- | --- |
| Minecraft | 26.3 | `gradle.properties` → `minecraft_version` |
| Fabric Loader | 0.19.5 | `loader_version` |
| Fabric Loom | 1.18.2 | `loom_version` |
| Fabric API | 0.161.0+26.3 | `fabric_api_version` |
| Cloth Config | 26.3.159 | `cloth_config_version` |
| Mod Menu | 21.0.0 | `modmenu_version` |
| Java | 25(`options.release = 25`) | `build.gradle` |
| Gradle | 9.4.0(wrapper) | `gradle/wrapper/gradle-wrapper.properties` |
| 映射 | Mojang 官方命名(代码/AW 均用 official 名) | — |

**升级版本时的连带修改清单**(缺一不可):
1. `gradle.properties` 中对应版本号;
2. `fabric.mod.json` 的 `depends`(`minecraft`、`java` 为手写值,`fabric-api`、`cloth-config` 由 `processResources` 自动展开,勿手改);
3. `README.md` 的 Requirements 一节;
4. `CHANGELOG.md` 新增条目;
5. mod 版本号 `mod_version` 按语义化递增(适配新 MC 版本至少升 minor);
6. 升级 MC 版本后**必须进游戏实测字幕渲染**(见 §7),因为 mixin 注入点对字节码形状敏感。

## 3. 目录结构

```
src/main/java/io/github/haykam821/colorfulsubtitles/
├── ColorfulSubtitles.java          # 入口门面:MOD_ID、LOGGER、配置的加载/保存/修复(唯一的配置 IO 入口)
├── ColorfulSubtitlesClient.java    # ClientModInitializer,仅触发配置预加载
├── ColorHolder.java                # duck interface:字幕条目的颜色状态读写 + setColor 着色决策逻辑
├── config/
│   ├── ColorfulSubtitlesConfig.java   # 配置数据模型 + CODEC(宽松)/FULL_CODEC(严格)
│   ├── ColorfulSubtitlesCodecs.java   # SoundSource 映射相关 Codec 工具
│   └── SubtitleColor.java             # 单个颜色项(text + 可选 background)+ hex 解析/格式化
├── mixin/
│   ├── SubtitleEntryMixin.java     # 给 SubtitleOverlay.Subtitle 附加颜色字段(实现 ColorHolder)
│   └── SubtitlesHudMixin.java      # 拦截 SubtitleOverlay 渲染与 onPlaySound,应用颜色
└── modmenu/
    ├── ColorfulSubtitlesModMenu.java      # ModMenuApi 入口
    ├── ColorfulSubtitlesConfigScreen.java # Cloth Config 配置界面构建
    └── PaletteColorEntry.java             # 自绘 HSV 调色板 + alpha 条的 ColorEntry 子类

src/main/resources/
├── fabric.mod.json                 # mod 元数据(version 等占位符由 processResources 展开)
├── colorfulsubtitles.mixin.json    # mixin 注册(仅 client 列表)
├── colorfulsubtitles.accesswidener # AW v2, official 命名
└── assets/colorfulsubtitles/lang/  # en_us.json + zh_cn.json,键必须一一对应
```

**分层规则**:
- `config/` 不得依赖 `mixin/` 与 `modmenu/`;`mixin/` 通过 `ColorHolder` 接口与其余代码解耦,不得直接依赖 `modmenu/`;渲染决策逻辑(选哪个颜色)放在 `ColorHolder.setColor`,不要散落到 mixin 注入方法里。
- 配置文件读写只允许经过 `ColorfulSubtitles` 的静态方法,其他类不得自行读写 `config/colorfulsubtitles.json`。

## 4. 架构与关键决策

### 4.1 配置系统
- 配置文件:`config/colorfulsubtitles.json`,Gson pretty-print,颜色以字符串 hex 存储。
- **双 Codec 策略**:读取用 `CODEC`(所有字段 `optionalFieldOf`,容忍缺键),写出用 `FULL_CODEC`(所有字段必填),保证落盘文件永远完整。新增配置字段必须同时更新两个 Codec、`isCompleteConfig`、`withDefaults` 三处。
- **自愈规则**(不得回退):文件不存在或为空 → 写入默认;解析失败 → 记 warn 日志并用默认覆盖;缺键 → `withDefaults` 补全并回写。任何配置改动不得让用户手上的旧配置文件变成启动失败。
- **颜色 hex 格式**:统一 `#AARRGGBB`;6 位 `#RRGGBB` 视为 alpha=FF 的向后兼容输入;文字颜色额外接受 Minecraft 命名颜色(如 `red`)。序列化时 alpha=FF 输出 6 位,否则 8 位。此格式已写入 1.8.0 CHANGELOG,**不得更改字节序**(历史上 `#RRGGBBAA` 的写法是 bug,已修复)。
- **背景色优先级**:全局默认背景(`default_color.background`)存在时**覆盖**所有分类背景;分类背景其次;都没有则使用原版背景色。GUI 中分类背景控件通过 `setDisplayRequirement`/`setDisplayCondition` 在全局背景启用时隐藏。改动此逻辑必须同步改 `ColorHolder.setColor`、GUI 显示条件、`tooltip.colorfulsubtitles.default.has_background` 两种语言的文案,三者必须自洽。
- "无背景覆盖"用 `Optional<Integer>` / 独立布尔表达,**禁止用 `-1` 之类哨兵值**(历史 bug:与 `#FFFFFF` 冲突)。

### 4.2 Mixin 层
- 附加状态一律走 **duck interface**(`ColorHolder`)+ 字段注入(`SubtitleEntryMixin`),不要用 `ThreadLocal`、静态 Map 或反射。
- `SubtitlesHudMixin` 通过 `@Redirect` iterator.next(ordinal=2)记录当前渲染条目,再用 `@ModifyArg`/`@ModifyExpressionValue` 改写颜色参数。这些注入点**与 26.3 的 `extractRenderState` 字节码强绑定**,是全项目最脆弱的部分——改动或升级后必须实测。
- Modern UI 兼容:文字颜色有两条互斥路径(原版 `GuiGraphicsExtractor.text` vs Modern UI 的 `ARGB.color`),用 `colorfulsubtitles$isModernUiLoaded()`(惰性缓存 `isModLoaded("modernui")`)分流。新增第三方兼容时沿用此模式:惰性检测 + 注入方法内早退,不要写死 class-load 探测。
- 文字着色公式:`ARGB.scaleRGB(自定义色, 原颜色红通道/255)`,目的是保留原版淡入淡出(原版把 alpha 编码进颜色通道)。改渲染逻辑时不得破坏淡出效果。

### 4.3 GUI 层
- 配置界面用 Cloth Config builder 构建,编辑中间态用单元素数组(`int[]`、`boolean[]`)配合 `setSaveConsumer`,统一在 `setSavingRunnable` 里组装新 config 并调用 `ColorfulSubtitles.setConfig`(该方法负责落盘)。
- `PaletteColorEntry` 继承 Cloth 的 `ColorEntry`,自绘部分只用 `GuiGraphicsExtractor` 的 `fill`/`fillGradient`/`outline`,不引入自定义 shader/纹理。

## 5. 编码规范

- **缩进用 tab**,UTF-8(`build.gradle` 已强制 `options.encoding = "UTF-8"`),JSON 资源文件同样 tab 缩进。
- 类内成员顺序:常量 → 静态字段 → 实例字段 → 构造器 → 实例方法 → 静态方法。工具类写 `private` 构造器。
- 命名:Mojang 官方映射命名;包名全小写;不用缩写(`background` 不写 `bg`,局部变量除外)。
- **Mixin 规范**:
  - 所有 `@Unique` 成员(字段和方法)加 `colorfulsubtitles$` 前缀(1.9.1 起全项目已统一)。
  - mixin 类只放注入逻辑与状态存取,业务决策放普通类。
  - 客户端 mixin 加 `@Environment(EnvType.CLIENT)` 并注册在 mixin.json 的 `client` 列表。
  - `defaultRequire = 1` 已开启:注入失败会硬崩,这是有意为之,**不要调低**——宁可启动崩溃也不要静默失效。
- **日志**:统一用 `ColorfulSubtitles.LOGGER`;配置类故障用 `warn` + 自愈,不抛出;渲染热路径(每帧执行的注入方法)内禁止打日志、禁止分配对象之外的重活。
- **空值防御**:所有依赖注入顺序的 mixin 状态字段(如 `colorfulsubtitles$iterationEntry`)在使用前必须判空回退到原值。
- **i18n**:新增任何用户可见文本必须同时更新 `en_us.json` 与 `zh_cn.json`,键名格式 `<类型>.colorfulsubtitles.<路径>`(`title.`/`category.`/`option.`/`tooltip.`)。两文件键集合必须完全一致。

## 6. 构建与依赖注意事项

- 构建命令:`.\gradlew.bat build`;产物在 `build/libs/colorfulsubtitles-<version>.jar`;开发运行:`.\gradlew.bat runClient`(工作目录 `run/`)。
- `build.gradle` 当前用 `implementation` 声明 fabric-api / cloth-config / modmenu 且未显式声明 `mappings`——**这是非标准 Loom 写法但当前可用**。如出现 runClient 缺 mod 或 remap 报错,首先尝试改为 `modImplementation`;若改动,须实测 build 与 runClient 后更新本节。
- cloth-config 依赖里 `exclude(group: "net.fabricmc.fabric-api")` 是有意的(避免版本冲突),不要移除。
- 新增第三方 maven 仓库写在 `build.gradle` 的 `repositories`,并注明用途注释。

## 7. 验证流程(提交前必做)

1. `.\gradlew.bat build` 通过;
2. 涉及渲染 / mixin / MC 版本升级的改动:`runClient` 进游戏,开启字幕(`字幕显示`),逐项确认:
   - 各分类文字颜色正确、音量淡出时颜色渐隐正常;
   - 背景色三态(全局覆盖 / 分类覆盖 / 无覆盖走原版)各测一例,含半透明 alpha;
   - 装 Modern UI 再测一遍文字颜色路径;
3. 涉及配置格式的改动:删除 `run/config/colorfulsubtitles.json` 测首启生成;再分别测空文件、缺键文件、损坏 JSON 的自愈;确认旧版本配置文件仍可加载;
4. 涉及 GUI 的改动:Mod Menu 进入配置界面,测调色板拖拽、alpha 条、重置按钮、保存后 JSON 内容正确。

## 8. 版本发布与文档同步

- 版本号:`gradle.properties` → `mod_version`,语义化(修 bug 升 patch,新功能/适配新 MC 升 minor)。
- **每次改版必须同步**:`CHANGELOG.md` 增加对应版本条目(中文,沿用现有 `### 适配 / 新增 / 修复` 分节格式);README 的 Requirements 与版本相关描述。CHANGELOG 缺失的历史版本(1.8.1、1.9.0)应在下次发版时补记。
- README 中 "The subtitle rendering mixin targets Minecraft <版本> internals" 的版本号随 MC 升级更新。

## 9. Git 约定

- 分支:`master` 为主分支。提交信息沿用现有风格:一句话描述做了什么(中文或英文均可),多个改动用多行列出。
- **禁止提交**:`build/`、`run/`、`.gradle/`、`codex-backups/`、`.claude/`(均已在 `.gitignore`)。`codex-backups/` 与 `build/recovery-*/` 是历史手工备份,仅本地保留,**不要在新代码中引用或"恢复"它们**——`src/` 是唯一事实来源。
- 提交前 `git status` 确认无意外文件;二进制产物(jar)不入库。

## 10. 已知欠账(按优先级)

(1.9.1 已清空此前全部欠账;1.9.2 修复默认文字色 GUI 不可编辑后暂无遗留。)

发现新欠账时按优先级追加;修复后从本清单删除对应条目。
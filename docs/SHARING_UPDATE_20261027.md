# 2026-10-27 日记与最近统计分享增量

起始 HEAD：7b7de395a9fc69a598cb14a6d2fb2b192c4b0385。

用户本轮明确要求：今日增加已保存日记分享；日记分享直接包含特殊情况，空值显示“无记录”，无需额外确认；历史的最近 7/14 天增加统计图片分享，按日期列出非空特殊情况；取消周期总结自动展示；核查滚动统计使用真实最近数据。该要求取代旧版 comment 默认排除/预览确认和自动周总结展示的对应交互，其他功能不变。

核查发现：RecentAverageCalculator 按含截止日在内的 7/14 个日历日筛选有效原始记录，缺失日期不计零；但 Public 历史页使用 form.referenceDate，编辑旧日记后可能偏移统计窗口。需将统计截止日与编辑表单日期分离，分享时重新读取数据库原始记录。

实现：今日与保存成功页使用同一已持久化日记回读/PNG/系统分享流程，取消 comment 预览与 opt-in。历史的选中 7/14 天窗口提供 PNG 分享，使用当前本地日期和数据库最新原始记录，非空特殊情况按 sleepDate 原文列出；无效历史行不进入统计，其窗口内备注仍保留。每次 PNG 使用独立临时文件，保留 content URI 与临时读取授权。

统计核查：现有 RecentAverageCalculator 的六项均值按有效原始记录计算，包含当日与前 6/13 日，窗口外及未来记录不参与，缺失不计零。独立 referenceDate 修复旧日记编辑引起的日期偏移，进入历史会重新读库；保存后的 today 也按实际本地日期定位。周期周总结数据和原始数据保留，保存成功不再触发周总结页或标记其已展示。

验证状态：最终生产代码全量 testDebugUnitTest：480 项、51 类，0 failures / errors / skipped；assembleDebug 和 assembleDebugAndroidTest 通过。全量报告保留在 work/sharing-full-tests-20261027/。随后加入长图测试，仅重新运行 ShareRegressionTest：6 项通过（包含该新增测试），报告保留在 work/sharing-render-tests-20261027/。主应用代码在全量运行后未改变。

目视验证：使用合成日记生成 work/sharing-preview-20261027/daily.png、recent-7.png、recent-14.png；已查看单日和 14 天 PNG，日期、特殊情况原文、多行文本及页尾均完整，没有裁切。真实 Android FileProvider 接收测试已更新并编译进测试 APK，尚未执行真机 instrumentation 或用户验收。

静态检查：git diff --check 通过；合并 Manifest 无 INTERNET / ACCESS_NETWORK_STATE / MANAGE_EXTERNAL_STORAGE；Room schema、版本与依赖未改。原有 meditation/ 为无关未跟踪内容，未触碰。

过程记录：首次受限 Gradle 因目录访问失败，改用批准后的本机执行；初次测试编译缺少 Compose 状态委托 import，补齐后上述测试通过。自动化结果均来自本轮实际执行。

文档同步：用户已明确回复“批准同步这些相关条款”。已仅同步 PRODUCT_SPEC.md、UX_SPEC.md、IMPORT_EXPORT_SPEC.md、TEST_PLAN.md 中本次三项需求的条款，以及 PUBLIC_PRODUCT_SPEC.md 的相应 Public UX 条目。SPEC_MANIFEST.json 仅刷新这五份文件的哈希/大小；其他历史清单条目未改，不宣称全仓库旧规格均已重新核对。

本轮 APK：app/build/outputs/apk/debug/app-debug.apk，11,724,842 字节，SHA-256 FF427CB97EEF15CF94EEDE182C6D87AC07301317133F4F1BCCD8D7366B5C64E4。App 0.2.2 / versionCode 4，Room 7 / BACKUP_3.1 / CBT-I NONE 均不变。

下一步：用户在 S24 验证今日、保存成功页、历史 7/14 天的分享接收与特殊情况长图。未创建 Git commit，当前代码与本报告保留在工作树中。完整治疗、教育内容授权与其他旧版未验收项不属于本轮完成范围。

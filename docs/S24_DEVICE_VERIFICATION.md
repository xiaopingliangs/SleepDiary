# Samsung S24 真机验证（2026-09-10）

设备：SM-S9210，Android 16。已安装 App APK SHA-256 与本地 APK 一致：a330c88b613a6be76a80d87f582f256984223a11e198f7fe977c59930e943e6a。

本轮未修改生产代码、医学规则、冻结规格或任何版本。新增 DataReliabilityDeviceTest，所有测试写入唯一命名的独立数据库，测试后仅删除各自测试数据库；没有清空或卸载用户 App。

## 实际系统文件流程

- 在手机 App 设置中，通过 Android Storage Access Framework 创建 JSON 文件，收到“完整备份已导出”。
- 文件为 BACKUP_1.1，包含原有 1 条日记（有非空 comment）、1 份安全问询、0 份周总结及 trackingState。
- 通过相同真实文件选择器成功创建 CSV，解析后 comment 与 JSON 完全一致。
- 通过系统文件选择器重新选择刚导出的 JSON，摘要正确；执行原样替换恢复，收到“完整备份已恢复”。
- 恢复后再次导出 JSON，排除 exportedAtUtc 后，全部字段与恢复前 JSON 完全一致。
- 在 Download 中保留用户数据备份文件：SleepDiary_backup_20260910_221551.json、SleepDiary_backup_20260910_222139.json、SleepDiary_entries_20260910_221732.csv。

## 真机隔离测试

DataReliabilityDeviceTest 5 项全部通过：

1. 连续保存 30 天原始记录，第 7/14/21/28 天分别生成第 1/2/3/4 周总结；每份 7 条，均值与固定输入一致。第 30 天不生成月报。关闭并重新打开磁盘数据库后全部数据一致。
2. 第 7 天漏记后，第 8 天生成 6 条记录的第 1 周总结；延迟记录正确显示此前有记录的第 2 周；空第 3/4 周不强制生成，随后不重复提示。
3. 包含 30 条日记、2 份 Safety 历史、5 份周总结（含 revision）及 AppState 的完整备份往返一致；BACKUP_1.0 兼容恢复成功且 onboarding=false；两版本在 afterDelete/afterEntries/afterState 注入失败均完整回滚；损坏 comment 均拒绝恢复且不改数据。
4. 备注编辑、清空为 null/空串后回读正确、createdAtUtc 保持；分享白名单排除备注和 Safety。
5. 真机 CSV 的 Unicode、引号、逗号、换行转义正确。

ShareDeviceTest 1 项通过：真机生成 PNG，FileProvider content URI 可读，PNG 文件头和临时读权限正确。测试未向第三方 App 实际发送内容，也不能据此声称完整 Sharesheet hand-off 已验收。

## 未通过项与限制

CommentImeDeviceTest 1 项失败：第 41 行查找 onCheckIsTextEditor View 返回 null；等待输入连接 10 秒的尝试仍失败，已撤回该尝试并保留原测试。失败发生在测试找到输入连接前，不能单凭此结果断言产品不能输入，也不能声称中文 IME 已验收。

最终 instrumentation：7 项运行，6 项通过，1 项失败。原始日志在 work/s24-device-tests.log。自动化组合输入与实际中文键盘输入仍需进一步诊断。

testDebugUnitTest 和 assembleDebug 成功（既有 230 项单元测试缓存结果有效，本轮未改生产或单元测试代码）。测试包构建成功。日志 work/s24-final-build.log。

冻结 PRODUCT_SPEC 第 7 节只定义 7 天周期，没有 30 天/月报功能。第 30 天属于第 5 周；不能将“没有月报”报告为计算失败。分享 PNG 按冻结规则不含自由备注和 Safety；本轮没有更改这一隐私边界。

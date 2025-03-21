# z-bill-analyzer

该项目是账单分析工具，可以将账单CSV文件导入，进行可视化分析。本项目不会存储任何数据，自己的数据页不会被他人看到，可以放心使用。目前支持的微信、支付宝、招商银行。

演示地址：http://47.119.137.19:8080/

CSV文件获取攻略：

- 微信：我-服务-钱包-账单-常见问题-下载账单-用于个人对账。一次性只能导出3个月的数据。
- 支付宝：我的-账单-右上角三个点-开具交易流水证明-用于个人对账。一次性能导出1年的数据。
- 招商银行：招商银行个人银行PC版-首页-交易查询-打印（最多13个月）

使用方式：

导出的CSV文件，不要做任何改动，包括文件名、内容等，否则会解析失败。

技术栈：

- 后端：SpringBoot、EasyExcel
- 前端：HTML、JS、ECharts

功能列表

- 导入多个CSV文件进行分析。
- 账单分析记录看到历史导入数据。
- 数据看板
- 图表分析：饼状图、折线图
- 账单明细分页列表、账单明细详情

功能演示

![](https://raw.githubusercontent.com/zLatiao/z-bill-analyzer/refs/heads/main/img/index.jpeg)

![](https://raw.githubusercontent.com/zLatiao/z-bill-analyzer/refs/heads/main/img/visualization.jpeg)

TODO

1. 加解析记录实体，与用户关联，增加定时清理账单的任务
2. 招行理财、朝朝宝、退款这些鬼东西怎么合并
3. 可以改成VUE运行后打包到SpringBoot
4. 导入多个账单，可以选择展示哪些账单
5. 支出分类占比点击某个分类可以展示筛选这个分类的账单列表
6. 点击图表能弹出对应的账单分页列表

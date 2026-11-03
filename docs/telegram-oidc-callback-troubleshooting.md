# Telegram OIDC Callback 403 排查记录

日期：2026-11-02
站点：`https://jobpost.duckdns.org`
回调路径：`/api/auth/telegram/callback/telegram`

## 现象

用户完成 Telegram 登录后，浏览器显示回调 URL 并返回 HTTP 403。用户曾在对话中粘贴带有 `code` 和 `state` 的完整授权回调 URL。该授权码是短时、一次性凭据；不应重放、保存到排查文档或再次分享。

## 已核实的实现

- 回调由 Spring Security OAuth2 Login filter 处理，不是前端页面或单独的 Controller。
- 安全规则允许匿名访问 `/api/auth/telegram/callback/**`。回调是 GET；项目的 CSRF 保护针对状态变更请求，不应通过关闭 CSRF 来排查此问题。
- 成功处理会保存/更新 Telegram 用户、设置 HttpOnly 会话 cookie，再跳转到配置的前端 URL。
- OIDC `state` 暂存在发起登录时的 HTTP session 中。登录和回调必须在同一浏览器、同一站点 host 的流程内完成。
- 发布版 `v0.1.0` 没有自定义 OAuth 失败处理器；Spring 默认失败跳转是 `/login?error`。本地工作区已增加失败时回到首页显示通用提示的实现，但它尚未提交或部署。

## 只读线上探测

探测未使用、重放或记录用户提供的授权码/state。

| 请求 | 观察结果 | 可得结论 |
|---|---|---|
| `GET /` | Nginx 返回 200 | HTTPS 站点和静态首页可访问 |
| `GET /api/auth/status` | Nginx 返回 200；认证启用，当前探测请求未认证 | `/api` 路由到后端，Telegram OIDC 配置开关为启用；不证明凭据正确或具体应用版本 |
| `GET /api/auth/telegram/callback/telegram`（无 query） | Nginx 返回 302，`Location: https://jobpost.duckdns.org/login?error`；响应设置 `Secure; HttpOnly; SameSite=Lax` 的 `JSESSIONID` | 回调路径到达 Spring OAuth2 处理链；空回调缺少已保存的授权请求，因此进入默认失败处理 |
| `GET /api/auth/telegram/callback/telegram?error=access_denied&state=diagnostic-only` | 返回 302 到 `/login?error` | 合成的拒绝响应也由 Spring 处理；没有用真实授权参数 |
| `GET /login?error` | Nginx 返回 200 静态 HTML | 当前 Nginx 对该路径回退到前端静态页 |

以上不能复现用户报告的真实 403。状态接口、无参数回调和合成拒绝响应都没有返回 403；也无法据此确认 Telegram 实际授权码交换是否成功。

## 判断与尚未确认项

现在没有足够证据把真实 403 归因于某一个组件。线上回调路由本身可到达 Spring，因此单纯的 `/api` 代理路径错误不是首要嫌疑。需要将真实请求的响应状态与时间戳和服务器端日志关联：

1. **OIDC session/state 不匹配**：登录流程若在 localhost、不同子域、不同浏览器/隐私窗口开始，或浏览器没有在顶层 HTTPS GET 回调时带回发起登录的 `JSESSIONID`，Spring 无法关联 `state`。确认从生产首页开始并在同一浏览器完成流程。
2. **入口层拒绝**：若浏览器看到的回调直接是 403，且同一时间 Spring 应用没有对应请求记录，检查 Nginx、主机防火墙或其他前置代理/WAF 的访问/错误日志和规则。
3. **应用层拒绝或异常**：若应用收到了请求，查看 Spring 日志中同一时间附近的 OAuth2 authentication failure / exception 类型；不要启用会记录 query string、授权码、token 或完整 headers 的调试日志。
4. **Telegram client/callback 配置不匹配**：只比对 Telegram 控制台中的已登记回调和运行时 `TELEGRAM_REDIRECT_URI` 是否都精确为 `https://jobpost.duckdns.org/api/auth/telegram/callback/telegram`；检查时不要输出 client secret。
5. **服务器实际版本**：公开状态接口不能识别 JAR 版本。需由服务器管理员通过部署记录或 release `COMMIT` 文件确认当前运行版本，不能仅凭首页可访问推断版本。

## 后续日志确认的根因

2026-11-03 用户提供的应用日志显示，OIDC 成功处理器已被调用，随后 `TelegramUserService.upsertFromOidc` 在 `Long.valueOf(oidcUser.getSubject())` 处抛出 `NumberFormatException`；异常输入是纯数字字符串，但超过 Java 有符号 Long 的上限。日志中含外部身份标识，本文不复述其原值。

这说明该次请求已到达 Spring 应用并进入认证成功处理流程，问题不是 Telegram 回调路径少/多一段，也不是这段日志所指向的 Nginx 403。异常发生在本地用户 upsert 和 Zipin Cookie 签发之前，因此登录没有完成。应用返回状态可能再被代理/错误页映射，必须以浏览器状态与代理日志分辨最终 HTTP 响应。

代码修复将 OIDC `sub` 按原始字符串保存；V3 将数据库 `telegram_id` 从 BIGINT 改为 ASCII、大小写敏感的 `VARCHAR(255)`，保留已有十进制 ID 的文本形式。生产 schema 未在此处核对，V3 尚不能直接应用到生产。

## 本地修复尝试

本地未提交改动增加 `TelegramAuthFailureHandler`：OAuth 登录失败或用户取消时，以固定 `auth=telegram-failed` 标记重定向到已配置的前端首页；不将 Telegram 的 `error_description` 传给浏览器；首页显示通用提示并移除地址栏中的标记。另已按上方根因开始将 `sub` 的 Java/SQL 类型改为字符串，并增加 V3 migration。

此改动改善默认 `/login?error` 失败体验，但**不能单独修复**由状态 cookie 缺失、入口层/WAF 拒绝、错误 client 配置或数据库/OIDC 交换异常造成的根因。生产服务器目前不会因为这项本地改动自动更新。

验证结果：

- `TelegramAuthFailureHandlerTest`：1 项通过。
- `TelegramOidcAuthorizationTest`：3 项通过，包括 provider denial 的安全重定向。
- `run-local.ps1 -TestDatabase` 定向测试：13 项通过，0 失败。
- 前端 `npm run build`：通过。

## 安全复测步骤

1. 使用 `https://jobpost.duckdns.org/` 打开一个浏览器窗口，从页面点击 Telegram 登录；不要从 localhost 开始后回到生产域名。
2. 完成 Telegram 授权一次；不要手工打开 callback URL、复制 `code` 或重试旧 callback。
3. 若仍失败，记下本地时间、浏览器最终 HTTP 状态、响应的 `Server`/`Location` 头，以及是否存在 `Set-Cookie`（只记录“存在/不存在”，不复制 cookie 值）。
4. 服务器管理员用同一时间范围对照 Nginx 和 Spring 应用日志。日志必须先删除 query string、cookie、authorization header、token 和个人资料字段。
5. 根据请求是否到达 Spring 再分层处理：未到达应用则排查入口层；已到达则按 Spring OAuth failure 类型检查 session/state、客户端配置和 Telegram token 响应。保持 CSRF 开启。
6. 修复后发布新版本并部署，再进行一次真实账号端到端验收；不要把“状态接口返回 enabled”当作登录验收。

## 部署边界

本文只记录代码和公开 HTTPS GET 探测，没有读取本机 `local.env`，没有重放用户的授权参数，没有访问或修改 AWS 资源，也没有更改生产服务器、Nginx、数据库或 Telegram 控制台。后续线上日志检查和部署需由有服务器权限的操作方执行并授权。

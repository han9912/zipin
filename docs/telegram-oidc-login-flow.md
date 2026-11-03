# Telegram 网页登录调用流程

本文按代码实现说明 Zipin 的 Telegram OIDC 授权码登录。Telegram Mini App / `initData` 登录不在此流程中。

## 参与方

- **浏览器**：用户访问 Zipin 页面，并在授权完成后携带浏览器 Cookie 返回。
- **Zipin 前端**：显示登录入口；回到首页后请求登录状态。
- **Zipin 后端**：Spring Security OAuth2 Client，负责发起授权、接收回调、验证身份、保存本地用户并签发应用会话。
- **Telegram OIDC**：展示登录/授权页面，并提供授权码、token 和签名密钥。
- **MySQL**：保存 Telegram 用户与招聘信息。

## 时序图

```mermaid
sequenceDiagram
    autonumber
    actor User as 用户
    participant Browser as 浏览器
    participant Frontend as Zipin 前端
    participant Backend as Zipin 后端 / Spring Security
    participant Telegram as Telegram OIDC
    participant DB as MySQL

    User->>Frontend: 点击“Telegram 登录”
    Frontend->>Browser: 顶层跳转 GET /api/auth/telegram/login/telegram
    Browser->>Backend: GET /api/auth/telegram/login/telegram
    Backend->>Backend: 生成 state、nonce、PKCE verifier/challenge
    Backend->>Backend: 保存授权请求到 HTTP session
    Backend-->>Browser: 302 Location: https://oauth.telegram.org/auth?...&redirect_uri=...
    Browser->>Telegram: 跳转到 Telegram 登录/授权页
    User->>Telegram: 登录并点击允许/授权
    Telegram-->>Browser: 302 到 Zipin callback，携带一次性 code 和 state
    Browser->>Backend: 顶层 GET /api/auth/telegram/callback/telegram?code=...&state=...
    Note over Browser,Backend: 浏览器应带回发起登录时的 JSESSIONID
    Backend->>Backend: OAuth2 登录过滤器读取 session 中的授权请求并比对 state
    Backend->>Telegram: 后端服务器请求 token，提交 code、client 身份与 PKCE verifier
    Telegram-->>Backend: 返回 token（包括 ID Token）
    Backend->>Backend: 校验 ID Token 签名、issuer、audience、expiry、nonce
    Backend->>DB: 按 Telegram sub upsert 用户并读回本地用户
    DB-->>Backend: 返回本地用户记录
    Backend->>Backend: 签发 12 小时应用 JWT 会话
    Backend-->>Browser: Set-Cookie: zipin_session=...; HttpOnly; Secure; SameSite=Lax；302 到前端首页
    Browser->>Frontend: 跟随 302，GET /
    Frontend->>Backend: GET /api/auth/status（自动携带 zipin_session）
    Backend->>Backend: JWT 过滤器验签、检查有效期并读取本地用户
    Backend->>DB: 按本地 user id 查询用户
    DB-->>Backend: 返回用户资料
    Backend-->>Frontend: authenticated=true 和用户资料
    Frontend-->>User: 显示已登录状态
```

## 详细步骤

### 1. 从 Zipin 发起登录

前端登录链接指向：

```text
/api/auth/telegram/login/telegram
```

这是浏览器顶层页面跳转，不是前端用 `fetch` 直接调用 Telegram。后端的 Spring Security 根据 Telegram OIDC 配置构造授权请求，生成并保存授权请求状态，再以 HTTP 302 将浏览器送往 Telegram。

授权请求包含授权码流程所需参数，例如 `client_id`、scope、回调地址，以及：

- `state`：将回调与本次登录请求关联，阻止请求伪造/串线。
- `nonce`：将 ID Token 绑定到这次认证请求。
- PKCE `code_challenge`：后续证明兑换授权码的一方持有对应 verifier。

授权请求保存在后端 HTTP session 中。因此浏览器会有一个 `JSESSIONID` Cookie；后续回调需要回到同一个 host，并带回对应 session Cookie。生产应从 `https://jobpost.duckdns.org/` 发起登录，不能从 localhost 发起后再回到生产域名。

### 2. 用户在 Telegram 登录并许可

用户在 `oauth.telegram.org` 完成 Telegram 侧的身份验证并点击允许后，Telegram 不会把用户的密码交给 Zipin。Telegram 会把浏览器重定向到登记的回调 URI：

```text
https://jobpost.duckdns.org/api/auth/telegram/callback/telegram
```

成功授权时，Telegram 通过回调 URL 的查询参数返回：

- `code`：短时、一次性授权码，不是应用登录 Cookie，也不是可长期使用的 JWT。
- `state`：必须与后端 session 保存的本次登录请求相匹配。

浏览器将这次顶层 HTTPS GET 请求送到 Zipin。Nginx 应把 `/api/` 原样反向代理到 Spring Boot。回调路径由 Spring Security OAuth2 登录过滤器处理，不是前端页面，也不要求用户预先登录 Zipin。

**不要复制、转发或重放带 `code` / `state` 的完整回调 URL。** URL 查询参数也不应写入应用访问日志、代理日志、分析系统或工单。

### 3. 后端校验并兑换授权码

Spring Security 通过浏览器带回的 `JSESSIONID` 找回发起登录时保存的授权请求，并校验 `state`。缺少 session、state 不匹配或回调不是本次登录请求的一部分时，认证不能继续。

通过关联校验后，Zipin 后端服务器直接向 Telegram token endpoint 发请求，提交授权码、客户端认证信息和 PKCE verifier。**客户端 Secret 和 PKCE verifier 不经过浏览器，不应出现在前端代码或 URL 中。**

Telegram 返回 token 后，后端使用 Telegram 的签名密钥验证 ID Token，并检查 issuer、audience、有效期和 nonce。只有验证通过后才把其身份声明当作可信身份。浏览器回调中的 `code` 本身不能证明用户身份。

### 4. 建立 Zipin 本地身份与会话

当前 OIDC 用户处理逻辑使用验证后的 ID Token 声明。服务端将 `sub` 作为不透明字符串原样保存，不假设它能解析为 Java 数值；读取 `preferred_username`、`name`、`picture` 等字段，并在 `telegram_users` 表 upsert 后读回本地用户。`telegram_users.id` 仍是 Zipin 内部的 Long 主键，和 OIDC `sub` 不同。

登录成功处理器随后：

1. 使用本地用户 ID 签发 Zipin HS256 JWT，时长为 12 小时。
2. 通过 `Set-Cookie` 设置 `zipin_session`，属性为 `HttpOnly`、`SameSite=Lax`、`Path=/`；生产配置启用 `Secure`。
3. 使 OAuth 登录使用的服务器端 HTTP session 失效。
4. 返回 HTTP 302，将浏览器送到配置的前端首页 `APP_FRONTEND_URL`。

这意味着 Telegram 负责验证 Telegram 账号；之后每次 Zipin API 请求使用的是 Zipin 自己签发的 Cookie 会话，不会每次都跳到 Telegram。

### 5. 首页识别登录状态

浏览器跟随 302 回到首页。前端启动时调用 `GET /api/auth/status`，浏览器按同源 Cookie 规则自动附带 `zipin_session`。后端 JWT 过滤器验证签名、issuer 和有效期，再按 JWT 中的本地用户 ID 从数据库读取用户并建立 Spring Security principal。状态 API 据此返回 `authenticated=true` 和用户资料，前端显示登录态。

后续发布操作使用这个 Cookie 会话，并携带 CSRF token；发布者 ID 从服务端认证 principal 得出，不信任客户端传入的身份字段。

## 失败时会发生什么

常见失败点包括：

- Telegram 用户取消授权或返回 `error`。
- 回调 host/路径与登记地址不一致。
- 浏览器没有带回发起流程时的 `JSESSIONID`，或 `state` 不匹配。
- 授权码过期、已使用，或者客户端凭据 / PKCE 校验失败。
- ID Token 的签名、issuer、audience、expiry、nonce 校验失败。
- 数据库 upsert/读回用户失败。

失败发生在 Telegram 授权、code 兑换、ID Token 校验和本地用户持久化等不同阶段时，HTTP 响应可能不同；不能仅凭一个错误页面判断是哪一阶段。

当前工作区尚有本地未提交的失败处理器：失败时跳回前端并附加固定的 `auth=telegram-failed` 标记，前端显示通用提示，不暴露 provider 错误详情。**已发布 `v0.1.0` 没有这个自定义失败处理器**，失败时可能由 Spring 默认跳转到 `/login?error`。本地修复并不意味着生产已更新。

## 回调参数与配置核对

生产必须逐字一致：

```text
Telegram 后台登记的 callback
= TELEGRAM_REDIRECT_URI
= https://jobpost.duckdns.org/api/auth/telegram/callback/telegram
```

登录最终回跳地址：

```text
APP_FRONTEND_URL=https://jobpost.duckdns.org/
```

生产同时要求：

- `TELEGRAM_OIDC_ENABLED=true`
- `AUTH_COOKIE_SECURE=true`
- HTTPS 站点和代理将 `/api/` 转发给后端
- 使用同一浏览器、同一站点 host，从 Zipin 首页开始整个授权流程
- 数据库中已部署所需 schema；生产迁移需先核对 schema、备份与回滚方案，不能仅凭应用启动成功推断数据表准备完成
- V3 将 `telegram_users.telegram_id` 从 `BIGINT` 改为区分大小写的 ASCII `VARCHAR(255)`；生产 profile 禁止自动迁移，必须在确认生产 schema、备份和回滚方案后单独审查/应用

生产环境变量样例见 [`production.env.example`](../production.env.example)。不得将实际值写入仓库、Release 包或排查文档。

## 当前验证范围

- 本地测试覆盖了授权跳转包含 `state`、`nonce`、PKCE S256、有效 Zipin Cookie 认证，以及用户拒绝时的本地安全失败跳转。
- 本地测试不是与 Telegram 的真实账号端到端测试。
- 生产状态接口返回 OIDC 已启用只表示配置开关/字段满足启用条件，不证明 client secret 有效、浏览器 session 连续、回调交换成功或当前部署版本包含本地未提交变更。

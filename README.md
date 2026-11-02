# Zipin Backend

Spring Boot 3.4, Java 17, MyBatis, and MySQL. The repository also contains legacy modules that use Redis and MinIO.

## Run the local opportunity MVP

Requirements: JDK 17+ and a running local MySQL 8 instance.

1. Create a dedicated database using MySQL Workbench or another local MySQL client:

   ```sql
   CREATE DATABASE zipin_mvp CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
   ```

2. Copy `local.env.example` to `local.env` and set credentials for a local MySQL user that can create tables in `zipin_mvp`. `local.env` is git-ignored; never commit it or share its password.

3. Start the backend from this directory:

   ```powershell
   Copy-Item local.env.example local.env
   # Edit local.env with your local MySQL credentials.
   .\run-local.ps1
   ```

   The runner selects the installed JDK and starts the isolated opportunity MVP entry point with the local `dev` profile. It does not start legacy Redis-dependent modules. Flyway applies the opportunity and Telegram account migrations.

4. To enable Telegram OIDC locally, create/configure the Telegram login client and register `http://localhost:5173/api/auth/telegram/callback/telegram` as its callback URL. Set `TELEGRAM_OIDC_ENABLED=true`, the client ID and secret, and callback URL in `local.env`. Set `ZIPIN_JWT_SECRET` to a Base64-encoded random key of at least 32 bytes (for example, generate 32 random bytes and Base64-encode them). Keep `local.env` private.

5. Start the frontend in another PowerShell window:

   ```powershell
   Set-Location frontend
   npm install
   npm run dev
   ```

   Open the Vite URL printed in the terminal.    The Vite server proxies `/api` to `http://localhost:8080`, including the OIDC callback.

The MVP stores four fields: `category`, `title`, `content`, and `contact`. Creating a listing writes it to MySQL, reads the saved row back, and refreshes the home-page list from the API.

## API

- `GET /api/opportunities` — latest 100 listings. Optionally pass `keyword` to search category, title, content, and contact; search matches the database before the 100-result limit. Public.
- `POST /api/opportunities` — create a listing. Request JSON fields: `category`, `title`, `content`, `contact`. Requires Telegram login and records the publisher.
- `GET /api/auth/status` — reports whether Telegram OIDC is configured and the current login profile.
- `GET /api/auth/csrf` — returns the CSRF token required for state-changing requests.
- `POST /api/auth/logout` — clears the HttpOnly login cookie.

OIDC authorization uses the authorization-code flow with PKCE. Spring Security validates the ID-token signature, issuer, audience, expiry and nonce. The application stores Telegram's stable numeric subject and basic profile fields; it does not automatically link legacy email accounts. The session is a 12-hour signed JWT in an HttpOnly, SameSite=Lax cookie. CSRF protection is enabled for POST requests. Logout clears the browser cookie; already-copied tokens remain valid until expiry.

For production, copy `production.env.example` into a protected runtime configuration (never commit populated values), set the RDS connection after verifying its schema and TLS requirements, and provide Telegram and JWT secrets through a managed secret store. The `prod` profile requires Telegram authentication, secure cookies, and HTTPS callback/frontend URLs. It binds the application to loopback, exposes only the health endpoint on loopback port 8081, and disables automatic Flyway migrations so deployment cannot silently change a production schema. Review and apply migrations separately after confirming the actual schema, data retention, and backup/rollback requirements.

The GitHub Actions workflow tests the isolated MVP against MySQL and builds the frontend. Push a version tag such as `v0.1.0` to create a GitHub Release containing `zipin-v0.1.0.tar.gz` and its SHA-256 checksum. The bundle contains only the executable backend JAR, frontend static files, and source commit ID; it contains no production configuration or secrets. If the repository is public, a server can download and verify the bundle with:

```sh
VERSION=v0.1.0
curl -fL -o "zipin-${VERSION}.tar.gz" \
  "https://github.com/han9912/zipin/releases/download/${VERSION}/zipin-${VERSION}.tar.gz"
curl -fL -o "zipin-${VERSION}.tar.gz.sha256" \
  "https://github.com/han9912/zipin/releases/download/${VERSION}/zipin-${VERSION}.tar.gz.sha256"
sha256sum -c "zipin-${VERSION}.tar.gz.sha256"
```

For a private repository, authenticate to GitHub before downloading the release. This workflow publishes artifacts only; it does not connect to or deploy to AWS. The OIDC state is held in the login-flow HTTP session; for multiple app instances, use load-balancer stickiness or a shared session store. Do not deploy the local dev server publicly.

The V2 Flyway migration adds a Telegram user table and nullable publisher reference while preserving old anonymous listings. Before applying it to AWS, inspect the actual production schema and data and confirm backup/rollback requirements. This code change does not authorize or perform a production migration or deployment.

## Verify

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd -Dtest=OpportunityServiceTest test
```

The focused unit tests do not require a database. Redis and MinIO are only needed by the legacy modules, not this opportunity MVP.

To test the HTTP create-then-read flow against the configured local MySQL database, run:

```powershell
.\run-local.ps1 -TestDatabase
```

The integration test uses MockMvc and rolls back its database insert.

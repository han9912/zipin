# Zipin Frontend

Vue 3 + TypeScript + Vite frontend for the Zipin opportunity board.

## Run locally

Start the backend and local MySQL first; see [backend setup](../README.md).

```powershell
npm install
npm run dev
```

## Validate and build

```powershell
npm run build
```

The Vite development server proxies `/api` requests to `http://localhost:8080`, including Telegram's OIDC callback. Configure the Telegram client and callback in the backend's ignored `local.env`. The home page reads and searches records; creating a listing requires Telegram login. Keep the unauthenticated development setup on a trusted local network and do not expose it publicly.

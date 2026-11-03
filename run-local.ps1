param(
    [switch]$TestDatabase
)

$ErrorActionPreference = 'Stop'

$envFile = Join-Path $PSScriptRoot 'local.env'
if (-not (Test-Path -LiteralPath $envFile)) {
    throw "Create local.env from local.env.example and configure local MySQL credentials first."
}

$requiredKeys = @('DB_URL', 'DB_USERNAME', 'DB_PASSWORD')
$allowedKeys = $requiredKeys + @(
    'TELEGRAM_OIDC_ENABLED',
    'TELEGRAM_CLIENT_ID',
    'TELEGRAM_CLIENT_SECRET',
    'TELEGRAM_REDIRECT_URI',
    'APP_FRONTEND_URL',
    'ZIPIN_JWT_SECRET',
    'AUTH_COOKIE_SECURE'
)
$configuredKeys = @{}
foreach ($line in Get-Content -LiteralPath $envFile) {
    $trimmedLine = $line.Trim()
    if (-not $trimmedLine -or $trimmedLine.StartsWith('#')) {
        continue
    }

    $parts = $trimmedLine -split '=', 2
    if ($parts.Count -ne 2 -or $parts[0] -notin $allowedKeys) {
        throw 'Invalid local.env entry. Only DB_URL, DB_USERNAME, and DB_PASSWORD are supported.'
    }

    $key = $parts[0].Trim()
    $value = $parts[1].Trim()
    if (-not $value -and $key -in $requiredKeys) {
        throw "$key must not be empty."
    }
    $configuredKeys[$key] = $value
}

foreach ($key in $requiredKeys) {
    if (-not $configuredKeys.ContainsKey($key)) {
        throw "Missing $key in local.env."
    }
}

foreach ($key in $allowedKeys) {
    if ($configuredKeys.ContainsKey($key)) {
        Set-Item -Path "Env:$key" -Value $configuredKeys[$key]
    } elseif ($key -notin $requiredKeys) {
        Remove-Item -Path "Env:$key" -ErrorAction SilentlyContinue
    }
}

if (-not $env:JAVA_HOME -or -not (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
    $jdkRoot = 'C:\Program Files\Eclipse Adoptium'
    $jdk = Get-ChildItem -LiteralPath $jdkRoot -Directory -ErrorAction SilentlyContinue |
        Sort-Object Name -Descending |
        Select-Object -First 1
    if (-not $jdk) {
        throw 'JDK 17 was not found. Set JAVA_HOME to a JDK 17 installation.'
    }
    $env:JAVA_HOME = $jdk.FullName
}

$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$env:SPRING_PROFILES_ACTIVE = 'dev'
if ($TestDatabase) {
    & (Join-Path $PSScriptRoot 'mvnw.cmd') `
        '-Dtest=OpportunityDatabaseIntegrationTest,OpportunityServiceTest,TelegramOidcAuthorizationTest,TelegramUserServiceTest,JwtSessionServiceTest,TelegramAuthSuccessHandlerTest,TelegramAuthFailureHandlerTest' `
        test
} else {
    & (Join-Path $PSScriptRoot 'mvnw.cmd') '-Popportunity-mvp' spring-boot:run
}
if ($LASTEXITCODE -ne 0) {
    throw "Maven exited with code $LASTEXITCODE."
}

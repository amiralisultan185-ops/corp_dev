param(
    [string]$PgBin = 'C:\Program Files\PostgreSQL\18\bin',
    [int]$Port = 55432
)
$ErrorActionPreference = 'Stop'
$labDirectory = Split-Path -Parent $PSScriptRoot
$dataDirectory = Join-Path $labDirectory '.local-postgres'
$logPath = Join-Path $labDirectory '.local-postgres.log'

if (-not (Test-Path -LiteralPath (Join-Path $PgBin 'initdb.exe'))) {
    throw "PostgreSQL binaries not found. Set -PgBin or use docker compose up -d --wait."
}

# Reuse the coursework database if Lab 3 already started it on this port.
& "$PgBin\pg_isready.exe" -h 127.0.0.1 -p $Port -q
if ($LASTEXITCODE -eq 0) {
    $savedPassword = $env:PGPASSWORD
    try {
        $env:PGPASSWORD = 'css'
        $probe = & "$PgBin\psql.exe" -X -h 127.0.0.1 -p $Port -U css -d css -tAc 'SELECT 1'
        if ($LASTEXITCODE -ne 0 -or $probe -ne '1') {
            throw "PostgreSQL is already running on port $Port but css/css cannot access database css. Set DB_URL, DB_USER and DB_PASSWORD for your database."
        }
    } finally {
        $env:PGPASSWORD = $savedPassword
    }
    Write-Host "Ready: reusing jdbc:postgresql://localhost:$Port/css (css / css)."
    return
}

if (-not (Test-Path -LiteralPath (Join-Path $dataDirectory 'PG_VERSION'))) {
    $passwordPath = Join-Path $labDirectory '.pg-password.tmp'
    try {
        [IO.File]::WriteAllText($passwordPath, "css`n")
        & "$PgBin\initdb.exe" -D $dataDirectory -U css --pwfile=$passwordPath --auth-host=scram-sha-256 --auth-local=trust --encoding=UTF8 --locale=C
        if ($LASTEXITCODE -ne 0) { throw 'initdb failed' }
    } finally {
        Remove-Item -LiteralPath $passwordPath -ErrorAction SilentlyContinue
    }
}

& "$PgBin\pg_ctl.exe" -D $dataDirectory status
if ($LASTEXITCODE -ne 0) {
    & "$PgBin\pg_ctl.exe" -D $dataDirectory -l $logPath -o "-p $Port -h 127.0.0.1" -w start
    if ($LASTEXITCODE -ne 0) { throw 'PostgreSQL failed to start; check .local-postgres.log' }
}

$previousPassword = $env:PGPASSWORD
try {
    $env:PGPASSWORD = 'css'
    $exists = & "$PgBin\psql.exe" -X -h 127.0.0.1 -p $Port -U css -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = 'css'"
    if ($LASTEXITCODE -ne 0) { throw 'Cannot connect to the local PostgreSQL cluster' }
    if ($exists -ne '1') {
        & "$PgBin\createdb.exe" -h 127.0.0.1 -p $Port -U css css
        if ($LASTEXITCODE -ne 0) { throw 'Cannot create css database' }
    }
} finally {
    $env:PGPASSWORD = $previousPassword
}
Write-Host "Ready: jdbc:postgresql://localhost:$Port/css (css / css)."

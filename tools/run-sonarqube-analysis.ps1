param(
    [Parameter(Mandatory = $true)]
    [string]$SonarToken,

    [string]$SonarHostUrl = "http://localhost:9000",

    [switch]$SkipTests
)

$ErrorActionPreference = "Stop"

$modules = @(
    @{ Path = "CatalogService"; Key = "pharmacy-catalog"; Name = "pharmacy-catalog" },
    @{ Path = "pharmacy-admin"; Key = "pharmacy-admin"; Name = "pharmacy-admin" },
    @{ Path = "pharmacy-auth"; Key = "pharmacy-auth"; Name = "pharmacy-auth" },
    @{ Path = "pharmacy-config-server"; Key = "pharmacy-config-server"; Name = "pharmacy-config-server" },
    @{ Path = "pharmacy-eureka"; Key = "pharmacy-eureka"; Name = "pharmacy-eureka" },
    @{ Path = "pharmacy-gateway"; Key = "pharmacy-gateway"; Name = "pharmacy-gateway" },
    @{ Path = "pharmacy-order"; Key = "pharmacy-order"; Name = "pharmacy-order" }
)

$root = Split-Path -Parent $PSScriptRoot

foreach ($module in $modules) {
    $modulePath = Join-Path $root $module.Path
    Write-Host "Running SonarQube analysis for $($module.Name)..."

    Push-Location $modulePath
    try {
        $mavenArgs = @(
            "clean",
            "verify",
            "sonar:sonar",
            "-Dsonar.host.url=$SonarHostUrl",
            "-Dsonar.login=$SonarToken",
            "-Dsonar.projectKey=$($module.Key)",
            "-Dsonar.projectName=$($module.Name)"
        )

        if ($SkipTests) {
            $mavenArgs += "-DskipTests=true"
        }

        & .\mvnw.cmd @mavenArgs

        if ($LASTEXITCODE -ne 0) {
            throw "SonarQube analysis failed for $($module.Name) with exit code $LASTEXITCODE."
        }
    }
    finally {
        Pop-Location
    }
}

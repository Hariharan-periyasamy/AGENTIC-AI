$baseUrl = 'http://localhost:8080'
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession

# Test Public Endpoints
Write-Host 'Testing /login...'
$response = Invoke-WebRequest -Uri "$baseUrl/login" -WebSession $session -UseBasicParsing -ErrorAction SilentlyContinue
Write-Host "Status: $($response.StatusCode)"

# Login as admin
Write-Host 'Logging in as admin...'
$loginData = @{
    email = 'admin@example.com'
    password = 'admin'
}
$response = Invoke-WebRequest -Uri "$baseUrl/login" -Method Post -Body $loginData -WebSession $session -UseBasicParsing -ErrorAction SilentlyContinue

# Admin endpoints
$adminEndpoints = @('/admin/dashboard', '/admin/requests', '/admin/audit-logs')
foreach ($endpoint in $adminEndpoints) {
    $response = Invoke-WebRequest -Uri "$baseUrl$endpoint" -WebSession $session -UseBasicParsing -ErrorAction SilentlyContinue
    Write-Host "$endpoint Status: $($response.StatusCode)"
}

# Login as user
Write-Host 'Logging in as user...'
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$loginData = @{
    email = 'hariharan@gmail.com'
    password = 'password' # wait, I don't know the password they registered with, let's use the seeded one hari@gmail.com / password
}
$loginData.email = 'hari@gmail.com'
$loginData.password = 'password'
$response = Invoke-WebRequest -Uri "$baseUrl/login" -Method Post -Body $loginData -WebSession $session -UseBasicParsing -ErrorAction SilentlyContinue

# User endpoints
$userEndpoints = @('/user/dashboard', '/user/certificates', '/user/new-request')
foreach ($endpoint in $userEndpoints) {
    $response = Invoke-WebRequest -Uri "$baseUrl$endpoint" -WebSession $session -UseBasicParsing -ErrorAction SilentlyContinue
    Write-Host "$endpoint Status: $($response.StatusCode)"
}

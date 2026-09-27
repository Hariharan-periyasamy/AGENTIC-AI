@echo off
echo ===================================================
echo  NGROK TUNNEL STARTUP
echo  Exposing Jenkins (port 8081) for GitHub webhooks
echo ===================================================
echo.
echo After ngrok starts, copy the https:// Forwarding URL.
echo Configure GitHub webhook as: https://YOUR-URL/github-webhook/
echo.
echo Starting ngrok tunnel...
ngrok http 8081

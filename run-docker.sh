#!/bin/bash

echo "🔧 Đang build project với Maven..."
mvn clean package -DskipTests

echo "🐳 Đang build Docker image..."
docker build -t foodstore-app .

echo "🚀 Đang chạy Docker container..."
docker run -p 9090:9090 --env-file .env foodstore-app

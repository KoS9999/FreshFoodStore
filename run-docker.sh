#!/bin/bash

echo "Đang build project với Maven..."
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "Maven build thất bại!"
    exit 1
fi

echo "Đang build Docker image..."
docker build -t foodstore-app .
if [ $? -ne 0 ]; then
    echo "Docker build thất bại!"
    exit 1
fi

echo "Đang chạy Docker container ứng dụng..."
docker rm -f foodstore-app 2>/dev/null || true
docker run --name foodstore-app \
    -p 9090:9090 \
    --env-file .env \
    foodstore-app
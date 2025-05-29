#!/bin/bash

echo "Простой тест вебинаров..."

# Получаем страницу входа
COOKIES=$(mktemp)
LOGIN_RESPONSE=$(curl -s -c "$COOKIES" http://localhost:8080/login)
CSRF_TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o 'name="_csrf" value="[^"]*"' | head -1 | sed 's/name="_csrf" value="//' | sed 's/"//')

# Авторизуемся
curl -s -b "$COOKIES" -c "$COOKIES" \
    -d "username=student" \
    -d "password=password" \
    -d "_csrf=$CSRF_TOKEN" \
    -X POST \
    http://localhost:8080/login > /dev/null

echo "Авторизация завершена. Тестируем вебинар 1..."

# Делаем запрос к вебинару 1
curl -s -b "$COOKIES" -w "HTTP_CODE:%{http_code}" http://localhost:8080/webinars/1 > /dev/null

echo "Запрос отправлен. Проверьте логи."

rm -f "$COOKIES" 
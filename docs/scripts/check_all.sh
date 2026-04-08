#!/bin/bash
# Комплексна перевірка коду Sportrack: Лінтинг + Компіляція
echo "=== Початок статичного аналізу ==="

echo "[1/3] Запуск Android Lint..."
./gradlew lint
LINT_STATUS=$?

echo "[2/3] Запуск перевірки статичної типізації (Kotlin Compiler)..."
./gradlew compileDebugKotlin
TYPE_STATUS=$?

if [ $LINT_STATUS -eq 0 ] && [ $TYPE_STATUS -eq 0 ]; then
    echo "=== Успіх! Код пройшов усі перевірки ==="
    exit 0
else
    echo "=== Помилка! Знайдено проблеми в коді ==="
    exit 1
fi
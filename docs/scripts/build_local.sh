#!/bin/bash
# Скрипт для локальної збірки проєкту
echo "Починаємо збірку Sportrack..."
./gradlew clean
./gradlew assembleDebug
echo "Збірка завершена! Шукайте APK в app/build/outputs/apk/debug/"
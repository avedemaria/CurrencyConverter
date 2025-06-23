# Currency Converter App

## Используемые подходы и технологии

- Retrofit (для демонстрации загрузки символов и названий валют вместо использования ресурсов)
- Hilt
- Unit-тесты (частично)
- UI - фрагменты и навигации через Navigation Component
- MVVM и Clean Architecture с разделением на слои

## Настройка ключей API

Для корректной работы приложения необходимо указать URL сервера и ключ API

В корне проекта должен быть файл `local.properties` со следующими параметрами:

   1. CURRENCY_API_URL=https://api.currencyapi.com/v3/
   2. CURRENCY_API_KEY=cur_live_1JyX7FWkzZzlEcQmSaU92mCQQfNyFSnHNgGvVd8b

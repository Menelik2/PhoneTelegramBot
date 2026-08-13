import requests

url = "https://api.telegram.org/bot8737241034:AAGtIGWu1KXUKncSyW6x_TmrV2c32kWWKhI/getMe"
response = requests.get(url)
print(response.json())

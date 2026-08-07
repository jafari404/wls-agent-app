# WLS Agent App

اپ اندروید پشتیبان (اپراتور) برای پلاگین وردپرس `wp-live-support`.

## راه‌اندازی
1. پروژه را در Android Studio باز کنید.
2. در `app/build.gradle.kts` مقدار `SITE_BASE_URL` را به آدرس سایتتان تغییر دهید.
3. برای Push:
   - در Firebase Console یک اپ اندروید با applicationId برابر `ir.axio.wlsagent` اضافه کنید.
   - `google-services.json` را در پوشه `app/` بگذارید.
4. برای هر کاربر پشتیبان: در wp-admin وارد پروفایل کاربر شوید → Application Passwords → یک رمز جدید بسازید (مثلاً با نام "Android Agent App") → آن رمز را (نه رمز اصلی) در صفحه ورود اپ وارد کنید.
5. کاربرانی که باید پشتیبان محسوب شوند باید نقش Administrator داشته باشند، یا در `wp-live-support/includes/class-wls-auth.php` کپبیلیتی `wls_agent` را به نقش دلخواه (مثلاً Editor) اضافه کنید.

## تست‌ها
اجرای تست‌های واحد و گزارش پوشش کد:

```
./gradlew jacocoDebugUnitTestReport
```

گزارش پوشش در `app/build/reports/jacoco/jacocoDebugUnitTestReport/html/index.html` ساخته می‌شود.

## نکته
Application Password فقط روی HTTPS کار می‌کند. اگر سایت شما هنوز SSL ندارد، ابتدا آن را فعال کنید.

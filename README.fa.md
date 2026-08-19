# PersianDatePicker

[![JitPack](https://jitpack.io/v/AliBinkhani/PersianDatePicker.svg)](https://jitpack.io/#AliBinkhani/PersianDatePicker)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![minSdk](https://img.shields.io/badge/minSdk-26-brightgreen.svg)](https://developer.android.com/studio/releases/platforms)
[![Compose Material 3](https://img.shields.io/badge/Jetpack%20Compose-Material%203-7F52FF.svg)](https://m3.material.io/components/date-pickers/overview)

[Read in English](README.md)

یک تقویم‌بازِ (date picker) **متریال ۳ (Material 3)** برای جتپک کامپوز (Jetpack Compose) در اندروید، با
پشتیبانی کامل و درجه‌یک از **تقویم فارسی (جلالی/هجری شمسی)** در کنار **تقویم میلادی** — هر دو در یک
کامپوننت واحد، که هر زمان بخواهید می‌توانید بین آن‌ها جابه‌جا شوید.

این کتابخانه در ابتدا کپی‌ای از کامپوننت `DatePicker` در `androidx.compose.material3` بود (که چون چند
نوع مورد نیازش — `CalendarModel`، توکن‌های طراحی، `Strings`، `Icons` — به‌صورت `internal` در ماژول
material3 تعریف شده‌اند و از بیرون آن ماژول قابل استفاده یا ارث‌بری نیستند، ناچار به کپی‌کردن شدیم)، و
سپس با پشتیبانی از تقویم فارسی و چند قابلیت دیگر که در کامپوننت اصلی وجود ندارد، مثل یک انتخاب‌گر ماه
(month picker)، گسترش داده شد. ظاهر، فاصله‌گذاری و رفتار این کامپوننت مطابق با
[مشخصات تقویم متریال ۳](https://m3.material.io/components/date-pickers/specs) است.

## تصاویر

<table>
  <tr>
    <td align="center"><img src="docs/screenshots/persian-en.png" width="220" alt="تقویم فارسی، رابط کاربری انگلیسی"><br><sub>تقویم فارسی (رابط کاربری انگلیسی)</sub></td>
    <td align="center"><img src="docs/screenshots/persian-fa.png" width="220" alt="تقویم فارسی، رابط کاربری فارسی"><br><sub>تقویم فارسی (رابط کاربری فارسی)</sub></td>
    <td align="center"><img src="docs/screenshots/gregorian-en.png" width="220" alt="تقویم میلادی"><br><sub>تقویم میلادی</sub></td>
    <td align="center"><img src="docs/screenshots/month-picker.png" width="220" alt="انتخاب‌گر ماه"><br><sub>انتخاب‌گر ماه</sub></td>
  </tr>
</table>

## امکانات

- **ساخته‌شده روی جتپک کامپوز + متریال ۳.** رنگ‌ها از `MaterialTheme.colorScheme` خوانده می‌شوند و از
  طریق `DatePickerColors` کاملاً قابل شخصی‌سازی هستند؛ در نتیجه تقویم به‌صورت خودکار با تم اپ شما هماهنگ
  می‌شود (از جمله رنگ داینامیک و حالت تیره)، نه اینکه مثل یک کامپوننت بیگانه به نظر برسد.
- **تقویم فارسی و میلادی، در کنار هم.** هر `DatePicker` یک `CalendarType` (`PERSIAN` یا `GREGORIAN`)
  انتخاب می‌کند؛ تغییر آن نحوه محاسبه و نمایش تاریخ‌ها را عوض می‌کند — سال‌های کبیسه، طول ماه‌ها و همه
  چیز — بدون اینکه چیز دیگری در نحوه استفاده از API تغییر کند.
- **رابط کاربری فارسی و انگلیسی، برای هر دو تقویم.** قالب‌بندی تاریخ در تقویم (نام ماه‌ها، ارقام، نام
  روزهای هفته، چیدمان راست‌به‌چپ) از یک `locale` صریح که شما مشخص می‌کنید پیروی می‌کند، مستقل از نوع
  تقویم. یعنی می‌توانید تقویم فارسی را با رابط انگلیسی نشان دهید (`مرداد` ← "Mordad") یا تقویم میلادی را
  با رابط فارسی، نه فقط دو حالت «معمول».
- **یک انتخاب‌گر ماه**، علاوه بر انتخاب‌گر سال — با لمس فیلد ماه می‌توانید مستقیم به هر ماهی از سال جاری
  بروید، دقیقاً مثل انتخاب‌گر سال. این قابلیت در کامپوننت اصلی `DatePicker` در `androidx.compose.material3`
  وجود ندارد؛ آن کامپوننت فقط انتخاب‌گر سال دارد.
- **فلش‌های ناوبری مستقل برای ماه و سال**، که هرکدام به‌صورت اختیاری با `showMonthNavigationArrows` و
  `showYearNavigationArrows` قابل مخفی‌کردن هستند — کامپوننت اصلی فقط فلش‌های ماه را دارد.
- **نمایش روزهای ماه مجاور.** خانه‌های ابتدایی/انتهایی جدول می‌توانند شماره روزهای (غیرفعال و کم‌رنگ) ماه
  قبل/بعد را طبق مشخصات متریال ۳ نشان دهند؛ این قابلیت با `showAdjacentMonthDays` قابل تنظیم است.
- **نوع `CalendarDate` که تقویم را می‌شناسد**، به‌جای یک `Long` خام بر حسب میلی‌ثانیه، برای خواندن و
  نوشتن تاریخ انتخاب‌شده استفاده می‌شود. این نوع، `CalendarType` خودش را همراه دارد؛ در نتیجه دادن یک
  تاریخ میلادی به یک تقویم فارسی (یا برعکس) به‌جای تولید تاریخ نامعتبر، به‌صورت خودکار تبدیل می‌شود.
- **ابزارهای تعامل (interop)** برای `java.util.Date`، `java.time.LocalDate` و `Long` بر حسب
  میلی‌ثانیه‌ی UTC، تا یکپارچه‌سازی با کدهای مبتنی بر timestamp موجود (یک پایگاه داده، یک REST API،
  `LocalDate.now()`) نیازی به محاسبات دستی تقویم نداشته باشد.
- **یک `DatePickerDialog`** که یک باگ واقعی در چیدمان دیالوگ متریال ۳ اصلی (روی‌هم‌افتادن ردیف حروف روزهای
  هفته با اولین ردیف شماره روزها) را با استفاده از یک `Surface` که به‌طور طبیعی ارتفاعش متناسب با محتوا
  است، به‌جای مکانیزم ارتفاع پویای نسخه اصلی که برای پشتیبانی از یک حالت نمایش ورودی (Input) که این
  کتابخانه پیاده‌سازی نکرده وجود دارد، برطرف می‌کند.

## پیش‌نیازها

| | |
|---|---|
| **minSdk** | ۲۶ (اندروید ۸.۰) |
| **compileSdk** | ۳۷ |
| **کاتلین (Kotlin)** | ۲.۲.۱۰ به بالا، همراه با [افزونه‌ی Compose Compiler برای گریدل](https://developer.android.com/develop/ui/compose/compiler) |
| **جتپک کامپوز (Jetpack Compose)** | BOM نسخه‌ی ۲۰۲۶.۰۲.۰۱ به بالا (Compose Foundation، Material 3، Animation) |

تقویم فارسی با استفاده از `android.icu.util.Calendar` پیاده‌سازی شده، که از API 24 بخشی از خود پلتفرم
اندروید بوده — و برای آن نیازی به هیچ وابستگی اضافه‌ای نیست.

## نصب

این کتابخانه از طریق [JitPack](https://jitpack.io/#AliBinkhani/PersianDatePicker) منتشر می‌شود.

**۱. مخزن JitPack را اضافه کنید** به فایل `settings.gradle.kts` پروژه‌تان:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

<details>
<summary>Groovy DSL (<code>settings.gradle</code>)</summary>

```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

</details>

**۲. وابستگی را اضافه کنید** به فایل `build.gradle.kts` ماژول خود:

```kotlin
dependencies {
    implementation("com.github.AliBinkhani:PersianDatePicker:<version>")
}
```

<details>
<summary>Groovy DSL (<code>build.gradle</code>)</summary>

```groovy
dependencies {
    implementation 'com.github.AliBinkhani:PersianDatePicker:<version>'
}
```

</details>

به‌جای `<version>`، یکی از [تگ‌های منتشرشده](https://github.com/AliBinkhani/PersianDatePicker/releases)
(مثلاً `1.0.0`)، یک هش کامیت مشخص، یا `main-SNAPSHOT` برای دنبال‌کردن آخرین کامیت برنچ `main` را قرار
دهید.

## استفاده

### شروع سریع: یک تقویم‌باز فارسی

```kotlin
@Composable
fun MyScreen() {
    // Defaults to CalendarType.PERSIAN and the app's current locale.
    val state = rememberDatePickerState()
    DatePicker(state = state)
}
```

### در یک دیالوگ

الگوی رایج: یک دکمه که یک `DatePickerDialog` را باز می‌کند، به همراه بخشی از رابط کاربری که مقدار
انتخاب‌شده‌ی فعلی را نشان می‌دهد.

```kotlin
@Composable
fun MyScreen() {
    val state = rememberDatePickerState()
    var dialogIsVisible by remember { mutableStateOf(false) }

    val selectedDate = state.selectedDate
    Text(selectedDate?.let { "${it.year}/${it.month}/${it.dayOfMonth}" } ?: "No date selected")
    Button(onClick = { dialogIsVisible = true }) { Text("Select date") }

    if (dialogIsVisible) {
        DatePickerDialog(
            onDismissRequest = { dialogIsVisible = false },
            confirmButton = { TextButton(onClick = { dialogIsVisible = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { dialogIsVisible = false }) { Text("Cancel") } },
        ) {
            DatePicker(state = state)
        }
    }
}
```

### تقویم میلادی

برای تغییر نوع تقویم، `calendarType` را پاس بدهید — بقیه‌ی API بدون تغییر باقی می‌ماند:

```kotlin
val state = rememberDatePickerState(calendarType = CalendarType.GREGORIAN)
```

### تقویم فارسی با رابط کاربری فارسی

نوع تقویم (`calendarType`) و زبان نمایش (`locale`) دو تنظیم مستقل از هم هستند. مقدار پیش‌فرض `locale`
از زبان جاری اپ پیروی می‌کند، اما می‌توانید هر locale دیگری را صراحتاً مشخص کنید:

```kotlin
val state = rememberDatePickerState(
    calendarType = CalendarType.PERSIAN, // the default; shown here for clarity
    locale = Locale("fa"),
)
```

> نکته: `locale` فقط *قالب‌بندی* تاریخ در خودِ تقویم (نام ماه‌ها، ارقام، نام روزهای هفته، راست‌به‌چپ) را
> کنترل می‌کند. رشته‌های ثابت رابط کاربری مثل دکمه‌های «تأیید»/«لغو» در دیالوگ، از تنظیمات locale منابع
> (resource) اپ شما پیروی می‌کنند — دقیقاً مثل هر متن دیگری در Compose که در اپ شما نمایش داده می‌شود.

### خواندن و تبدیل تاریخ انتخاب‌شده

`DatePickerState.selectedDate` یک `CalendarDate` است — یعنی `year`/`month`/`dayOfMonth` ساده، بر حسب
`calendarType` خودِ همان تقویم، نه یک `Long` بر حسب میلی‌ثانیه:

```kotlin
data class CalendarDate(val year: Int, val month: Int, val dayOfMonth: Int, val calendarType: CalendarType)
```

آن را با `toPersian()`/`toGregorian()` به سیستم تقویم دیگر تبدیل کنید:

```kotlin
val selected: CalendarDate? = state.selectedDate         // e.g. year=1405, month=5, day=7 (Persian)
val gregorian: CalendarDate? = selected?.toGregorian()    // e.g. year=2026, month=7, day=29
```

اختصاص‌دادن یک `CalendarDate` از یک سیستم تقویم *متفاوت* به `selectedDate` به‌صورت خودکار تبدیل می‌شود؛
پس هیچ‌وقت لازم نیست پیش از دادن یک تاریخ به تقویم، خودتان دستی آن را تبدیل کنید:

```kotlin
// state.calendarType == CalendarType.PERSIAN
state.selectedDate = CalendarDate(2026, 8, 18, CalendarType.GREGORIAN) // auto-converted to Persian
```

### تعامل با `Long`، `Date` و `LocalDate`

اگر فقط یک timestamp میلی‌ثانیه‌ای UTC، یک `java.util.Date`، یا یک `java.time.LocalDate` دارید (مثلاً
از یک پایگاه داده یا REST API)، آن را با یکی از موارد زیر تبدیل کنید — همه‌ی آن‌ها از طریق instant به
وقت UTC عبور می‌کنند که سیستم تقویم مخصوص به خودش را ندارد، پس صرف‌نظر از `calendarType` همیشه امن
هستند:

```kotlin
CalendarDate.fromEpochMillis(utcTimeMillis)   // -> CalendarDate (Gregorian)
date.toEpochMillis()                          // CalendarDate -> Long

CalendarDate.fromJavaDate(javaUtilDate)       // -> CalendarDate (Gregorian)
date.toJavaDate()                             // CalendarDate -> java.util.Date

CalendarDate.fromLocalDate(localDate)         // -> CalendarDate (Gregorian)
date.toLocalDate()                            // CalendarDate -> java.time.LocalDate
```

### شخصی‌سازی ناوبری و جدول روزها

```kotlin
DatePicker(
    state = state,
    showMonthNavigationArrows = true,  // "‹ Mordad ›"
    showYearNavigationArrows = true,   // "‹ 1405 ›"
    showAdjacentMonthDays = true,      // grayed-out days from the previous/next month
)
```

### مرجع سریع API

| نماد | توضیح |
|---|---|
| `DatePicker(state, ...)` | خودِ کامپوننت قابل‌ترکیب (composable) تقویم — یک جدول تقویمی با ناوبری ماه/سال. |
| `DatePickerDialog(onDismissRequest, confirmButton, ...)` | یک `DatePicker` (یا محتوای دیگر) را در یک دیالوگ متریال ۳ همراه با دکمه‌های تأیید/لغو قرار می‌دهد. |
| `rememberDatePickerState(...)` | یک `DatePickerState` می‌سازد و آن را در طول recomposition‌ها/تغییرات پیکربندی حفظ می‌کند. |
| `DatePickerState` | وضعیت تقویم: `selectedDate`، `displayedMonth`، `displayMode`، `calendarType`، `yearRange`، `selectableDates`، `locale`. |
| `CalendarType` | `PERSIAN` یا `GREGORIAN` — اینکه یک تقویم یا تاریخ بر حسب کدام سیستم تقویم بیان شده است. |
| `CalendarDate` | یک `year`/`month`/`dayOfMonth` به‌همراه `CalendarType` مربوط به آن‌ها. `toPersian()`/`toGregorian()` بین این دو تبدیل می‌کنند. |
| `CalendarYearMonth` | یک جفت ساده‌ی `year`/`month` — نوع داده‌ی `DatePickerState.displayedMonth`. |
| `SelectableDates` | برای غیرفعال‌کردن تاریخ‌ها یا سال‌های خاص پیاده‌سازی کنید (در رابط کاربری به‌صورت غیرفعال نمایش داده می‌شوند). |
| `DatePickerDefaults` | مقادیر پیش‌فرض و کارخانه‌ها (factories): `colors()`، `dateFormatter()`، بازه‌های سال پیش‌فرض (`PersianYearRange` = ۱۳۰۰ تا ۱۵۰۰، `YearRange` = ۱۹۰۰ تا ۲۱۰۰) و غیره. |

### اپ نمونه

ماژول [`app`](app) یک نمونه‌ی کامل و اجراشدنی است — مخزن را کلون کرده و اجرایش کنید تا تقویم را در عمل
ببینید.

## مجوز (License)

```
Copyright 2026 Ali Binkhani

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

برای متن کامل به [LICENSE](LICENSE) مراجعه کنید. این پروژه بخش‌هایی از کد
[`androidx.compose.material3`](https://developer.android.com/jetpack/androidx/releases/compose-material3)
(AOSP، مجوز Apache License 2.0) را کپی و اقتباس کرده است — فایل‌های مربوطه هدر کپی‌رایت اصلی خود را
حفظ کرده‌اند.

# Документация плагина NMPlugin для разработки модов Mindustry

Плагин предназначен для упрощения сборки, аннотационной обработки, запуска и публикации модов для игры Mindustry. Поддерживает Java 20+ с обратной совместимостью через Jabel. Публикуется на JitPack: `com.github.nekit508.mindustry-mod-plugin`.

---

## 1. Подключение плагина

### 1.1 settings.gradle
```groovy
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url 'https://www.jitpack.io' }
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == 'com.github.nekit508.mindustry-mod-plugin') {
                useModule("com.github.nekit508.mindustry-mod-plugin:${requested.version}")
            }
        }
    }
}
```

### 1.2 build.gradle (корневой проект)
```groovy
plugins {
    id 'java'
    id 'com.github.nekit508.mindustry-mod-plugin' version 'latest-version'
}
```

---

## 2. Глобальные настройки (локальный файл)

Плагин читает `settings/settings.local.json` (или `settings/local.json` как fallback). Пример:

```json
{
  "build": {
    "useAndroid": true,
    "sdkRoot": "D:/soft/android-sdk"
  },
  "copy": [
    "C:/Users/user/AppData/Roaming/Mindustry/mods"
  ],
  "mindustry": {
    "downloadDir": "C:/Users/user/AppData/Roaming/Mindustry/versions",
    "workingDirectory": "mindustry-dir.local",
    "dataDirectory": "mindustry-dir.local/data",
    "copyModInDataDir": true
  },
  "offlineMode": false,
  "autoOfflineMode": true,
  "autoOfflineModeTimeout": 1000
}
```

| Ключ | Описание | По умолчанию |
|------|----------|---------------|
| `build.useAndroid` | Включить dex (Android) | `true` |
| `build.sdkRoot` | Путь к Android SDK | env `ANDROID_HOME`/`ANDROID_SDK_ROOT` |
| `copy` | Список каталогов для копирования готового .jar | – |
| `mindustry.downloadDir` | Куда скачивать Mindustry.jar | `build/mindustry` |
| `mindustry.workingDirectory` | Рабочая папка для запуска Mindustry | – |
| `mindustry.dataDirectory` | Папка данных игры (APPDATA/XDG_DATA_HOME) | `workingDirectory` |
| `mindustry.copyModInDataDir` | Копировать мод в папку mods в dataDirectory | `true` |
| `offlineMode` | Принудительно офлайн | `false` |
| `autoOfflineMode` | Автоопределение (ping google.com, github.com) | `true` |
| `autoOfflineModeTimeout` | Таймаут проверки соединения, мс | `5000` |

---

## 3. Расширения плагина

В корневом `build.gradle` создаются экземпляры расширений:

```groovy
nmp.core(project, "core", isLib, group)
nmp.anno(project, "anno", core)
nmp.entityAnno(project, "eanno", core, excludeComponents)
nmp.tools(project, "tools", core)
nmp.mmcAnno(project, "mmcAnno", core)   // закомментировано в примере
```

### 3.1 NMPluginCoreExtension – основная сборка мода

Обязательное расширение, создаёт задачи для компиляции, dex, запуска, генерации `mod.json`.

**Параметры конструктора:**
- `name` – имя расширения (произвольное, используется в `core.with`)
- `project` – проект Gradle
- `publishable` – если `true`, настраивает `maven-publish` + `java-library`
- `group` – Maven group ID (обязателен при `publishable=true`)

**Свойства (можно переопределить в `nmp.setting`):**

| Свойство | Тип | По умолчанию | Описание |
|----------|-----|--------------|-----------|
| `mindustryVersion` | `String` | `"v146"` | Версия Mindustry (тег GitHub) |
| `modName` | `String` | `project.name` | Имя мода (в `mod.json`) |
| `modVersion` | `String` | – | Версия мода (обязательно задать) |
| `modGroup` | `String` | `project.group` | Группа для Maven |
| `jabelVersion` | `String` | `"1.0.1-1"` | Версия Jabel |
| `sourceCompatibility` | `JavaVersion` | `JavaVersion.VERSION_20` | Целевая версия Java |
| `generateModInfo` | `Boolean` | `true` | Автогенерация `mod.json` |
| `mindustryWorkingDirectory` | `File` | из `local` | Рабочий каталог для запуска |
| `mindustryDataDirectory` | `File` | из `local` или `workingDirectory` | Каталог данных |
| `mindustryCopyModInDataDir` | `Boolean` | из `local` (или `true`) | Копировать мод в `.../mods` |
| `srcDirs` | `List<File>` | `["src"]` | Исходники Java |
| `resDirs` | `List<File>` | `["res"]` | Ресурсы |
| `genDir` | `File` | `"gen"` | Каталог для сгенерированных исходников |
| `mavenPublishPluginName` | `String` | `"maven-publish"` |
| `javaLibraryPluginName` | `String` | `"java-library"` |

**Задачи (создаются автоматически):**

| Задача | Описание |
|--------|----------|
| `nmpBuild` | Собирает `classes.jar` (со всеми runtime зависимостями) |
| `nmpDex` | Конвертирует `nmpBuild` в dex (если `buildAndroid=true`) |
| `nmpBuildRelease` | Финальный jar (дексированный или обычный) |
| `nmpCopyBuildRelease` | Копирует `nmpBuildRelease` во все каталоги из `copy` + `mods` внутри `dataDirectory` |
| `nmpGenerateModInfo` | Генерация `mod.json` |
| `nmpFetchMindustry` | Скачивает Mindustry.jar (с проверкой sha256) |
| `nmpRunMindustry` | Запускает Mindustry с подстановкой `workingDirectory` и `dataDirectory` |
| `nmpCopyBuildReleaseRunMindustry` | Очередь: копирование → запуск |
| `nmpBuildSources` (если `publishable`) | Jar с исходниками |
| `nmpBuildLibrary` (если `publishable`) | Jar для Maven публикации |

**Пример настройки core:**

```groovy
core.with {
    core.configureTasks {
        nmpGenerateModInfo { GenerateModInfoTask task ->
            task.set(
                "author": "NaN",
                "description": "Test mod.",
                "repo": "https://github.com/...",
                "main": "core.Mod"
            )
        }
    }

    nmp.setting {
        modVersion.set "v0.0.4"
        mindustryVersion.set "v146"
    }
}
```

### 3.2 NMPluginAnnoExtension – модуль с аннотационными процессорами

Создаёт отдельный модуль (подпроект), который компилируется и используется как `compileOnly` и `annotationProcessor` для `core`.

**Параметры:**
- `name` – имя расширения
- `project` – проект (обычно подпроект `:annotations`)
- `core` – экземпляр `NMPluginCoreExtension`

**Свойства:**

| Свойство | Тип | По умолчанию | Описание |
|----------|-----|--------------|-----------|
| `jabelVersion` | `String` | из `core` | |
| `sourceCompatibility` | `JavaVersion` | из `core` | |
| `srcDirs` | `List<File>` | `["src"]` | |
| `resDirs` | `List<File>` | `["res"]` | |
| `genDir` | `File` | `"gen"` | |

**Задачи:**
- `nmpaGenerateProcessorsFile` – генерирует файл `META-INF/services/javax.annotation.processing.Processor` на основе классов, помеченных аннотацией-триггером (по умолчанию ищет `// anno processor class` в файлах, но можно переопределить).

**Настройка триггера:**
```groovy
anno.with {
    configureTasks {
        nmpaGenerateProcessorsFile {
            triggerString.set "@AnnotationProcessor"
        }
    }
}
```

> **Важно:** Плагин добавляет `compileOnly` и `annotationProcessor` зависимость от `:annotations` в `core` проекте.

### 3.3 NMPluginEntityAnnoExtension – интеграция EntityAnno

Использует библиотеку [EntityAnno](https://github.com/GglLfr/EntityAnno) для автоматической генерации компонентов (композиция вместо наследования). Работает только в том же проекте, что и `core` (нельзя выносить в подпроект).

**Параметры:**
- `name` – имя расширения
- `project` – проект (должен совпадать с проектом `core`)
- `core` – экземпляр `NMPluginCoreExtension`
- `excludeComponents` – исключать ли сгенерированные классы компонентов из итогового jar (рекомендуется `true`)

**Свойства:**

| Свойство | Тип | По умолчанию | Описание |
|----------|-----|--------------|-----------|
| `genPackage` | `String` | – | Пакет для сгенерированных классов (обязательно задать) |
| `revisionsDir` | `DirectoryProperty` | `"revisions"` | Каталог для ревизий |
| `fetchedCompsPackage` | `String` | `genPackage + ".comps.fetched"` | Пакет для загруженных компонентов Mindustry |
| `fetchedCompsDir` | `DirectoryProperty` | `"fetchedComps"` | Каталог для скачанных компонентов |
| `modCompsPackage` | `String` | `genPackage + ".comps"` | Пакет для своих компонентов |
| `entityAnnoVersion` | `String` | – | Версия EntityAnno (обязательно задать) |

**Задачи:**
- `nmpeaFetchComps` – скачивает файлы компонентов из официального репозитория Mindustry (пакет `mindustry.entities.comp`), модифицирует их (заменяет аннотации) и сохраняет в `fetchedCompsDir`. Выполняется перед `compileJava`.

**Настройка:**
```groovy
eanno.with {
    nmp.setting {
        genPackage.set "core.gen"
        entityAnnoVersion.set "1.0.0"   // актуальная версия
    }
}
```

> **Примечание:** В коде есть TODO по использованию kapt для Kotlin, но пока используется стандартный `annotationProcessor`. `@EntityDef`, `@Component` и т.д. преобразуются в `@EntityComponent(base = true, vanilla = true)`.

### 3.4 NMPluginToolsExtension – утилиты/инструменты

Создаёт модуль для написания вспомогательных Java-программ (например, процедурная генерация контента). Запуск через `nmptRunTools`.

**Параметры:** аналогично `anno`: `name`, `project`, `core`.

**Свойства:** те же, что у `NMPluginAnnoExtension`.

**Задача:**
- `nmptRunTools` – `JavaExec`, запускает `mainClass` из модуля. Класс должен быть в `sourceSets.main`. Можно задать `mainClass` в конфигурации:

```groovy
tools.with {
    configureTasks {
        nmptRunTools {
            mainClass.set "tools.Tools"
        }
    }
}
```

Плагин автоматически добавляет `arc-core` как `implementation` (если нужно).

### 3.5 NMPluginMMCAnnoExtension – интеграция MindustryModCore (Zelaux)

Расширение для использования [MindustryModCore](https://github.com/Zelaux/MindustryModCore) (альтернативная реализация аннотаций). В тестовом проекте закомментировано из-за неработающих аргументов компилятора.

**Параметры:** `name`, `project`, `core`.

**Свойства:**

| Свойство | Тип | По умолчанию | Описание |
|----------|-----|--------------|-----------|
| `mmcVersion` | `String` | – | Версия MMC (например `v2.0.3b`) |
| `modules` | `List<String>` | `["load","remote","logic","assets","struct","serialize"]` | Модули для подключения |
| `classPrefix` | `String` | – | Префикс для генерируемых классов |
| `rootDirectory` | `DirectoryProperty` | `project.projectDir` | Корень модуля |
| `genRes` | `DirectoryProperty` | `"genRes"` | Сгенерированные ресурсы |
| `rawRes` | `DirectoryProperty` | `"rawRes"` | Исходные ресурсы |
| `modInfoPath` | `RegularFileProperty` | `nmpGenerateModInfo.outputFile` | Путь к `mod.json` |
| `revisionsPath` | `DirectoryProperty` | `"mmcRevisions"` | Каталог ревизий |

**Метод `genericInit()`** должен быть вызван в конфигурации для включения всей функциональности:

```groovy
mmcAnno.configure {
    genericInit()
    classPrefix.set "TM"
}
```

> **Известная проблема:** Аргументы аннотационного процессора не передаются (указано `// TODO AP argument does not works`).

---

## 4. Структура тестового мода (пример)

```
test/
├── annotations/               ← модуль с процессорами
│   ├── build.gradle
│   └── src/.../annotations/
├── core/                      ← нет, всё в корне, но core – основная логика
├── tools/                     ← модуль инструментов
│   ├── build.gradle
│   └── src/tools/Tools.java
├── src/                       ← исходники основного мода
│   └── core/Mod.java
├── res/                       ← ресурсы
├── gen/                       ← генерируется
├── fetchedComps/              ← скачанные компоненты
├── settings/settings.local.json
├── build.gradle               ← корневой build
└── mod.json                   ← генерируется, но может быть создан вручную
```

Корневой `build.gradle` настраивает `allprojects` с репозиториями и плагином `java`. Затем применяется `NMPlugin` и создаются расширения.

---

## 5. Особенности и баги

### 5.1 Баги (исходя из кода и комментариев)

| Проблема | Место | Статус |
|----------|-------|--------|
| Аргументы AP не передаются для MMC Anno | `NMPluginMMCAnnoExtension` | TODO |
| В `FetchComponentsTask` замена аннотаций может быть неполной | `FetchComponentsTask.groovy` | Возможны ошибки |
| Не учитываются изменения в Mindustry (жесткие строки замены) | там же | Нужно обновлять вручную |
| `BuildLibraryTask` ничего не делает (`// TODO this task now does nothing`) | `BuildLibraryTask.groovy` | Пустая заглушка |
| При использовании `excludeComponents` путь к пакету формируется через `replaceAll("\\.", "/")` – может быть проблематично на Windows | `NMPluginEntityAnnoExtension.groovy` | Используется `replaceAll("[/\\\\]", "/")` |
| При автоофлайн режиме `InetAddress.getByName` может работать медленно или нестабильно | `NMPlugin.groovy` | Рекомендуется явно задавать `offlineMode` |
| `GenerateProcessorsFileTask` – поиск триггера по тексту всего файла, а не по аннотации | `GenerateProcessorsFileTask.groovy` | Неэффективно |
| `DexTask` – не кэшируется (`@DisableCachingByDefault`) | | Всегда выполняется заново |
| `RunMindustry` – всегда не up-to-date (`outputs.upToDateWhen { false }`) | | Всегда запускает игру |

### 5.2 Особенности

- **Jabel** – позволяет использовать Java 20+ синтаксис при компиляции в Java 8 байткод. Включается автоматически через `--release 8 --enable-preview`.
- **Автоопределение офлайн** – пингует `google.com` и `github.com`. При ошибке переводит в офлайн.
- **Скачивание Mindustry.jar** – проверяет SHA-256 из release assets. Если поле `digest` отсутствует, не перезаписывает существующий файл.
- **Генерация `mod.json`** – поддерживает произвольные поля через `miscData`. Основные поля: `name`, `displayName`, `version`, `minGameVersion`, `main`, `author`, `description`, `subtitle`, `repo`, `dependencies`, `softDependencies`, `java`, `pregenerated`, `hidden`, `keepOutlines`.
- **Запуск Mindustry с переопределением путей** – работает через переменные окружения: `APPDATA` (Windows) или `XDG_DATA_HOME` (Linux).
- **Jitpack сборка** – если установлена переменная окружения `JITPACK=true`, то для подпроектов автоматически проставляются `version` (из `VERSION`) и `group` (на основе пути проекта). Только для `publishable=true`.

---

## 6. Пример полного build.gradle (Java 20+)

```groovy
import com.github.nekit508.nmp.tasks.core.GenerateModInfoTask

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        maven { url "https://raw.githubusercontent.com/Zelaux/MindustryRepo/master/repository" }
        maven { url "https://www.jitpack.io" }
    }
    apply plugin: "java"
}

group = "com.example.mod"
def modVersion = "v1.0.0"
def mindustryVer = "v146"
def entityAnnoVer = "1.0.0"

apply plugin: com.github.nekit508.nmp.NMPlugin
apply plugin: "java-library"
apply plugin: "maven-publish"

nmp.core project, "core", false, null   // не publishable

nmp.entityAnno project, "eanno", core, true

core.with {
    nmp.setting {
        modVersion.set modVersion
        mindustryVersion.set mindustryVer
        modName.set "MyAwesomeMod"
    }

    configureTasks {
        nmpGenerateModInfo { task ->
            task.set(
                "author": "YourName",
                "description": "Does cool things",
                "main": "com.example.mod.ModMain",
                "repo": "https://github.com/you/your-mod"
            )
        }
    }
}

eanno.with {
    nmp.setting {
        genPackage.set "com.example.mod.gen"
        entityAnnoVersion.set entityAnnoVer
    }
}

// Дополнительные репозитории для core (например, свои зависимости)
core.attachedProject.dependencies {
    implementation "some:lib:1.0"
}
```

**Настройка Java 20+** – плагин автоматически устанавливает `sourceCompatibility = 20` и настраивает Jabel. Всё, что нужно – использовать современный синтаксис (`var`, `switch expressions`, `record` и т.д.) в коде мода.

---

## 7. Сводные таблицы

### 7.1 Задачи плагина

| Задача | Расширение | Описание |
|--------|------------|----------|
| `nmpBuild` | core | Сборка всех классов и зависимостей в jar |
| `nmpDex` | core | DEX-преобразование (Android) |
| `nmpBuildRelease` | core | Финальный jar мода |
| `nmpCopyBuildRelease` | core | Копирование в папки mods |
| `nmpGenerateModInfo` | core | Генерация `mod.json` |
| `nmpFetchMindustry` | core | Скачивание Mindustry.jar |
| `nmpRunMindustry` | core | Запуск игры |
| `nmpCopyBuildReleaseRunMindustry` | core | Копирование + запуск |
| `nmpaGenerateProcessorsFile` | anno | Генерация сервисного файла для процессоров |
| `nmpeaFetchComps` | entityAnno | Скачивание компонентов Mindustry |
| `nmptRunTools` | tools | Запуск Java-инструментов |
| *(mmc задачи отсутствуют в текущей версии)* | mmcAnno | — |

### 7.2 Важные свойства для mod.json

| Ключ | Тип | Обязателен | default от плагина |
|------|-----|------------|---------------------|
| `name` | String | да | имя проекта |
| `version` | String | да | задать вручную |
| `minGameVersion` | String | да | мажорная версия из `mindustryVersion` |
| `main` | String | да | задать вручную |
| `java` | Boolean | нет | `true` |
| `displayName` | String | нет | – |
| `author` | String | нет | – |
| `description` | String | нет | – |
| `repo` | String | нет | – |
| `dependencies` | List | нет | – |
| `softDependencies` | List | нет | – |
| `hidden` | Boolean | нет | – |
| `keepOutlines` | Boolean | нет | – |

---

Документация актуальна на момент анализа кода плагина (версия соответствует опубликованной на JitPack). Для обновлений и исправлений следите за репозиторием: [github.com/nekit508/mindustry-mod-plugin](https://github.com/nekit508/mindustry-mod-plugin).
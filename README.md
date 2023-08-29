# Android Pdf Web Viewer

Android library for opening pdf files using PDF.js with the swipe book feature with turn.js and jquery.

## Getting Started

1. Add the jitpack repository to your build file
**Groovy**
```groovy
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
**Kotlin DSL**
```kotlin
dependencyResolutionManagement {
    ...
    repositories {
        ...
        maven { url = URI.create("https://jitpack.io") }
    }
}
```
2. Add the dependency
**Groovy**
```groovy
	dependencies {
	        implementation 'com.github.azisanw19:android-pdf-web-viewer:Tag'
	}
```
**Kotlin DSL**
```kotlin
    dependencies {
        implementation("com.github.azisanw19:android-pdf-web-viewer:Tag")
    }
```

## Documentation

See the [documentation](https://github.com/azisanw19/android-pdf-web-viewer/wiki) for examples and general use of Sweet Dialog.

## Contributing

Please open an issue first before making a pull request.

## License

This project is released under [Apache License](https://github.com/azisanw19/android-pdf-web-viewer/blob/main/LICENSE) however some code come from external with following licenses:

* jQuery released under [MIT License](https://github.com/jquery/jquery/blob/master/LICENSE.txt)
* PDF.js released under [Apache License](https://github.com/mozilla/pdf.js/blob/master/LICENSE)
* turn.js released under [BSD License](https://github.com/blasten/turn.js/blob/master/license.txt)
* pdf-viewer released under [MIT License](https://github.com/RaffaeleMorganti/pdf-viewer/blob/master/LICENSE)

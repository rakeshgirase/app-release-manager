# Google Play Store Publisher

This is a simple tool to upload android apk files to play store which is inspired from https://github.com/codebysd/java-play-store-uploader. 
Suitable for automation of play store uploads in a CI system.

## Requirements

1. Java (JRE) 8 or above

## Install

1. Download the latest jar file from [Releases Section](https://github.com/rakeshgirase/app-release-manager/releases).
2. Place it in root folder of your application. You can check in the file for Cloud build.
3. Use command java -jar release-manager-1.2.jar -key "key.json" -file "app.apk" -track "internal" -name "myApp" -notes "new release". (You can use this command in your cloud build config file)


## Usage

### 1. Setup Play Store

Ensure that the app is created on Play Store. Setup Play Store listing and other required information so that release management is enabled and new releases can be published. It is advised to do a manual upload and release at least once in the beginning.

### 2. Setup Release Tracks

Play Store allows to upload apps on release tracks like internal, alpha, beta and production. Enable and setup the track you want to use, on Play Store console.

### 3. Get Service Account Key

To access Play Store API a JSON key file is needed. 

i. Create Service Account:
https://developers.google.com/android/management/service-account
Download key.json:

ii. Go to https://console.developers.google.com/apis/credentials

iii. Select "Create credentials" > "Service Account key" and generate a new key for the Service that is associated to your Google Play service account.

### 4. Build apk or aab file

Build signed production android apk or aab file to upload. In case of a CI server, this file should be already generated.

### 4. Run Upload Command

Execute the binary, passing required data in arguments.
* APK File
    ```bash
     java -jar release-manager-1.3.jar -key "key.json" -file "app.apk" -track "internal" -name "myApp" -notes "new release"
    ```
* AAB File
    ```bash
     java -jar release-manager-1.3.jar -key "key.json" -file "app.aab" -track "internal" -name "myApp" -notes "new release" -name appName -packageName app.package.name
    ```

#### CLI Options

Running without any arguments will print available argument options.

```bash
Options:
 -file VAL          : The apk or aab file to publish
 -key VAL           : JSON key file of authorized service account
 -name VAL          : (optional) AndroidPublisher name on Play Store (defaults to
                      name in apk)
 -notes VAL         : (optional) Release notes
 -notesFile VAL     : (optional) Release notes from file
 -track VAL         : Release track to use. Eg. internal, alpha, beta or production
 -packageName VAL   : (optional for apk) App Package Name
 ```

## Development

To build:

```bash
mvn clean install
```

Pull requests and suggestions are welcome.

Happy with this module!

<a href="https://www.buymeacoffee.com/rakeshgirase" target="_blank"><img src="https://bmc-cdn.nyc3.digitaloceanspaces.com/BMC-button-images/custom_images/orange_img.png" alt="Buy Me A Coffee" style="height: auto !important;width: auto !important;"></a>

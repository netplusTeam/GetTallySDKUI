## Getting started

## Adding dependency: Add this dependency into your project build.gradle file

```gradle
dependencies {
    implementation 'com.github.netplusTeam:GetTallySDK:1.0.3'
}
```
While this is optional, it is recommended you update your compile and kotlin options to
```kotlin
compileOptions {
    sourceCompatibility JavaVersion.VERSION_17
    targetCompatibility JavaVersion.VERSION_17
}

kotlinOptions {
    jvmTarget = '17'
}
```
Once your project is syncronized, clean and rebuild to ensure everything is stable.

## Manifest: Add these to your manifest file(only if they don't already exisit)

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```
### Usage: To use the Tally SDK, simply add this to your button click listener or your desired view click listener

```kotlin
myButton.setOnClickListener{
    val email = "test@gmail.com"
    val password = "test1234"
    val intent = TallyActivity.getIntent(this, email, password)
    startActivity(intent)
}
```
Once this code above is added pass the reqiured parameters as requested from the SDK, for test purposes, we'll be using email and password. This code above launches the Tall SDK UI, which is overlayed on the app, from there users can interact.

### Card tokenization
When users input and tokenize their card information, the SDK provides you(the financial institution) access token an encrypted version of the card imformation in a String format. This encrypted information is stored crypotographically in the AndroidKey Store. To lean more about AndroidKey Store [Visit Android official documentation](https://developer.android.com/privacy-and-security/keystore). 
The SDK already handles all the complex tasks, so you need not to worry. Just add these lines or codes below and you're good to go. The SDK already handles encrypting and storing the cryptographic data.

### To READ the cryptographic information, add this line of code
```kotlin
 val tokenizedCardsData = TallSecurityUtil.retrieveData(this)
```
This line of code above reads from the AndroidKeyStore and returns all saved cryptographic data, decrypts it and returns the actual value to you.

### To DELETE the cryptographic information, add this line of code
```kotlin
TallSecurityUtil.deleteAllData(this)
```


### That's all you need to do.

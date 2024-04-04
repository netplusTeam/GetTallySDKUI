**Getting Started with Tally SDK: A Guide for Businesses and Developers**

### **Introduction to Tally SDK: Revolutionizing Financial Transactions in Your App**

In today's fast-paced digital world, financial transactions and data management require not only robust security but also seamless integration and user-friendly features. The Tally SDK is designed to cater to these needs, offering a suite of features that enhance your app's financial capabilities. This guide introduces the key features of Tally SDK, demonstrating how it can transform the way your app handles financial transactions and data.

### Key Features of Tally SDK

1. **Card Tokenization**: At the heart of secure financial transactions is card tokenization. This feature converts sensitive card information into a secure and encrypted token. This means that actual card details are never stored or transmitted in a way that could be exploited, significantly reducing the risk of fraud.

2. **View the Latest Tokenized Card**: Users can easily access and view the most recently tokenized card, making it convenient to manage and use their preferred payment method without navigating through their entire card portfolio.

3. **View All Tokenized Cards**: For users with multiple tokenized cards, this feature provides a comprehensive overview of all their cards in one place. It simplifies card management and enhances user experience by offering easy access to their entire tokenized card list.

4. **Save QR Code Image to User's Device Gallery**: This innovative feature allows users to save the QR code of their tokenized card directly to their device's gallery. It's a convenient way to store and retrieve QR codes for payments and transactions without the need for a physical card.

5. **View Transactions from a Single QR (Token)**: Users can view the transaction history associated with a specific QR code or token. This targeted insight into transactions offers a clear and detailed view of spending patterns and transaction details for individual tokens.

6. **View Transaction History from All Tokenized Cards**: For a broader overview, this feature allows users to access the transaction history across all their tokenized cards. It's an essential tool for tracking spending, managing finances, and reviewing transaction details across multiple cards.

7. **View All Merchants and Location on Google Maps**: Enhancing the user experience further, this feature integrates with Google Maps to display merchants and their locations. Users can easily find where their transactions are happening, discover new merchants, and plan their visits accordingly.

### Step 1: Setting Up
To get started, you need to add the Tally SDK to your project. This involves a simple modification to your project's build configuration.

- **Add the SDK**: Insert a line in your project's `build.gradle` file under dependencies:
  ```groovy
   implementation("com.github.netplusTeam:GetTallySDKUI:v1.2.0-beta")
  ```
- **Update Java and Kotlin Compatibility** (Recommended but optional): To ensure the best performance and compatibility, update your project to use Java Version 17 and set the Kotlin target to the same. This might involve adjusting your compile options.

### Step 2: Configuring Your App
Your app needs the right permissions to make full use of Tally SDK's capabilities. This includes access to the internet, network state, location, and external storage. Add the necessary permissions to your `AndroidManifest.xml` file, only if they're not already present.

### Step 3: Using the SDK
Integrating Tally SDK into your app's functionality is straightforward. For example, you can add it to a button in your app. When the user clicks this button, the SDK's user interface (UI) will launch, allowing them to interact with financial features securely. Here’s a simple setup to get you started:

```kotlin
myButton.setOnClickListener{
   val intent = TallyActivity.getIntent(this, email, password, fullName, bankName, phoneNumber, userId)
   startActivity(intent)
}
```
Replace the parameters with the necessary credentials. This action opens the Tally SDK UI, ready for user interaction.

### Card Tokenization
The Tally SDK offers a secure way to handle card information through tokenization. This process converts sensitive card details into a secure token, minimizing the risk of exposing actual card information.

- **Storing and Encrypting Card Data**: The SDK takes care of encrypting card information and storing it securely, leveraging Android's cryptographic storage solutions. You don't need to manage the complexities of encryption. To learn more about Android Keystore system visit [https://developer.android.com/privacy-and-security/keystore]
  
- **Reading Encrypted Data**: To access the stored and encrypted card information, use:
  ```kotlin
  val tokenizedCardsData = TallSecurityUtil.retrieveData(this)
  ```
  This command retrieves and decrypts the data for you.

- **Deleting Information**: If you need to clear stored data, simply call:
  ```kotlin
  TallSecurityUtil.deleteAllData(this)
  ```




## Flutter Setup
Open the Android module of the Flutter project and add the Android dependency following the steps above in the `buil.gradle` file.
In the `MainActivity` class or you can create/put it in your desired class but make sure it extends `FlutterActivity`. Follow this code below

```kotlin
class MainActivity: FlutterActivity() {
    private val CHANNEL = "com.fundall.gettallysdkui"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
                call, result ->
            if (call.method == "startTallyActivity") {
                val email = call.argument<String>("email")
                val fullName = call.argument<String>("fullName")
                val bankName = call.argument<String>("bankName")
                val phoneNumber = call.argument<String>("phoneNumber")
                val userId = call.argument<String>("userId")

                val intent = TallyActivity.getIntent(
                    this,
                    email,
                    fullName,
                    bankName,
                    phoneNumber,
                    userId)
                startActivity(intent)

                result.success("Tally Activity Started")
            } else {
                result.notImplemented()
            }
        }
    }
}

````
In the `Manifest` file add the line of code the `<application/>` 
```xml
tools:replace="android:name"
tools:overrideLibrary="com.netplus.qrenginui"
```

In your Dart(Flutter), add a Function that calls the Native function. We have created a `Dart` class for this use case. It's a simple screen that shows a button, feel free to use your own custome `UI`

```Dart
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: Text('Trigger SDK Example'),
        ),
        body: Center(
          child: SDKTriggerButton(),
        ),
      ),
    );
  }
}

class SDKTriggerButton extends StatelessWidget {
  // Define the method channel
  static const platform = MethodChannel('com.fundall.gettallysdkui');

  // Function to call the native method
  Future<void> triggerSdkFunction() async {
    try {
      final String result = await platform.invokeMethod('startTallyActivity', {
        // Pass any required arguments by your native method
        "email": "email@example.com",
        "password": "password",
        "fullName": "John Doe",
        // Add other parameters as needed
      });
      print(result); // Success result from native code
    } on PlatformException catch (e) {
      print("Failed to invoke the method: '${e.message}'.");
    }
  }

  @override
  Widget build(BuildContext context) {
    return ElevatedButton(
      onPressed: () {
        triggerSdkFunction(); // Call this function when the button is pressed
      },
      child: Text('Trigger SDK'),
    );
  }
}
````
### Conclusion
Integrating Tally SDK into your app not only enhances its financial capabilities but does so with an emphasis on security and ease of use. By following the steps outlined above, developers and business stakeholders alike can ensure a smooth implementation process, bringing sophisticated financial transaction features to your users with minimal hassle. Whether you’re enhancing an existing application or building a new one, Tally SDK provides the tools you need to succeed.

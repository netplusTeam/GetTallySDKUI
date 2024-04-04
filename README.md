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
  buildFeatures {
        dataBinding = true
        viewBinding = true
    }
  
   implementation("com.github.netplusTeam:GetTallySDKUI:v1.2.1-beta")
   implementation 'com.github.netplusTeam:GetTallySDK:v1.1.0-beta'
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
import com.netplus.qrengine.utils.TallSecurityUtil
import com.netplus.qrenginui.activities.TallyActivity
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel

/**
 * MainActivity class responsible for configuring the Flutter engine and handling method channel calls.
 * This class integrates native Kotlin code with Flutter using method channels to trigger native SDK functionality.
 */
class MainActivity : FlutterActivity() {
    // Define the method channel identifier
    private val CHANNEL = "com.fundall.gettallysdkui"

    /**
     * Configure the Flutter engine to handle method channel calls and event channel stream.
     * Method channel is used to trigger the native SDK activity.
     * Event channel is used to retrieve data from the native side.
     */
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        // Setup method channel to handle method calls from Flutter
        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL
        ).setMethodCallHandler { call, result ->
            if (call.method == "startTallyActivity") {
                // Extract arguments from the method call
                val email = call.argument<String>("email").toString()
                val fullName = call.argument<String>("fullName").toString()
                val bankName = call.argument<String>("bankName").toString()
                val phoneNumber = call.argument<String>("phoneNumber").toString()
                val userId = call.argument<String>("userId").toString()

                // Start the TallyActivity using the extracted arguments
                val intent = TallyActivity.getIntent(
                    this,
                    email,
                    fullName,
                    bankName,
                    phoneNumber,
                    userId
                )
                startActivity(intent)
                result.success("Tally Activity Started") // Return success result to Flutter
            } else {
                result.notImplemented() // Handle method calls not implemented
            }
        }

        // Setup event channel to retrieve data from the native side
        val eventChannel = EventChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            "com.netplus.qrengine.tallysdk/tokenizedCardsData"
        )
        eventChannel.setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                // Retrieve data from TallSecurityUtil and send it to Flutter
                val data = TallSecurityUtil.retrieveData(this@MainActivity)
                events?.success(data)
                println(data ?: "There's no saved data") // Print retrieved data (if any)
            }

            override fun onCancel(arguments: Any?) {
                // Handle cancellation if needed
            }
        })
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
/// This is an example Flutter application demonstrating how to trigger
/// and receive data from a native SDK using platform channels.
/// 
/// The application consists of a single screen with a button that, when pressed,
/// triggers a native SDK function. The data returned from the native side
/// is then displayed on the screen.
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
        body: SDKTriggerButton(), // Display the SDK trigger button and the result
      ),
    );
  }
}

/// This widget represents a button that triggers the native SDK function
/// and displays the data returned from the native side.
class SDKTriggerButton extends StatelessWidget {
  // Define the method channel for communication between Flutter and native code
  static const platform = MethodChannel('com.fundall.gettallysdkui');
  static const EventChannel _eventChannel = EventChannel('com.netplus.qrengine.tallysdk/tokenizedCardsData');

  /// Function to trigger the native SDK function
  Future<void> triggerSdkFunction() async {
    try {
      // Invoke the native method to start the SDK activity
      final String result = await platform.invokeMethod('startTallyActivity', {
        // Pass any required arguments to the native method
        "email": "email@example.com",
        "fullName": "John Doe",
        "bankName": "GTBank",
        "phoneNumber": "000000000",
        "userId": "00",
      });
      print(result); // Print success result from native code
    } on PlatformException catch (e) {
      print("Failed to invoke the method: '${e.message}'.");
    }
  }

  /// Stream to listen for data returned from the native side
  static Stream<dynamic> get tokenizedCardsDataStream {
    return _eventChannel.receiveBroadcastStream();
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

//To listen to the braodcaster, update your code and add this block

StreamBuilder<dynamic>(
        stream: TallySDK.tokenizedCardsDataStream,
        builder: (context, snapshot) {
          if (snapshot.hasData) {
            // Process and display data received from Kotlin
            return Center(
              child: Text('Data from Kotlin: ${snapshot.data}'),
            );
          } else if (snapshot.hasError) {
            // Handle error
            return Center(
              child: Text('Error: ${snapshot.error}'),
            );
          } else {
            // Loading indicator while waiting for data
            return Center(
              child: CircularProgressIndicator(),
            );
          }
        },
      ),
    );

````
### Conclusion
Integrating Tally SDK into your app not only enhances its financial capabilities but does so with an emphasis on security and ease of use. By following the steps outlined above, developers and business stakeholders alike can ensure a smooth implementation process, bringing sophisticated financial transaction features to your users with minimal hassle. Whether you’re enhancing an existing application or building a new one, Tally SDK provides the tools you need to succeed.

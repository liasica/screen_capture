import 'package:flutter/material.dart';
import 'package:screen_capture/screen_capture.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _screenCapturePlugin = ScreenCapture();

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: [
              MaterialButton(
                child: const Text('Request Permission'),
                onPressed: () async {
                  final result = await _screenCapturePlugin.requestPermission();
                  print(result);
                },
              ),
              MaterialButton(
                child: const Text('Take Capture'),
                onPressed: () async {
                  final result = await _screenCapturePlugin.takeCapture();
                  print(result);
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}

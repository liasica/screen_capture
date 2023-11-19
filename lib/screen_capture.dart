import 'dart:typed_data';

import 'screen_capture_platform_interface.dart';

class ScreenCapture {
  Future<bool?> requestPermission() {
    return ScreenCapturePlatform.instance.requestPermission();
  }

  Future<Uint8List?> takeCapture() {
    return ScreenCapturePlatform.instance.takeCapture();
  }
}

import 'dart:typed_data';

import 'screen_capture_platform_interface.dart';

class ScreenCapture {
  Future<bool?> requestPermission() {
    return ScreenCapturePlatform.instance.requestPermission();
  }

  Future<Uint8List?> takeCapture({required int x, required int y, required int width, required int height}) {
    return ScreenCapturePlatform.instance.takeCapture(x: x, y: y, width: width, height: height);
  }
}

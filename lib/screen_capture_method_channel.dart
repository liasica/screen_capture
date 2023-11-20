import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'screen_capture_platform_interface.dart';

/// An implementation of [ScreenCapturePlatform] that uses method channels.
class MethodChannelScreenCapture extends ScreenCapturePlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('screen_capture');

  @override
  Future<bool?> requestPermission() async {
    return await methodChannel.invokeMethod<bool?>('requestPermission');
  }

  @override
  Future<Uint8List?> takeCapture({required int x, required int y, required int width, required int height}) async {
    return await methodChannel.invokeMethod<Uint8List?>('takeCapture', {
      'x': x,
      'y': y,
      'width': width,
      'height': height,
    });
  }
}

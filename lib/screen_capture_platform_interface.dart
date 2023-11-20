import 'dart:typed_data';

import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'screen_capture_method_channel.dart';

abstract class ScreenCapturePlatform extends PlatformInterface {
  /// Constructs a ScreenCapturePlatform.
  ScreenCapturePlatform() : super(token: _token);

  static final Object _token = Object();

  static ScreenCapturePlatform _instance = MethodChannelScreenCapture();

  /// The default instance of [ScreenCapturePlatform] to use.
  ///
  /// Defaults to [MethodChannelScreenCapture].
  static ScreenCapturePlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [ScreenCapturePlatform] when
  /// they register themselves.
  static set instance(ScreenCapturePlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<bool?> requestPermission() {
    throw UnimplementedError('requestPermission() has not been implemented.');
  }

  Future<Uint8List?> takeCapture({required int x, required int y, required int width, required int height}) {
    throw UnimplementedError('takeCapture() has not been implemented.');
  }
}

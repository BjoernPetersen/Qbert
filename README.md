# Qbert

MusicBot implementation for Android.

**Note:** This project is in a very early stage and using it is not recommended yet.
Most parts are slight adaptations of the MusicBot-desktop implementation and will be gradually
replaced with more appropriate solutions over time.

## Limitations

Qbert only supports plugins which have been discovered at compile-time because the `ServiceLoader`
mechanism used by MusicBot is not supported by Android.
This limitation might be alleviated in the future by using a different plugin discovery mechanism.

## License

This project is released under the Apache 2.0 License. That includes every file in this repository,
unless explicitly stated otherwise at the top of a file.
A copy of the license text can be found in the [LICENSE file](LICENSE).

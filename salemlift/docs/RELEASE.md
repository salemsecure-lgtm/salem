# RELEASE.md — building and signing Salem Lift

## Signed release APK

```sh
./gradlew :app:assembleRelease   # → app/build/outputs/apk/release/app-release.apk
./gradlew verifyOffline          # fails if any network permission/client sneaks in
```

## Signing keys

Release signing reads `keystore.properties` at the repo's `salemlift/` root
(git-ignored):

```properties
storeFile=/absolute/path/to/your.keystore
storePassword=…
keyAlias=…
keyPassword=…
```

Without that file, the build falls back to `salemlift/release.keystore` with
well-known dev credentials (`salemlift` / `salemlift-dev`) — good enough for
personal sideloading, **not** for distribution. Generate your own once and
keep it safe (losing it means users must uninstall/reinstall to update):

```sh
keytool -genkeypair -v -keystore my-release.keystore -alias salemlift \
  -keyalg RSA -keysize 2048 -validity 10950
```

Keystores are never committed (`.gitignore` covers `*.keystore`, `*.jks`,
`keystore.properties`).

## What `verifyOffline` enforces (runs on every `check`)

1. The merged **release manifest** contains neither `INTERNET` nor
   `ACCESS_NETWORK_STATE`.
2. The **release runtime classpath** contains no networking client
   (okhttp/retrofit/ktor-client/volley/grpc/cronet/apache-http).

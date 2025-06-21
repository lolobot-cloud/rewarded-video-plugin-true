// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "DouslitereRewardedVideoPluginTrue",
    platforms: [.iOS(.v14)],
    products: [
        .library(
            name: "DouslitereRewardedVideoPluginTrue",
            targets: ["RewardedVideoPluginPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "7.0.0")
    ],
    targets: [
        .target(
            name: "RewardedVideoPluginPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/RewardedVideoPluginPlugin"),
        .testTarget(
            name: "RewardedVideoPluginPluginTests",
            dependencies: ["RewardedVideoPluginPlugin"],
            path: "ios/Tests/RewardedVideoPluginPluginTests")
    ]
)
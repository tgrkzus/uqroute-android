# UQRoute for Android
The Android app for UQRoute. UQRoute is a mapping service for University of Queensland campuses that provides turn-by-turn routing to various buildings and classes.
UQRoute uses [OpenStreetMaps](https://www.openstreetmap.org/) data which is often much more accurate and detailed than Google Maps. 
The Mapzen SDK provides rendering, routing and location services through it's implementation and various dependencies.

# Build Instructions
Assuming you have Android Studio installed and setup, first clone this repo. To protect API keys and signing passwords you will need to the following:

1. Create a ```keys.properties``` file in the ```app/src/main/res/``` directory (Android Studio can do this for you):
2. Add the following constants to the properties file: (e.g. ```MAPZEN_KEY_DEV = mapzen-XXXXXX```):
3. Mapzen keys: ```MAPZEN_KEY_DEV```, ```MAPZEN_KEY_PROD```:
 * You will need to get these from the [Mapzen website](https://mapzen.com), generating keys is free with rather generous limits. All you need to do is login with your Github account. These keys are neccessary to draw the map and provide routing services
4. Signing keys: ```KEY_PASS```, ```STORE_FILE```, ```STORE_PASS```:
 * These are mostly unnecessary unless you want to generate signed APKs, google how to do this. If you're interested otherwise just modify the gradle file to not require these constants.
5. Build with Android Studio (release/debug)

 After you've defined these constants Android studio should have no issues building and running the project. If any of these instructions are unclear/wrong feel free to make an issue.
 
 
# Helpful links
* [Mapzen Android SDK](https://github.com/mapzen/android)

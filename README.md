# Temi Data Rating

# connecting to temi (follow the instruction on https://github.com/robotemi/sdk/wiki/Installing-and-Uninstalling-temi-Applications)
1. connect to the same network as temi
2. on temi - go to Settings -> Temi Developer Tools -> tap on ADB Port Opening.
3. curl -k "https://TEMI_IP_ADDRESS:TEMI_PORT/grantAuth?pwd=PASS"
4. adb connect <IP_ADDRESS>:<PORT>
5. adb start-server
6. open this folder/project on android studio

## Building

First you need to setup your environment, in order to connect with Temi. You can follow the instructions under the official repository: [Temi SDK](https://github.com/robotemi/sdk/wiki/Installing-and-Uninstalling-temi-Applications)
  
After that, you are able to run the application using Android Studio or directly from the command line.

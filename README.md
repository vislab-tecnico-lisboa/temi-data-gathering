# Temi Data Rating

# connecting to temi (follow the instruction on https://github.com/robotemi/sdk/wiki/Installing-and-Uninstalling-temi-Applications)
1. connect to the same network as temi
2. on temi - go to Settings -> Temi Developer Tools -> tap on ADB Port Opening.
3. curl -k "https://TEMI_IP_ADDRESS:TEMI_PORT/grantAuth?pwd=PASS"
4. adb connect <IP_ADDRESS>:<PORT>
5. adb start-server
6. open this folder/project on android studio

## Structure

* `build.gradle` - root gradle config file
* `settings.gradle` - root gradle settings file
* `app` - our only project in this repo
* `app/build.gradle` - project gradle config file
* `app/src` - main project source directory
* `app/src/main` - main project flavour
* `app/src/main/AndroidManifest.xml` - manifest file
* `app/src/main/java` - java source directory
* `app/src/main/res` - resources directory

## Building

First you need to setup your environment, in order to connect with Temi. You can follow the instructions under the official repository: [Temi SDK](https://github.com/robotemi/sdk/wiki/Installing-and-Uninstalling-temi-Applications)
  
After that, you are able to run the application using Android Studio or directly from the command line.

#### Clean

	gradle clean

#### Test

Were you to add automated java tests, you could configure them in your
`build.gradle` file and run them within gradle as well.

	gradle test

## Further reading

* [Build System Overview](https://developer.android.com/sdk/installing/studio-build.html)
* [Gradle Plugin User Guide](http://tools.android.com/tech-docs/new-build-system/user-guide)
* [Gradle Plugin Release Notes](http://tools.android.com/tech-docs/new-build-system)

## Author

| Name              | University                 |                                                                                                                                                                                                                                                                                                                                                             More info |
|:------------------|----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| Tiago Fonseca | Instituto Superior Técnico |     [<img src="https://i.ibb.co/brG8fnX/mail-6.png" width="17">](mailto:tiagoatfonseca@tecnico.ulisboa.pt "tiagoatfonseca@tecnico.ulisboa.pt") [<img src="https://github.githubassets.com/favicon.ico" width="17">](https://github.com/TiagoFonseca99 "TiagoFonseca99") [<img src="https://i.ibb.co/TvQPw7N/linkedin-logo.png" width="17">](https://www.linkedin.com/in/tiago-fonseca-167275197/ "tiagofonseca") |

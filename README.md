EKI Speak
=========

Implementation of Android's TextToSpeechService that provides Estonian text-to-speech.

Modification of the source code downloaded on 2018-04-22 from
<https://www.eki.ee/heli/index.php?option=com_content&view=article&id=5&Itemid=466>
(<https://www.eki.ee/heli/images/koduleht/EKIsynt2.zip>).

Compilation
-----------

Create the file `local.properties` that points to the location of the Android SDK
and NDK, e.g.

    ndk.dir=/home/kaarel/myapps/android-sdk/ndk-bundle
    sdk.dir=/home/kaarel/myapps/android-sdk

Run Gradle, e.g.

    gradle installDebug

or (in case you have set up release keys) e.g.

    gradle aR
    gradle deploy

License
-------

<http://www.eki.ee/eki/litsents.html>

See also
--------

- another modification of the original EKI code: <https://play.google.com/store/apps/details?id=ee.eki.heli.EKISpeak>

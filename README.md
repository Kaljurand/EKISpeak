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

Usage examples
--------------

- Download an epub-formatted book from [Digar](https://www.digar.ee) and listen to it with an TTS-supporting e-reader, e.g. [eReader Prestigio](https://play.google.com/store/apps/details?id=com.prestigio.ereader)
- Listen to newspaper articles (and other webpages) using [Pocket](https://play.google.com/store/apps/details?id=com.ideashower.readitlater.pro)
- Make a dialog system using [Kõnele](http://kaljurand.github.io/K6nele/), e.g. try the [Prompt rewrite rule application](https://docs.google.com/spreadsheets/d/1ViO4swIovvuRJC-kiPaQOIdAkuwHCbQvTQlNUwaAoJQ/edit#gid=0) that repeats the spoken input text with a TTS engine
- Get an audible confirmation to executed commands in [Arvutaja](http://kaljurand.github.io/Arvutaja/)

See also
--------

- original EKI TTS app on Google Play: <https://play.google.com/store/apps/details?id=ee.eki.ekisynt>
- another modification of the (previous version of the) original EKI code: <https://play.google.com/store/apps/details?id=ee.eki.heli.EKISpeak>

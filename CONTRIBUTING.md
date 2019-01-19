Contributing
============

Introduction
------------

Contribute via [GitHub pull requests](https://help.github.com/articles/about-pull-requests/).


TODO
----

- document relationship to Vabamorf + automatically sync Vabamorf
- document relationship to <https://github.com/ikiissel/synthts_et> + automatically sync its changes
- document relationship to Elisa Raamat Iselugeja
- document input format, e.g. which preprocessing on text is safe (e.g. can one replace numbers with words without affecting morf. analysis)
- implement a separate preprocessing step (after morph. analysis), that correctly handles numerical expressions
- better handling of foreign text
- better handling of proper names, acronyms, abbreviations
- sometimes "off tilde" is spoken at the end of the utterance
- increase volume
- add support for Võro
- allow voices to be removed/added
- allow other resources (e.g. Vabamorf lexica) to be changed
- allow different voices to be selected in a external app, e.g. Pocket
- clarify how long input is allowed (seems to be lower than TextToSpeech.getMaxSpeechInputLength(), which is 4000)

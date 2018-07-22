package com.autodidacte;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class GameEngine {

    public enum GameType
    {
        eTrouverLettre,
        eTrouver1ereLettre,
        eTrouverMot;
    }

    public interface InitCallback
    {
        void execute();
    }

    static Hashtable<Integer, ArrayList<String>> _notationWord;
    static Hashtable<Integer, ArrayList<Character>> _notationLetter;
    static Hashtable<Integer, ArrayList<Character>> _notationFirstLetter;
    static private int _currentLevel = 0;
    static private int _currentLetterLevel = 0;
    static private int _currentWordIndex = -1;
    static private int _currentLetterIndex = -1;
    static private int _currentFirstLetterIndex = -1;
    static private int _currentFirstLetterLevel = 0;
    static private TextToSpeech tts;
    static private OcrDetectorProcessor _detector = null;
    static private GameType _gameType;
    static boolean _firstTime = true;
    static boolean _onReturnBack = false;
    private static boolean _onTap = false;
    static int MAX_LEVEL = 3;
    static int SUCCESS_ACTIVITY = 1;
    static Hashtable<Character, String> _letterToWord;
    static Hashtable<String, Character> _WordToLetter;
    static Activity _ocrCaptureActivity = null;
    static Activity _questionActivity = null;
    static boolean _init = false;
    static InitCallback _initCallback;



    public static void init(Activity questionActivity)
    {
        if(!_init) {
            _questionActivity = questionActivity;
            _currentWordIndex = -1;
            _currentLetterIndex = -1;
            _currentLevel = 0;
            // Set good defaults for capturing text.
            boolean autoFocus = true;
            boolean useFlash = true;

            SharedPreferences prefs = questionActivity.getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();


            // TODO: Set up the Text To Speech engine.
            TextToSpeech.OnInitListener listener =
                    new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(final int status) {
                            if (status == TextToSpeech.SUCCESS) {
                                Log.d("TTS", "Text to speech engine started successfully.");
                                tts.setLanguage(Locale.FRANCE);
                                _init = true;
                                if(_initCallback != null)
                                    _initCallback.execute();
                            } else {
                                Log.d("TTS", "Error starting the text to speech engine.");
                            }
                        }
                    };
            tts = new TextToSpeech(questionActivity.getApplicationContext(), listener);

            if (_letterToWord == null)
                _letterToWord = new Hashtable<Character, String>();

            if (_letterToWord.size() == 0) {
                initLetterToWord();
            }

            initNotationWords();
            initNotationLetter(_notationLetter);
            initNotationLetter(_notationFirstLetter);
            _firstTime = true;

            AudioManager audioManager = (AudioManager) _questionActivity.getSystemService(Context.AUDIO_SERVICE);
            int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max * 3 / 2, AudioManager.FLAG_SHOW_UI);
        }
    }

    public static void setInitCallback(InitCallback callback)
    {
        _initCallback = callback;
    }

    public static boolean isInit()
    {
        return _init;
    }

    public static void setOcrDetector(OcrDetectorProcessor detector)
    {
        _detector = detector;
    }

    public static void launchOcrCapture()
    {
        Intent ocrCaptureActivity = new Intent(_questionActivity, OcrCaptureActivity2.class);
        _questionActivity.startActivity(ocrCaptureActivity);
    }

    public static void setGameType(GameType type)
    {
        _gameType = type;
    }

    public static GameType getGameType()
    {
        return _gameType;
    }


    public static void onTapChange(boolean onTap)
    {
        _onTap = onTap;
    }


    private static void initLetterToWord()
    {
        _letterToWord.put('a', "avion");
        _letterToWord.put('b', "banane");
        _letterToWord.put('c', "carotte");
        _letterToWord.put('d', "dinosaure");
        _letterToWord.put('e', "éléphant");
        _letterToWord.put('f', "fraise");
        _letterToWord.put('g', "girafe");
        _letterToWord.put('h', "hérisson");
        _letterToWord.put('i', "igloo");
        _letterToWord.put('j', "jus");
        _letterToWord.put('k', "kangourou");
        _letterToWord.put('l', "lion");
        _letterToWord.put('m', "mangue");
        _letterToWord.put('n', "nuage");
        _letterToWord.put('o', "orange");
        _letterToWord.put('p', "pomme");
        _letterToWord.put('q', "quatre");
        _letterToWord.put('r', "robot");
        _letterToWord.put('s', "soleil");
        _letterToWord.put('t', "tigre");
        _letterToWord.put('u', "ustensiles");
        _letterToWord.put('v', "voiture");
        _letterToWord.put('w', "wagon");
        _letterToWord.put('x', "xylophone");
        _letterToWord.put('y', "yaourt");
        _letterToWord.put('z', "zèbre");
    }

    private static void initWordToLetter()
    {
        _WordToLetter.put("avion", 'a');
        _WordToLetter.put("banane", 'b');
        _WordToLetter.put("carotte", 'c');
        _WordToLetter.put("dinosaure", 'd');
        _WordToLetter.put("éléphant", 'e');
        _WordToLetter.put("fraise", 'f');
        _WordToLetter.put("girafe", 'g');
        _WordToLetter.put("hérisson", 'h');
        _WordToLetter.put("igloo", 'i');
        _WordToLetter.put("jus", 'j');
        _WordToLetter.put("kangourou", 'k');
        _WordToLetter.put("lion", 'l');
        _WordToLetter.put("mangue", 'm');
        _WordToLetter.put("nuage", 'n');
        _WordToLetter.put("orange", 'o');
        _WordToLetter.put("pomme", 'p');
        _WordToLetter.put("quatre", 'q');
        _WordToLetter.put("robot", 'r');
        _WordToLetter.put("soleil", 's');
        _WordToLetter.put("tigre", 't');
        _WordToLetter.put("ustensiles", 'u');
        _WordToLetter.put("voiture", 'v');
        _WordToLetter.put("wagon", 'w');
        _WordToLetter.put("xylophone", 'x');
        _WordToLetter.put("yaourt", 'y');
        _WordToLetter.put("zèbre", 'z');
    }


    private static void initNotationWords()
    {
        SharedPreferences preferences = _questionActivity.getPreferences(MODE_PRIVATE);
        String init = preferences.getString("init", null);
        Iterator<Character> it = _letterToWord.keySet().iterator();
        if(init == null){
            SharedPreferences.Editor editor = preferences.edit();

            while(it.hasNext()) {
                String value = _letterToWord.get(it.next());
                editor.putString(value, "0");
            }

            editor.putString("init", "init");
            editor.apply();
        }

        _notationWord = new Hashtable<Integer, ArrayList<String>>();

        it = _letterToWord.keySet().iterator();
        while(it.hasNext()) {
            String value = _letterToWord.get(it.next());
            String level = preferences.getString(value, "");
            int nLevel = Integer.parseInt(level);
            if(_notationWord.containsKey(nLevel) == false)
                _notationWord.put(nLevel, new ArrayList<String>());
            _notationWord.get(nLevel).add(value);
        }
    }

    private static void initNotationLetter(Hashtable<Integer, ArrayList<Character>> letters)
    {
        letters = new Hashtable<Integer, ArrayList<Character>>();
        SharedPreferences preferences = _questionActivity.getPreferences(MODE_PRIVATE);
        String init = preferences.getString("iniLetters", null);
        if(init == null){
            SharedPreferences.Editor editor = preferences.edit();

            for(int i = 0; i < 26; i++) {
                char letter = (char)('A' + i);
                String sLetter = String.valueOf(letter);
                editor.putString(sLetter, "0");
                letter = (char)('a' + i);
                String letterMinus = String.valueOf(letter);
                editor.putString(letterMinus, "0");
            }
            editor.putString("initLetter", "init");
            editor.apply();
        }
        initNotationLetterArray('a', letters);
        initNotationLetterArray('A', letters);
    }


    static void initNotationLetterArray(Character firstChar, Hashtable<Integer, ArrayList<Character>> letters)
    {
        SharedPreferences preferences = _questionActivity.getPreferences(MODE_PRIVATE);
        for(int i = 0; i < 26; i++) {
            char letter = (char)(i + firstChar);
            String value = String.valueOf(letter);
            String level = preferences.getString(value, "");
            int nLevel = Integer.parseInt(level);
            if(letters.containsKey(nLevel) == false)
                letters.put(nLevel, new ArrayList<Character>());
            letters.get(nLevel).add(letter);
        }
    }

    public static String wordFromLetter(char c)
    {
        if(c < 'a')
            c = (char)(c + ('a' - 'A'));
        if(_letterToWord.containsKey(c))
            return _letterToWord.get(c);
        return "";
    }

    private static String nextItem()
    {
        if(_gameType == GameType.eTrouverMot) {
            _currentWordIndex++;
            if (_currentWordIndex >= _notationWord.get(_currentLevel).size()) {
                do {
                    _currentLevel++;
                }
                while (_notationWord.get(_currentLevel).size() == 0 || _currentLevel < MAX_LEVEL);
                _currentWordIndex = 0;
            }
        }
        else if(_gameType == GameType.eTrouverLettre) {
            _currentLetterIndex++;
            if (_currentLetterIndex >= _notationLetter.get(_currentLetterLevel).size()) {
                do {
                    _currentLetterLevel++;
                }
                while (_notationWord.get(_currentLetterLevel).size() == 0 || _currentLetterLevel < MAX_LEVEL);
                _currentLetterIndex = 0;
            }
        }
        else if(_gameType == GameType.eTrouver1ereLettre) {
            _currentFirstLetterIndex++;
            if (_currentFirstLetterIndex >= _notationFirstLetter.get(_currentFirstLetterLevel).size()) {
                do {
                    _currentFirstLetterLevel++;
                }
                while (_notationFirstLetter.get(_currentFirstLetterLevel).size() == 0 || _currentFirstLetterLevel < MAX_LEVEL);
                _currentFirstLetterIndex = 0;
            }
        }
        return currentItem();
    }

    public static String currentItem()
    {
        if(_gameType == GameType.eTrouverMot)
            return  _notationWord.get(_currentLevel).get(_currentWordIndex).substring(0, 1);
        else if(_gameType == GameType.eTrouverLettre)
            return _notationLetter.get(_currentLetterLevel).get(_currentLetterIndex).toString();
        else if(_gameType == GameType.eTrouver1ereLettre)
            return _notationFirstLetter.get(_currentFirstLetterLevel).get(_currentFirstLetterIndex).toString();
        return "";
    }

    public static void askNextItem()
    {
        tts.setLanguage(Locale.FRANCE);
        String c;
        if(!_onTap){
            if(_detector != null)
                _detector._waitingForDetection = false;
            c = nextItem();
        }
        else
            c = currentItem();

        tts.setSpeechRate(0.8f);
        String sentence = "";

        if(_gameType == GameType.eTrouverMot) {
            if (_firstTime) {
                sentence = "Bonjour, quel mot commence par la lettre, " + c;
                _firstTime = false;
            } else if (_onTap)
                sentence += "Quel mot commence par la lettre, " + c;
            else
                sentence = "Lettre suivante, quel mot commence par, " + c;
            if(c == "y" || c == "Y")
                sentence += " grec ";
        }
        else if(_gameType == GameType.eTrouverLettre){
            if (_firstTime) {
                sentence = "Bonjour, trouve moi la lettre, " + c;
                if(c == "y" || c == "Y")
                    sentence += " grec ";
                if(isLow(c.charAt(0)))
                    sentence += " minuscule";
                else
                    sentence += " majuscule";
                _firstTime = false;
            } else if (_onTap)
                sentence = "Trouve moi la lettre, " + c;
            else
                sentence = "Lettre suivante, trouve moi la lettre, " + c + " ?";
        }
        else if(_gameType == GameType.eTrouver1ereLettre){
            if (_firstTime) {
                sentence = "Bonjour, par quelle lettre commence le mot " + c;
                if(c == "y" || c == "Y")
                    sentence += " grec ";
                if(isLow(c.charAt(0)))
                    sentence += " minuscule";
                else
                    sentence += " majuscule";
                _firstTime = false;
            } else if (_onTap)
                sentence = "Trouve moi la lettre, " + c;
            else
                sentence = "Lettre suivante, trouve moi la lettre, " + c + " ?";
        }

        tts.speak(sentence, TextToSpeech.QUEUE_ADD, null, "DEFAULT");
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                int test = 0;
                test = test;
            }

            @Override
            public void onDone(String utteranceId) {
                int test = 0;
                test = test;
            }

            @Override
            public void onError(String utteranceId) {
                int test = 0;
                test = test;
            }
        });

        if (!_onTap && !_onReturnBack) {
            Utils.Sleep(4000);
            if(_detector != null)
                _detector._waitingForDetection = true;
        }
    }


    public static void onSuccess(String wordFound)
    {
        boolean version1 = false;
        boolean version2 = false;
        boolean version3 = true;
        SharedPreferences prefs = _questionActivity.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String level = prefs.getString(wordFound, "");

        Integer nLevel = Integer.parseInt(level) + 1;
        level = Integer.toString(nLevel);
        editor.putString(wordFound, level);
        editor.commit();
        String test = prefs.getString(wordFound, "");

        String sentence = "Bravo, " + wordFound + " commence bien par " + currentItem() + " ! ";
        Intent successActivity = new Intent(_questionActivity, SuccessActivity.class);
        successActivity.putExtra("sentence", sentence);
        successActivity.putExtra("result", "success");
        _questionActivity.startActivityForResult(successActivity, SUCCESS_ACTIVITY);
    }

    static boolean isLow(char c)
    {
        return c >= 'a';
    }

    public static void onFail(String wrongWord, String ExpectedWord)
    {
        if(_detector != null)
        _detector._waitingForDetection = false;
        String sentence = "Tu tes trompé, " + wrongWord + " ne commence pas par, " + currentItem() +
                ", le bon mot etait " + ExpectedWord;
        Intent successActivity = new Intent(_questionActivity, SuccessActivity.class);
        successActivity.putExtra("sentence", sentence);
        successActivity.putExtra("result", "error");
        _questionActivity.startActivityForResult(successActivity, SUCCESS_ACTIVITY);
    }

}

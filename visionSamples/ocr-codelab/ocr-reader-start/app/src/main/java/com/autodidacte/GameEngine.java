package com.autodidacte;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

public class GameEngine {

    public static int LETTRE_ACTIVITY = 1;
    public static int MOT_ACTIVITY = 2;
    public static int PREMIERE_LETTRE_ACTIVITY = 3;
    public static int QUESTION_ACTIVITY = 4;
    public static int SUCCESS_ACTIVITY = 5;
    public static int CAPTURE_ACTIVITY = 6;
    public static int CAPTURE_ACTIVITY2 = 7;
    public static int INIT_MISSING_LANGUAGE = 1;

    public enum GameType
    {
        eTrouverLettre,
        eTrouverPremiereLettre,
        eTrouverMot;
    }

    public interface InitTextToSpeechCallback
    {
        void execute(Activity activity);
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
    static public TextToSpeech tts;
    static private OcrDetectorProcessor _detector = null;
    static private GameType _gameType;
    static private boolean _firstTime = true;
    static boolean _onReturnBack = false;
    private static boolean _onTap = false;
    static int MAX_LEVEL = 3;
    static Hashtable<Character, String> _letterToWord;
    static Hashtable<String, Character> _WordToLetter;
    static Activity _ocrCaptureActivity = null;
    static Activity _questionActivity = null;
    static boolean _init = false;
    static InitTextToSpeechCallback _initCallback;
    static public boolean returnToAlphabetActvity = false;
    static public Activity _currentActivity = null;

    public static Locale currentLanguage()
    {
        return Locale.FRANCE;
    }


    public static void setFirstTime()
    {
        _firstTime = true;
    }


    public static void init(Activity questionActivity, InitTextToSpeechCallback initTextToSpeechCallback)
    {
        // TODO: Set up the Text To Speech engine.
        MainActivity.initTextToSpeech(questionActivity, initTextToSpeechCallback);

        _questionActivity = questionActivity;
        _currentWordIndex = -1;
        _currentLetterIndex = -1;
        _currentLevel = 0;

        SharedPreferences prefs = questionActivity.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (_letterToWord == null)
            _letterToWord = new Hashtable<Character, String>();

        if (_letterToWord.size() == 0) {
            initLetterToWord();
        }

        initNotationWords();
        _notationLetter = initNotationLetter();
        SharedPreferences preferences = _questionActivity.getPreferences(MODE_PRIVATE);
        String testInit = preferences.getString("initLetters", null);

        _notationFirstLetter = initNotationFirstLetter();
        _firstTime = true;

        Utils.setAudioVolume(0, _questionActivity);


    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == GameEngine.INIT_MISSING_LANGUAGE)
        {

        }
    }

    public static boolean isInit()
    {
        return _init;
    }

    public static void setOcrDetector(OcrDetectorProcessor detector)
    {
        _detector = detector;
    }

    public static void setOcrCaptureActivity(OcrCaptureActivity activity)
    {
        _ocrCaptureActivity = activity;
    }

    public static void launchOcrCapture()
    {
        Intent capture = null;
        capture = new Intent(_questionActivity, OcrCaptureActivity.class);
        _questionActivity.startActivityForResult(capture, CAPTURE_ACTIVITY);
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


    public static void initLetterToWord()
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

    private static Hashtable<Integer, ArrayList<Character>> initNotationLetter()
    {
        String initString = "initLetters";
        Hashtable<Integer, ArrayList<Character>> letters = new Hashtable<Integer, ArrayList<Character>>();
        SharedPreferences preferences = _questionActivity.getPreferences(MODE_PRIVATE);
        String init = preferences.getString(initString, null);
        if(init == null){
            SharedPreferences.Editor editor = preferences.edit();

            for(int i = 0; i < 26; i++) {
                char letter = (char)('A' + i);
                if(letter == 'l' || letter == 'I' || letter == 't' || letter == 'i' )
                    continue;
                String sLetter = String.valueOf(letter);
                editor.putString(sLetter, "0");
                letter = (char)('a' + i);
                String letterMinus = String.valueOf(letter);
                editor.putString(letterMinus, "0");
            }
            editor.putString(initString, "init");
            editor.apply();
        }
        initNotationLetterArray('a', letters);
        initNotationLetterArray('A', letters);
        return letters;
    }

    private static Hashtable<Integer, ArrayList<Character>> initNotationFirstLetter()
    {
        String initString = "initFirstLetters";
        Hashtable<Integer, ArrayList<Character>> letters = new Hashtable<Integer, ArrayList<Character>>();
        SharedPreferences preferences = _questionActivity.getPreferences(MODE_PRIVATE);
        String init = preferences.getString(initString, null);
        if(init == null){
            SharedPreferences.Editor editor = preferences.edit();

            for(int i = 0; i < 26; i++) {
                char letter = (char)('a' + i);
                if(letter == 'l' || letter == 'I' || letter == 't' || letter == 'i' )
                    continue;
                String sLetter = String.valueOf(letter) + "_prem";
                editor.putString(sLetter, "0");
            }
            editor.putString(initString, "init_prem");
            editor.apply();
        }

        //initNotationLetterArray('a', letters);
        for(int i = 0; i < 26; i++) {
            char letter = (char)(i + 'a');
            String value = String.valueOf(letter) + "_prem";
            String level = preferences.getString(value, "");
            if(level.length() > 0) {
                int nLevel = Integer.parseInt(level);
                if (letters.containsKey(nLevel) == false)
                    letters.put(nLevel, new ArrayList<Character>());
                letters.get(nLevel).add(letter);
            }
        }

        return letters;
    }


    static void initNotationLetterArray(Character firstChar, Hashtable<Integer, ArrayList<Character>> letters)
    {
        SharedPreferences preferences = _questionActivity.getPreferences(MODE_PRIVATE);
        for(int i = 0; i < 26; i++) {
            char letter = (char)(i + firstChar);
            String value = String.valueOf(letter);
            String level = preferences.getString(value, "");
            if(level.length() > 0) {
                int nLevel = Integer.parseInt(level);
                if (letters.containsKey(nLevel) == false)
                    letters.put(nLevel, new ArrayList<Character>());
                letters.get(nLevel).add(letter);
            }
        }
    }


    boolean isIntoWordList(String word)
    {
        return _letterToWord.contains(word);
    }


    public static String wordFromLetter(char c)
    {
        if(c < 'a')
            c = (char)(c + ('a' - 'A'));
        if(_letterToWord.containsKey(c))
            return _letterToWord.get(c);
        return "";
    }

    static void updateWordIndexIncrement()
    {
        _currentWordIndex++;
        if (_currentWordIndex >= _notationWord.get(_currentLevel).size()) {
            do {
                _currentLevel++;
            }
            while (_notationWord.get(_currentLevel).size() == 0 || _currentLevel < MAX_LEVEL);
            _currentWordIndex = 0;
        }
    }

    static void updateWordIndexRandom()
    {
        int max = _notationWord.get(_currentLevel).size();

        Random r = new Random();
        _currentWordIndex = r.nextInt(max + 1);
    }

    void updateLetterIndexIncrement()
    {
        _currentLetterIndex++;
        if(_notationLetter != null) {
            if (_currentLetterIndex >= _notationLetter.get(_currentLetterLevel).size()) {
                do {
                    _currentLetterLevel++;
                }
                while (_notationWord.get(_currentLetterLevel).size() == 0 || _currentLetterLevel < MAX_LEVEL);
                _currentLetterIndex = 0;
            }
        }
    }

    static void updateLetterIndexRandom()
    {
        int max = 0;
        while(max == 0) {
            max = _notationLetter.get(_currentLetterLevel++).size();
        }
        _currentLetterLevel--;

        Random r = new Random();
        _currentLetterIndex = r.nextInt(max + 1);
    }

    private static String nextItem()
    {
        if(_gameType == GameType.eTrouverMot) {
            updateWordIndexRandom();
        }
        else if(_gameType == GameType.eTrouverLettre) {
            updateLetterIndexRandom();
        }
        else if(_gameType == GameType.eTrouverPremiereLettre) {
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
        else if(_gameType == GameType.eTrouverPremiereLettre) {
            char c = _notationFirstLetter.get(_currentFirstLetterLevel).get(_currentFirstLetterIndex);
            String word = _letterToWord.get(c);
            return word;
        }
        return "";
    }

    public static void onTap()
    {
        _onTap = true;
        askNextItem();
        //_onTap = false;
    }

    public static void askNextItem()
    {
         int languageRet = tts.setLanguage(GameEngine.currentLanguage());
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
            String lettre = c;
            if(c == "y" || c == "Y")
                lettre += " grec ";
            if(isLow(c.charAt(0)))
                lettre += " minuscule";
            else
                lettre += " majuscule";
            if (_firstTime) {
                sentence = "Bonjour, trouve moi la lettre, " + lettre;
                _firstTime = false;
            } else if (_onTap)
                sentence = "Trouve moi la lettre, " + lettre;
            else
                sentence = "Lettre suivante, trouve moi la lettre, " + lettre + " ?";
        }
        else if(_gameType == GameType.eTrouverPremiereLettre){
            if (_firstTime) {
                sentence = "Bonjour, par quelle lettre commence le mot " + c;
                _firstTime = false;
            } else if (_onTap)
                sentence = "Trouve moi la premiere lettre du mot " + c;
            else
                sentence = "Suivant, par quelle lettre commence le mot " + c + " ?";
        }


        int speakRet = tts.speak(sentence, TextToSpeech.QUEUE_ADD, null, "DEFAULT");

        if(!_onTap) {
            int listenerRet = tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    int test = 0;
                    test = test;
                }

                @Override
                public void onDone(String utteranceId) {
                    if(!_onTap) {
                        if (_detector != null)
                            _detector._waitingForDetection = true;
                        Utils.Sleep(500);
                        launchOcrCapture();
                    }
                    else
                        _onTap = false;
                }

                @Override
                public void onError(String utteranceId) {
                    int test = 0;
                    test = test;
                }
            });
        }
     }

    public static void onLetterSuccess(char c)
    {
        String sentence = "Bravo, tu as trouvé la bonne lettre ! ";

        Intent ocrCaptureActivity = new Intent(_ocrCaptureActivity, SuccessActivity.class);
        ocrCaptureActivity.putExtra("sentence", sentence);
        ocrCaptureActivity.putExtra("result", "success");
        _ocrCaptureActivity.startActivityForResult(ocrCaptureActivity, SUCCESS_ACTIVITY);
    }


    public static void speak(String s)
    {
        tts.speak(s, TextToSpeech.QUEUE_ADD, null, "DEFAULT");
    }


    public static void onSuccess(String itemFound)
    {
        SharedPreferences prefs = _questionActivity.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String key = itemFound;
        if(getGameType() == GameType.eTrouverPremiereLettre)
            key = key.toLowerCase() + "_prem";

        String level = prefs.getString(key, "");

        Integer nLevel = Integer.parseInt(level) + 1;
        int levelInt = nLevel.intValue();
        level = Integer.toString(nLevel);
        editor.putString(itemFound, level);
        editor.commit();
        String test = prefs.getString(itemFound, "");

        String item = currentItem();

        String sentence = "";
        if(getGameType() == GameType.eTrouverMot)
            sentence = "Bravo, " + itemFound + " commence bien par " + item + " ! ";
        else if(getGameType() == GameType.eTrouverLettre) {
            char charFound = itemFound.charAt(0);
            boolean isMinus = (charFound >= 'a') && (charFound <= 'z');
            String itemFoundVocal = (charFound == 'y' || charFound == 'Y') ? itemFound + " grec" : itemFound;
            sentence = "Bravo, tu as trouver la lettre " + itemFoundVocal + (isMinus ? " minuscule" : " majuscule");
            ArrayList<Character> currentLevelArray = _notationLetter.get(levelInt-1);
            currentLevelArray.remove(_currentLetterIndex);
            if(_notationLetter.get(nLevel) == null)
                _notationLetter.put(nLevel, new ArrayList<Character>());
            _notationLetter.get(nLevel).add(charFound);
        }
        else if(getGameType() == GameType.eTrouverPremiereLettre) {
            char charFound = itemFound.charAt(0);
            sentence = "Bravo, " + item + " commence bien par " + itemFound + " ! ";
            ArrayList<Character> currentLevelArray = _notationFirstLetter.get(levelInt-1);
            currentLevelArray.remove(_currentLetterIndex);
            if(_notationLetter.get(nLevel) == null)
                _notationLetter.put(nLevel, new ArrayList<Character>());
            _notationLetter.get(nLevel).add(charFound);
        }
        Intent successActivity = new Intent(_ocrCaptureActivity, SuccessActivity.class);
        successActivity.putExtra("sentence", sentence);
        successActivity.putExtra("result", "success");
        successActivity.putExtra("currentItem", item);

        _ocrCaptureActivity.startActivityForResult(successActivity, SUCCESS_ACTIVITY);
    }

    static boolean isLow(char c)
    {
        return c >= 'a';
    }

    public static void onFail(String wrongItem, String ExpectedItem)
    {
        String item = currentItem();

        String sentence = "Tu tes trompé, ";
        if(getGameType() == GameType.eTrouverMot) {
            sentence += wrongItem + " ne commence pas par, " + currentItem() +
                    ", le bon mot etait " + ExpectedItem;
        }
        else if(getGameType() == GameType.eTrouverLettre){
            String minmajWrongWord =  isLow(wrongItem.charAt(0)) ? "minuscule" : "majuscule";
            String minmajExpeted =  isLow(ExpectedItem.charAt(0)) ? "minuscule" : "majuscule";
            sentence += "tu as choisi la lettre " + wrongItem + " " + minmajWrongWord +
                    ", j'avais demander la lettre " + ExpectedItem + " " + minmajExpeted;
        }
        Intent successActivity = new Intent(_ocrCaptureActivity, SuccessActivity.class);
        successActivity.putExtra("sentence", sentence);
        successActivity.putExtra("result", "error");
        successActivity.putExtra("currentItem", item);
        _ocrCaptureActivity.startActivityForResult(successActivity, SUCCESS_ACTIVITY);
    }

    public static void retour(Activity parent, View view)
    {
        String name = parent.getLocalClassName();
        parent.finish();
    }

    public static void options(Activity parent, View view)
    {
        Intent menuOptions = new Intent(parent, MenuOptionsActivity.class);
        parent.startActivity(menuOptions);
    }

    public static int getVideoFromGameType(GameEngine.GameType type, Activity activity)
    {
        int video = 0;
        String name = activity.getLocalClassName();
        if(name.toLowerCase().indexOf("menu") != -1) {
            if (type == GameEngine.GameType.eTrouverMot)
                video = R.raw.submenumot;
            else if (type == GameEngine.GameType.eTrouverPremiereLettre)
                video = R.raw.submenupremierelettre;
            else if (type == GameEngine.GameType.eTrouverLettre)
                video = R.raw.submenulettre;
        }
        else if(name.toLowerCase().indexOf("question") != -1) {
            if (type == GameEngine.GameType.eTrouverMot)
                video = R.raw.questionmot;
            else if (type == GameEngine.GameType.eTrouverPremiereLettre)
                video = R.raw.questionpremierelettre;
            else if (type == GameEngine.GameType.eTrouverLettre)
                video = R.raw.questionlettres;
        }
        return video;
    }

    static class OnVideoReadyCallback implements Utils.IOnVideoReadyCallback
    {
        public void execute(VideoView video)
        {
            video.setOnCompletionListener(null);
            GameEngine.askNextItem();
        }
    }

    public static void configureGeneralButtons(Activity activity, int videoWidth, int videoHeight, int retourId, int optionsId, int aideId)
    {
        Button retour = (Button)activity.findViewById(retourId);
        Button options = (Button)activity.findViewById(optionsId);
        Button aide = (Button)activity.findViewById(aideId);

        Button arr[] = {retour, options, aide};

        int color = 0xAA888888;
        for(int i = 0; i < arr.length; i++)
        {
            Button b = arr[i];
            if(b != null)
                b.setBackgroundColor(color);
        }

        int w = videoWidth;
        int h = videoHeight;

        int precision = 10000;

        int xRetour = 1800;
        int yRetour = 250;
        int wRetour = 1300;
        int hRetour = 1200;

        int xOptions = 8700;
        int yOptions = 180;
        int wOptions = 1100;
        int hOptions = 1000;

        int xAide = 8700;
        int yAide = 1200;
        int wAide = 1100;
        int hAide = 1000;

        if(retour != null) {
            retour.setX(w * xRetour / precision);
            retour.setY(h * yRetour / precision);
            retour.setLayoutParams(new RelativeLayout.LayoutParams(w * wRetour / precision, h * hRetour / precision));
            retour.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.stopSounds();
                    Activity parent = ((Activity)v.getContext());
                    if(parent != null) {
                        String name = parent.getLocalClassName();
                        if(name == "alphabetActivity") {
                            MainActivity.mustFinish();
                        }
                        parent.finish();
                    }
                }
            });
        }

        if(aide != null) {
            aide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Intent menuOptions = new Intent(v.getContext(), MenuOptionsActivity.class);
                    //v.getContext().startActivity(menuOptions);
                }
            });
            aide.setX(w * xAide / precision);
            aide.setY(h * yAide / precision);
            aide.setLayoutParams(new RelativeLayout.LayoutParams(w * wAide / precision, h * hAide / precision));
        }

        if(options != null) {
            options.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent menuOptions = new Intent(v.getContext(), MenuOptionsActivity.class);
                    v.getContext().startActivity(menuOptions);
                }
            });
            options.setX(w * xOptions / precision);
            options.setY(h * yOptions / precision);
            options.setLayoutParams(new RelativeLayout.LayoutParams(w * wOptions / precision, h * hOptions / precision));
        }
    }

}


/*
 * Copyright (C) 2008-2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.spartacusrex.spartacuside.keyboard;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.method.MetaKeyKeyListener;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import com.duy.editor.R;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


/**
 * Example of writing an input method for a soft keyboard.  This code is
 * focused on simplicity over completeness, so it should in no way be considered
 * to be a complete soft keyboard implementation.  Its purpose is to provide
 * a basic example for how you would get started writing an input method, to
 * be fleshed out as appropriate.
 */
public class TerminalKeyboard extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener, SharedPreferences.OnSharedPreferenceChangeListener {

    static final boolean DEBUG = false;

    /**
     * This boolean indicates the optional example code for performing
     * processing of hard keys in addition to regular text generation
     * from on-screen interaction.  It would be used for input methods that
     * perform language translations (such as converting text entered on
     * a QWERTY keyboard to Chinese), but may not be used for input methods
     * that are primarily intended to be used for on-screen text entry.
     */
    static final boolean PROCESS_HARD_KEYS = false;
    public static int CTRL_KEY = -9;
    public static int ALT_KEY = -10;
    // Implementation of KeyboardViewListener
    public int mLastPrimCode = -87687;
    SharedPreferences mPrefs;
    boolean mRefreshRequired = false;
    int mCurrentOrientation;
    int mKeyboardPortraitHeight = 0;
    int mKeyboardPortraitType = 0;
    int mKeyboardLandscapeHeight = 0;
    int mKeyboardLandscapeType = 0;
    boolean mVibrate;
    boolean mKeyClick;
    Hashtable<Integer, String> mSpecialCodes;
    private Vibrator mVibrator;
    private AudioManager mAudioManager;
    private LatinKeyboardView mInputView;
    private CandidateView mCandidateView;
    private CompletionInfo[] mCompletions;
    private StringBuilder mComposing = new StringBuilder();
    private boolean mPredictionOn;
    private boolean mCompletionOn;
    private int mLastDisplayWidth;
    private boolean mCapsLock;
    private long mLastShiftTime;

    /*private LatinKeyboard mSymbolsKeyboard;
    private LatinKeyboard mSymbolsShiftedKeyboard;
    private LatinKeyboard mQwertyKeyboard;
    private LatinKeyboard mQwertyKeyboardShift;
    private LatinKeyboard mCurKeyboard;*/
    private long mMetaState;
    private String mWordSeparators;
    private KeyboardSwitcher mKeyboards;

    /**
     * Main initialization of the input method component.  Be sure to call
     * to super class.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mWordSeparators = getResources().getString(R.string.word_separators);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefs.registerOnSharedPreferenceChangeListener(this);

        //Get the Prefs
        mKeyboardPortraitType = getStringPref("keyboard-port-size", "0");
        mKeyboardPortraitHeight = getStringPref("keyboard-port-keyheight", "1");
        mKeyboardLandscapeType = getStringPref("keyboard-land-size", "1");
        mKeyboardLandscapeHeight = getStringPref("keyboard-land-keyheight", "1");

        mVibrate = getStringPref("vibrate", "1") == 1 ? true : false;
        mKeyClick = getStringPref("keyclick", "0") == 1 ? true : false;

        //Check Limits
        if (mKeyboardPortraitHeight >= KeyboardSwitcher.KEYBOARD_SIZES) {
            mKeyboardPortraitHeight = 0;
        }
        if (mKeyboardPortraitType > 1) {
            mKeyboardPortraitType = 0;
        }
        if (mKeyboardLandscapeHeight >= KeyboardSwitcher.KEYBOARD_SIZES) {
            mKeyboardPortraitHeight = 0;
        }
        if (mKeyboardLandscapeType > 1) {
            mKeyboardPortraitType = 0;
        }

        //All the keyboards..
        mKeyboards = new KeyboardSwitcher();

        // Get instance of Vibrator from current Context
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        //The Special Key Codes
        mSpecialCodes = new Hashtable<Integer, String>();
        mSpecialCodes.put(new Integer(-900), "++");
        mSpecialCodes.put(new Integer(-901), "--");
        mSpecialCodes.put(new Integer(-902), "&&");
        mSpecialCodes.put(new Integer(-903), "||");
        mSpecialCodes.put(new Integer(-904), "\\\\");
        mSpecialCodes.put(new Integer(-905), "//");
        mSpecialCodes.put(new Integer(-906), "==");
        mSpecialCodes.put(new Integer(-907), "<=");
        mSpecialCodes.put(new Integer(-908), ">=");
        mSpecialCodes.put(new Integer(-909), "!=");
        mSpecialCodes.put(new Integer(-910), ">>");


        //No Candidate View
        setCandidatesViewShown(false);
    }

    /**
     * This is the point where you can do all of your UI initialization.  It
     * is called after creation and any configuration change.
     */
    @Override
    public void onInitializeInterface() {
//        Log.v("SpartacusRex", "Keyboard :  onInitializeInterface() orient : "+getResources().getConfiguration().orientation);

        //Orientation
        mCurrentOrientation = getResources().getConfiguration().orientation;

        //Check not redoing..
        if (mKeyboards.isInited()) {
            // Configuration changes can happen after the keyboard gets recreated,
            // so we need to be able to re-build the keyboards if the available
            // space has changed.
            int displayWidth = getMaxWidth();

            //Refresh the Keyboard
            refreshKeyboard();

            //If no Change.. No Change
            if (displayWidth == mLastDisplayWidth) {
                return;
            }

            mLastDisplayWidth = displayWidth;
        }

        //Create the Keyboards
        mKeyboards.init(this);

        //Refresh the Keyboard
        refreshKeyboard();
    }

    /**
     * Called by the framework when your view for creating input needs to
     * be generated.  This will be called the first time your input method
     * is displayed, and every time it needs to be re-created such as due to
     * a configuration change.
     */
    @Override
    public View onCreateInputView() {
        mInputView = (LatinKeyboardView) getLayoutInflater().inflate(R.layout.input, null);
        mInputView.setOnKeyboardActionListener(this);
        mInputView.setFunction(mKeyboards.isFunction());
        mInputView.setKeyboard(mKeyboards.getCurrentKeyboard());

        //Set the Mode..
        if (mKeyboards.getKeyboardType() == KeyboardSwitcher.KEYBOARD_NORM) {
            mInputView.setMode(LatinKeyboardView.MODE_SMALL);
        } else {
            mInputView.setMode(LatinKeyboardView.MODE_LARGE);
        }

        return mInputView;
    }

    /**
     * Called by the framework when your view for showing candidates needs to
     * be generated, like {@link #onCreateInputView}.
     */
    @Override
    public View onCreateCandidatesView() {
        mCandidateView = new CandidateView(this);
        mCandidateView.setService(this);

        return mCandidateView;
    }

    private void refreshKeyboard() {
        //Set the Init
        boolean small = true;
        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if (mKeyboardPortraitType == 0) {
                mKeyboards.setKeyboardType(KeyboardSwitcher.KEYBOARD_NORM);
            } else {
                mKeyboards.setKeyboardType(KeyboardSwitcher.KEYBOARD_LARGE);
                small = false;
            }

            //Set Size
            mKeyboards.setKeyboardSize(mKeyboardPortraitHeight);
        } else {
            if (mKeyboardLandscapeType == 0) {
                mKeyboards.setKeyboardType(KeyboardSwitcher.KEYBOARD_NORM);
            } else {
                mKeyboards.setKeyboardType(KeyboardSwitcher.KEYBOARD_LARGE);
                small = false;
            }

            //Set Size
            mKeyboards.setKeyboardSize(mKeyboardLandscapeHeight);
        }

        if (mInputView != null) {
            mInputView.setKeyboard(mKeyboards.getCurrentKeyboard());
            if (small) {
                mInputView.setMode(LatinKeyboardView.MODE_SMALL);
            } else {
                mInputView.setMode(LatinKeyboardView.MODE_LARGE);
            }
//            Log.v("SpartacusRex", "New Keyboard.. Setting Positions..");
//            mInputView.setKeyPositions();
        }
    }

    /**
     * This is the main point where we do our initialization of the input method
     * to begin operating on an application.  At this point we have been
     * bound to the client, and are now receiving all of the detailed information
     * about the target of our edits.
     */
    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);

        //Check if a refresh is required..
        if (mRefreshRequired) {
            mRefreshRequired = false;

            refreshKeyboard();
        }

        // Reset our state.  We want to do this even if restarting, because
        // the underlying state of the text editor could have changed in any way.
        mComposing.setLength(0);
        updateCandidates();

        if (!restarting) {
            // Clear shift states.
            mMetaState = 0;
        }

        mPredictionOn = false;
        mCompletionOn = false;
        mCompletions = null;

        updateShiftKeyState(attribute);

        // We are now going to initialize our state based on the type of text being edited.
        /*switch (attribute.inputType&EditorInfo.TYPE_MASK_CLASS) {
            case EditorInfo.TYPE_CLASS_NUMBER:
            case EditorInfo.TYPE_CLASS_DATETIME:
                // Numbers and dates default to the symbols keyboard, with
                // no extra features.
                mCurKeyboard = mSymbolsKeyboard;
                break;

            case EditorInfo.TYPE_CLASS_PHONE:
                // Phones will also default to the symbols keyboard, though
                // often you will want to have a dedicated phone keyboard.
                mCurKeyboard = mSymbolsKeyboard;
                break;

            case EditorInfo.TYPE_CLASS_TEXT:
                // This is general text editing.  We will default to the
                // normal alphabetic keyboard, and assume that we should
                // be doing predictive text (showing candidates as the
                // user types).
                mCurKeyboard = mQwertyKeyboard;
                mPredictionOn = true;

                // We now look for a few special variations of text that will
                // modify our behavior.
                int variation = attribute.inputType &  EditorInfo.TYPE_MASK_VARIATION;
                if (variation == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD ||
                        variation == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    // Do not display predictions / what the user is typing
                    // when they are entering a password.
                    mPredictionOn = false;
                }

                if (variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                        || variation == EditorInfo.TYPE_TEXT_VARIATION_URI
                        || variation == EditorInfo.TYPE_TEXT_VARIATION_FILTER) {
                    // Our predictions are not useful for e-mail addresses
                    // or URIs.
                    mPredictionOn = false;
                }

                if ((attribute.inputType&EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0) {
                    // If this is an auto-complete text view, then our predictions
                    // will not be shown and instead we will allow the editor
                    // to supply their own.  We only show the editor's
                    // candidates when in fullscreen mode, otherwise relying
                    // own it displaying its own UI.
                    mPredictionOn = false;
                    mCompletionOn = isFullscreenMode();
                }

                // We also want to look at the current state of the editor
                // to decide whether our alphabetic keyboard should start out
                // shifted.
                updateShiftKeyState(attribute);
                break;

            default:
                // For all unknown input types, default to the alphabetic
                // keyboard with no special features.
                mCurKeyboard = mQwertyKeyboard;
                updateShiftKeyState(attribute);
        }
        */

        // Update the label on the enter key, depending on what the application
        // says it will do.
        mKeyboards.getCurrentKeyboard().setImeOptions(getResources(), attribute.imeOptions);
    }

    /**
     * This is called when the user is done editing a field.  We can use
     * this to reset our state.
     */
    @Override
    public void onFinishInput() {
        super.onFinishInput();

        // Clear current composing text and candidates.
        mComposing.setLength(0);
        updateCandidates();

        setCandidatesViewShown(false);

        if (mInputView != null) {
            mInputView.closing();
        }
    }

    @Override
    public void onStartInputView(EditorInfo attribute, boolean restarting) {
        super.onStartInputView(attribute, restarting);
        // Apply the selected keyboard to the input view.
        mInputView.setKeyboard(mKeyboards.getCurrentKeyboard());
        mInputView.closing();

    }

    /**
     * This tells us about completions that the editor has determined based
     * on the current text in it.  We want to use this in fullscreen mode
     * to show the completions ourself, since the editor can not be seen
     * in that situation.
     */
    /*@Override public void onDisplayCompletions(CompletionInfo[] completions) {
        if (mCompletionOn) {
            mCompletions = completions;
            if (completions == null) {
                setSuggestions(null, false, false);
                return;
            }
            
            List<String> stringList = new ArrayList<String>();
            for (int i=0; i<(completions != null ? completions.length : 0); i++) {
                CompletionInfo ci = completions[i];
                if (ci != null) stringList.add(ci.getText().toString());
            }
            setSuggestions(stringList, true, true);
        }
    }*/

    /**
     * Deal with the editor reporting movement of its cursor.
     */
    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd,
                                  int newSelStart, int newSelEnd,
                                  int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);

        // If the current selection in the text view changes, we should
        // clear whatever candidate text we have.
        if (mComposing.length() > 0 && (newSelStart != candidatesEnd
                || newSelEnd != candidatesEnd)) {
            mComposing.setLength(0);
            updateCandidates();
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.finishComposingText();
            }
        }
    }

    /**
     * This translates incoming hard key events in to edit operations on an
     * InputConnection.  It is only needed when using the
     * PROCESS_HARD_KEYS option.
     */
    private boolean translateKeyDown(int keyCode, KeyEvent event) {
//        Log.v("SpartacusRex","SOFT : translateKeyDown "+keyCode);

        mMetaState = MetaKeyKeyListener.handleKeyDown(mMetaState,
                keyCode, event);
        int c = event.getUnicodeChar(MetaKeyKeyListener.getMetaState(mMetaState));
        mMetaState = MetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
        InputConnection ic = getCurrentInputConnection();
        if (c == 0 || ic == null) {
            return false;
        }

        boolean dead = false;

        if ((c & KeyCharacterMap.COMBINING_ACCENT) != 0) {
            dead = true;
            c = c & KeyCharacterMap.COMBINING_ACCENT_MASK;
        }

        if (mComposing.length() > 0) {
            char accent = mComposing.charAt(mComposing.length() - 1);
            int composed = KeyEvent.getDeadChar(accent, c);

            if (composed != 0) {
                c = composed;
                mComposing.setLength(mComposing.length() - 1);
            }
        }

        onKey(c, null);

        return true;
    }

    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        Log.v("SpartacusRex","SOFT : onKeyDown "+ keyCode+" "+event.getMetaState());
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // The InputMethodService already takes care of the back
                // key for us, to dismiss the input method if it is shown.
                // However, our keyboard could be showing a pop-up window
                // that back should dismiss, so we first allow it to do that.
                if (event.getRepeatCount() == 0 && mInputView != null) {
                    if (mInputView.handleBack()) {
                        return true;
                    }
                }
                break;

            case KeyEvent.KEYCODE_DEL:
                // Special handling of the delete key: if we currently are
                // composing text for the user, we want to modify that instead
                // of let the application to the delete itself.
                if (mComposing.length() > 0) {
                    onKey(Keyboard.KEYCODE_DELETE, null);
                    return true;
                }
                break;

            case KeyEvent.KEYCODE_ENTER:
                // Let the underlying text editor always handle these.
                return false;

            default:
                // For all other keys, if we want to do transformations on
                // text being entered with a hard keyboard, we need to process
                // it and do the appropriate action.
                if (PROCESS_HARD_KEYS) {
                    if (keyCode == KeyEvent.KEYCODE_SPACE
                            && (event.getMetaState() & KeyEvent.META_ALT_ON) != 0) {
                        // A silly example: in our input method, Alt+Space
                        // is a shortcut for 'android' in lower case.
                        InputConnection ic = getCurrentInputConnection();
                        if (ic != null) {
                            // First, tell the editor that it is no longer in the
                            // shift state, since we are consuming this.
                            ic.clearMetaKeyStates(KeyEvent.META_ALT_ON);
                            keyDownUp(KeyEvent.KEYCODE_A);
                            keyDownUp(KeyEvent.KEYCODE_N);
                            keyDownUp(KeyEvent.KEYCODE_D);
                            keyDownUp(KeyEvent.KEYCODE_R);
                            keyDownUp(KeyEvent.KEYCODE_O);
                            keyDownUp(KeyEvent.KEYCODE_I);
                            keyDownUp(KeyEvent.KEYCODE_D);
                            // And we consume this event.
                            return true;
                        }
                    }
                    if (mPredictionOn && translateKeyDown(keyCode, event)) {
                        return true;
                    }
                }
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        Log.v("SpartacusRex","SOFT : onKeyUp "+keyCode +" "+event.getMetaState());

        // If we want to do transformations on text being entered with a hard
        // keyboard, we need to process the up events to update the meta key
        // state we are tracking.
        if (PROCESS_HARD_KEYS) {
            if (mPredictionOn) {
                mMetaState = MetaKeyKeyListener.handleKeyUp(mMetaState,
                        keyCode, event);
            }
        }

        return super.onKeyUp(keyCode, event);
    }

    /**
     * Helper function to commit any text being composed in to the editor.
     */
    private void commitTyped(InputConnection inputConnection) {
        if (mComposing.length() > 0) {
            inputConnection.commitText(mComposing, mComposing.length());
            mComposing.setLength(0);
            updateCandidates();
        }
    }

    /**
     * Helper to update the shift state of our keyboard based on the initial
     * editor state.
     */
    private void updateShiftKeyState(EditorInfo attr) {
        /*if (attr != null
                && mInputView != null && mQwertyKeyboard == mInputView.getKeyboard()) {
            int caps = 0;
            EditorInfo ei = getCurrentInputEditorInfo();
            if (ei != null && ei.inputType != EditorInfo.TYPE_NULL) {
                caps = getCurrentInputConnection().getCursorCapsMode(attr.inputType);
            }
            mInputView.setShifted(mCapsLock || caps != 0);
        }*/
    }

    /**
     * Helper to determine if a given character code is alphabetic.
     */
    private boolean isAlphabet(int code) {
        if (Character.isLetter(code)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Helper to send a key down / key up pair to the current editor.
     */
    private void keyDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }

//    private void playKeyClick(int primaryCode) {
//        if (true) {
//            // FIXME: Volume and enable should come from UI settings
//            // FIXME: These should be triggered after auto-repeat logic
//            int sound = AudioManager.FX_KEYPRESS_STANDARD;
//            switch (primaryCode) {
//            case Keyboard.KEYCODE_DELETE:
//                sound = AudioManager.FX_KEYPRESS_DELETE;
//                break;
//            case Keyboard.KEYCODE_ENTER:
//                sound = AudioManager.FX_KEYPRESS_RETURN;
//                break;
//            case Keyboard.KEYCODE_SPACE:
//                sound = AudioManager.FX_KEYPRESS_SPACEBAR;
//                break;
//            }
//            mAudioManager.playSoundEffect(sound);
//        }
//    }

    /**
     * Helper to send a character to the editor as raw key events.
     */
    private void sendKey(int keyCode) {
        switch (keyCode) {
            case '\n':
                keyDownUp(KeyEvent.KEYCODE_ENTER);
                break;
            default:
                if (keyCode >= '0' && keyCode <= '9') {
                    keyDownUp(keyCode - '0' + KeyEvent.KEYCODE_0);
                } else {
                    getCurrentInputConnection().commitText(String.valueOf((char) keyCode), 1);
                }
                break;
        }
    }

    public void onKey(int primaryCode, int[] keyCodes) {
//        Log.v("SpartacusRex","MAIN SOFT ENTRY : onKey "+primaryCode);

        if (mInputView == null) {
            return;
        }

        //Actions..
        if (mVibrate) {
            mVibrator.vibrate(25);
        }

        int soundeffect = AudioManager.FX_KEYPRESS_STANDARD;

        Keyboard current = mInputView.getKeyboard();
        boolean validkey = false;

        if (primaryCode <= KeyEvent.KEYCODE_DPAD_RIGHT && primaryCode >= KeyEvent.KEYCODE_DPAD_UP) {
            //Check for CTRL
            boolean on = mKeyboards.isCTRL();
            if (on) {
                //Its an arrow key - ABCD
                keyDownUp(primaryCode * -1);
            } else {
                //Its an arrow key - ABCD
                keyDownUp(primaryCode);
            }

        } else if (primaryCode == CTRL_KEY) {
            boolean on = mKeyboards.isCTRL();
            //CTRL Key pressed..
            if (!on) {
                //Disable ALT if necessary
                if (mKeyboards.isALT()) {
                    mKeyboards.setALT(false);
                    mInputView.invalidateAllKeys();


                    //Send Up Signal
                    //getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, -98));
                }

                //CTRL ACTIVE
                mKeyboards.setCTRL(true);

                //Send Down signal
                getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, -99));
            } else {
                //Send UP signal
                getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, -99));

                //Not on..
                mKeyboards.setCTRL(false);
            }

        } else if (primaryCode == ALT_KEY) {
            boolean on = mKeyboards.isALT();
            //CTRL Key pressed..
            if (!on) {
                //Disable CTRL if necessary
                if (mKeyboards.isCTRL()) {
                    mKeyboards.setCTRL(false);
                    mInputView.invalidateAllKeys();

                    //Send Up Signal
                    getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, -99));
                }

                //CTRL ACTIVE
                mKeyboards.setALT(true);

                //Send Down signal
                //getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, -98));
            } else {
                //Send UP signal
                //getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, -98));

                //Not on..
                mKeyboards.setALT(false);
            }

        }/*else if (isWordSeparator(primaryCode)) {
            // Handle separator
            //if (mComposing.length() > 0) {
            //    commitTyped(getCurrentInputConnection());
            //}

            //CHECK FOR ALT
            if(mKeyboards.isALT()){
                //Send ESCape first
                handleCharacter(27, null);
            }
            
            sendKey(primaryCode);

            validkey = true;

        }*/ else if (primaryCode == -5) {
            //Normal BACK delete
            soundeffect = AudioManager.FX_KEYPRESS_DELETE;
            handleBackspace();

        } else if (primaryCode == -743) {
            //SPECIAL ENTER..
            keyDownUp(KeyEvent.KEYCODE_ENTER);

        } else if (primaryCode == -6) {
            //Forward delete
            keyDownUp(-100);

        } else if (primaryCode == -1 || primaryCode == -999) {
            handleShift();

        } else if (primaryCode == -3) {
            //handleClose();
            showOptionsMenu();

        } else if (primaryCode == -2 && mInputView != null) {
            //FUNCTION KEY
            mKeyboards.FNKey();

            //Tell the Keyboard
            mInputView.setFunction(mKeyboards.isFunction());

            //Now redo
            mInputView.setKeyboard(mKeyboards.getCurrentKeyboard());


        } else if (primaryCode >= 131 && primaryCode <= 142) {
            //F1-F12
            keyDownUp(primaryCode);

            //Its Valid..
            //validkey = true;

        } else if (primaryCode <= -150 && primaryCode >= -153) {
            //Checl for CTRL
            int fact = 1;
            //if(mKeyboards.isCTRL()){
            //    fact = -1;
            //}

            //PAGE UP / DOWN /HOME / NEXT
            if (primaryCode == -150) {
                //Page UP
                keyDownUp(92 * fact);
            } else if (primaryCode == -151) {
                //Page DOWN
                keyDownUp(93 * fact);
            } else if (primaryCode == -152) {
                //Home
                keyDownUp(122 * fact);
            } else if (primaryCode == -153) {
                //End
                keyDownUp(123 * fact);
            }

            //validkey = true;

        } else if (primaryCode <= -900 && primaryCode >= -910) {
            //Special Multi KEY
            handleMultiFunction(primaryCode);

            //Its Valid..
            validkey = true;

        } else {
            //CHECK FOR ALT
            if (mKeyboards.isALT()) {
                //QUICK FIX - NEEDS MUCH MORE..
                //Send ESCape first
                handleCharacter(27, null);
            }

            //Is it Enter or Space
            if (primaryCode == 13) {
                //Return
                soundeffect = AudioManager.FX_KEYPRESS_RETURN;

            } else if (primaryCode == 32) {
                //Return
                soundeffect = AudioManager.FX_KEYPRESS_SPACEBAR;
            }

            if (primaryCode == 13 && mKeyboards.isCTRL()) {
                //Always send the ENTER key
                keyDownUp(KeyEvent.KEYCODE_ENTER);

            } else {
                handleCharacter(primaryCode, keyCodes);
            }

            validkey = true;
        }

        if (mKeyClick) {
            mAudioManager.playSoundEffect(soundeffect, -1);
        }

        //Reset the keyboard..
        if (validkey) {
            if (mKeyboards.isFunction()) {
                //Switch back
                mKeyboards.FNKey();

                //Tell the Keyboard
                mInputView.setFunction(false);

                //Now redo
                mInputView.setKeyboard(mKeyboards.getCurrentKeyboard());

            }

            //Sort the shift
            if (mKeyboards.validKeyPress()) {
                mInputView.setKeyboard(mKeyboards.getCurrentKeyboard());

            }

            //SWITCH OFF THE ALT / CTRL Combo
            if (mKeyboards.isCTRL()) {
                mKeyboards.setCTRL(false);
                //Send Up Signal
                getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, -99));
            }

            if (mKeyboards.isALT()) {
                mKeyboards.setALT(false);
            }

            //redraw..
            mInputView.invalidateAllKeys();
        }
    }

    private void handleMultiFunction(int zCode) {
        //Special Multi KEY
        String txt = mSpecialCodes.get(new Integer(zCode));
        if (txt != null) {
            //Pipe each letter one at atime..
            try {
                String first = txt.substring(0, 1);
                String second = txt.substring(1, 2);

                //Pipe it..
                getCurrentInputConnection().commitText(first, 1);
                getCurrentInputConnection().commitText(second, 1);
            } catch (Exception e) {
            }
        }
    }

    private void showOptionsMenu() {
        //Check valid InputView
        if (mInputView == null) {
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).showInputMethodPicker();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
//        builder.setIcon(R.drawable.ic_dialog_keyboard);
        builder.setNegativeButton("Cancel", null);
        CharSequence itemSettings = "Keyboard Settings";
        CharSequence itemInputMethod = "Choose Keyboard";
        CharSequence hideMethod = "Hide Keyboard";
        builder.setItems(new CharSequence[]{itemSettings, itemInputMethod, hideMethod},
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface di, int position) {
                        di.dismiss();

                        switch (position) {
                            case 0:
                                handleClose();

                                Intent intent = new Intent();
                                intent.setClass(TerminalKeyboard.this, KeyboardPrefs.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);

                                break;
                            case 1:
                                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).showInputMethodPicker();
                                break;
                            case 2:
                                handleClose();

                                break;
                        }
                    }
                });

        builder.setTitle("Keyboard Options");
        Dialog mOptionsDialog = builder.create();
        Window window = mOptionsDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.token = mInputView.getWindowToken();
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        mOptionsDialog.show();
    }

    private int getStringPref(String zKey, String zDefault) {
        int ival = 0;
        try {
            String value = mPrefs.getString(zKey, zDefault);
            ival = Integer.parseInt(value);
        } catch (NumberFormatException numberFormatException) {
            return 0;
        }
        return ival;
    }

    //When the Preference Activity is finished..
    public void onSharedPreferenceChanged(SharedPreferences zPrefs, String zKey) {
//        Log.v("SpartacusRex","Pref Change "+zKey);
        if (zKey.equals("keyboard-port-size")) {
            String value = zPrefs.getString(zKey, "0");
            int ival = Integer.parseInt(value);

            if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                if (ival == 0) {
                    //SMALL KEYBOARD
                    mKeyboards.setKeyboardType(KeyboardSwitcher.KEYBOARD_NORM);
                } else {
                    //LARGE
                    mKeyboards.setKeyboardType(KeyboardSwitcher.KEYBOARD_LARGE);
                }
            }
            mKeyboardPortraitType = ival;

        } else if (zKey.equals("keyboard-land-size")) {
            String value = zPrefs.getString(zKey, "1");
            int ival = Integer.parseInt(value);

            if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (ival == 0) {
                    //SMALL KEYBOARD
                    mKeyboards.setKeyboardType(KeyboardSwitcher.KEYBOARD_NORM);
                } else {
                    //LARGE
                    mKeyboards.setKeyboardType(KeyboardSwitcher.KEYBOARD_LARGE);
                }
            }
            mKeyboardLandscapeType = ival;

        } else if (zKey.equals("keyboard-port-keyheight")) {
            String value = zPrefs.getString(zKey, "1");
            int ival = Integer.parseInt(value);

            if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                mKeyboards.setKeyboardSize(ival);
            }
            mKeyboardPortraitHeight = ival;

        } else if (zKey.equals("keyboard-land-keyheight")) {
            String value = zPrefs.getString(zKey, "1");
            int ival = Integer.parseInt(value);

            if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                mKeyboards.setKeyboardSize(ival);
            }
            mKeyboardLandscapeHeight = ival;

        } else if (zKey.equals("vibrate")) {
            String value = zPrefs.getString(zKey, "1");
            int ival = Integer.parseInt(value);
            mVibrate = (ival == 1);

        } else if (zKey.equals("keyclick")) {
            String value = zPrefs.getString(zKey, "0");
            int ival = Integer.parseInt(value);
            mKeyClick = (ival == 1);
        }

        //refresh next load..
        mRefreshRequired = true;
    }

    public void onText(CharSequence text) {
//        Log.v("SpartacusRex","SOFT : onText "+text.toString());

        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        ic.beginBatchEdit();
        if (mComposing.length() > 0) {
            commitTyped(ic);
        }
        ic.commitText(text, 0);
        ic.endBatchEdit();
        updateShiftKeyState(getCurrentInputEditorInfo());
    }

    /**
     * Update the list of available candidates from the current composing
     * text.  This will need to be filled in by however you are determining
     * candidates.
     */
    private void updateCandidates() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                ArrayList<String> list = new ArrayList<String>();
                list.add(mComposing.toString());
                setSuggestions(list, true, true);
            } else {
                setSuggestions(null, false, false);
            }
        }
    }

    public void setSuggestions(List<String> suggestions, boolean completions,
                               boolean typedWordValid) {
        /*if (suggestions != null && suggestions.size() > 0) {
            setCandidatesViewShown(true);
        } else if (isExtractViewShown()) {
            setCandidatesViewShown(true);
        }
        if (mCandidateView != null) {
            mCandidateView.setSuggestions(suggestions, completions, typedWordValid);
        }*/
    }

    private void handleBackspace() {
        final int length = mComposing.length();
        if (length > 1) {
            mComposing.delete(length - 1, length);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            updateCandidates();
        } else if (length > 0) {
            mComposing.setLength(0);
            getCurrentInputConnection().commitText("", 0);
            updateCandidates();
        } else {
            keyDownUp(KeyEvent.KEYCODE_DEL);
        }
        updateShiftKeyState(getCurrentInputEditorInfo());
    }

    private void handleShift() {
        if (mInputView == null) {
            return;
        }

        //Shift key Event
        mKeyboards.shiftKey();

        //Set the keyboard
        mInputView.setKeyboard(mKeyboards.getCurrentKeyboard());


//        Keyboard currentKeyboard = mInputView.getKeyboard();
        
        /*if (mQwertyKeyboard == currentKeyboard) {
            mInputView.setKeyboard(mQwertyKeyboardShift);
        }else{
            mInputView.setKeyboard(mQwertyKeyboard);
        }*/

        //boolean shift = !currentKeyboard.isShifted();
        /*mInputView.setShifted(shift);
        mQwertyKeyboard.setShifted(shift);
        mSymbolsKeyboard.setShifted(shift);*/

        /*if(shift){
            mQwertyKeyboard.getShiftKey().icon = getResources().getDrawable(R.drawable.sym_keyboard_shift_locked);
        }else{
            mQwertyKeyboard.getShiftKey().icon = getResources().getDrawable(R.drawable.sym_keyboard_shift);
        }

        mQwertyKeyboard.getShiftKey().on = false;
        
        
        /*if (mQwertyKeyboard == currentKeyboard) {
            // Alphabet keyboard
            //checkToggleCapsLock();
            //mInputView.setShifted(mCapsLock || !mInputView.isShifted());
            boolean shift = !mInputView.isShifted();
            mInputView.setShifted(shift);
            mSymbolsKeyboard.setShifted(shift);
            
        } else if (currentKeyboard == mSymbolsKeyboard) {
            //mSymbolsKeyboard.setShifted(true);

            boolean shift = !mInputView.isShifted();
            mInputView.setShifted(shift);
            mSymbolsKeyboard.setShifted(shift);

            //mInputView.setKeyboard(mSymbolsShiftedKeyboard);
            //mSymbolsShiftedKeyboard.setShifted(true);

        }/* else if (currentKeyboard == mSymbolsShiftedKeyboard) {
            mSymbolsShiftedKeyboard.setShifted(false);
            mInputView.setKeyboard(mSymbolsKeyboard);
            mSymbolsKeyboard.setShifted(false);
        }*/
    }

    private void handleCharacter(int primaryCode, int[] keyCodes) {
        if (isInputViewShown()) {
            if (mInputView.isShifted()) {
                primaryCode = Character.toUpperCase(primaryCode);
            }
        }
        if (isAlphabet(primaryCode) && mPredictionOn) {
            mComposing.append((char) primaryCode);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            updateShiftKeyState(getCurrentInputEditorInfo());
            updateCandidates();
        } else {
            getCurrentInputConnection().commitText(String.valueOf((char) primaryCode), 1);
        }
    }

    private void handleClose() {
        commitTyped(getCurrentInputConnection());
        requestHideSelf(0);
        mInputView.closing();
    }

    private void checkToggleCapsLock() {
        long now = System.currentTimeMillis();
        if (mLastShiftTime + 800 > now) {
            mCapsLock = !mCapsLock;
            mLastShiftTime = 0;
        } else {
            mLastShiftTime = now;
        }
    }

    private String getWordSeparators() {
        return mWordSeparators;
    }

    public boolean isWordSeparator(int code) {
        String separators = getWordSeparators();
        return separators.contains(String.valueOf((char) code));
    }

    public void pickDefaultCandidate() {
        pickSuggestionManually(0);
    }

    public void pickSuggestionManually(int index) {
        if (mCompletionOn && mCompletions != null && index >= 0
                && index < mCompletions.length) {
            CompletionInfo ci = mCompletions[index];
            getCurrentInputConnection().commitCompletion(ci);
            if (mCandidateView != null) {
                mCandidateView.clear();
            }
            updateShiftKeyState(getCurrentInputEditorInfo());
        } else if (mComposing.length() > 0) {
            // If we were generating candidate suggestions for the current
            // text, we would commit one of them here.  But for this sample,
            // we will just commit the current text.
            commitTyped(getCurrentInputConnection());
        }
    }

    public void swipeRight() {
        if (mCompletionOn) {
            pickDefaultCandidate();
        }
    }

    public void swipeLeft() {
        handleBackspace();
    }

    public void swipeDown() {
        handleClose();
    }

    public void swipeUp() {
    }

    public void onPress(int primaryCode) {
    }

    public void onRelease(int primaryCode) {
    }

}

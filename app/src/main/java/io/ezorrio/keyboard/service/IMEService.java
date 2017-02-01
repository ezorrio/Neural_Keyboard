package io.ezorrio.keyboard.service;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import io.ezorrio.keyboard.R;

/**
 * Created by golde on 30.01.2017.
 */

public class IMEService extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private static final int KEYCODE_NUMBERS = -100;
    private static final int KEYCODE_TOGGLE_LANGUAGE = -101;
    private static final int KEYCODE_ABC = -102;
    private static final String TAG = "IMEService";

    private static final int STATE_NO_SHIFT = 0;
    private static final int STATE_SHIFT = 1;
    private static final int STATE_CAPS= 2;

    private static final int KEYBOARD_EN = 0;
    private static final int KEYBOARD_RU = 1;
    private static final int KEYBOARD_SYMBOLS = 2;

    private static final int EN = 0;
    private static final int RU = 1;

    private KeyboardView mKeyboardView;
    private Keyboard mKeyboard;
    private InputConnection mInputConnection;
    private int mState = STATE_NO_SHIFT;
    private int mCurrentKeyboard = KEYBOARD_EN;
    private int mCurrentLanguage = EN;

    @Override
    public View onCreateInputView() {
        mKeyboardView = (KeyboardView)getLayoutInflater().inflate(R.layout.keyboard, null);
        mKeyboard = new Keyboard(this, R.xml.qwerty_en);

        mKeyboardView.setKeyboard(mKeyboard);
        mKeyboardView.setPreviewEnabled(false);
        mKeyboardView.setOnKeyboardActionListener(this);

        mInputConnection = getCurrentInputConnection();
        return mKeyboardView;
    }

    @Override
    public void onPress(int primaryCode) {

    }

    @Override
    public void onRelease(int primaryCode) {

    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        mInputConnection = getCurrentInputConnection();
        playClick(primaryCode);
        switch(primaryCode){
            case Keyboard.KEYCODE_DELETE :
                mInputConnection.deleteSurroundingText(1, 0);
                break;
            case Keyboard.KEYCODE_SHIFT:
                mState++;
                if (mState > STATE_CAPS){
                    mState = STATE_NO_SHIFT;
                }
                setAllShifted(mState);
                return;
            case Keyboard.KEYCODE_DONE:
                mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            case KEYCODE_NUMBERS:
                setCurrentKeyboard(KEYBOARD_SYMBOLS);
                break;
            case KEYCODE_ABC:
                setCurrentKeyboard(mCurrentLanguage);
                break;
            case KEYCODE_TOGGLE_LANGUAGE:
                int toggleTo = mCurrentLanguage == EN ? KEYBOARD_RU : KEYBOARD_EN;
                setCurrentKeyboard(toggleTo);
                break;
            default:
                char code = (char) primaryCode;
                if(Character.isLetter(code) && (mState == STATE_SHIFT || mState == STATE_CAPS)){
                    code = Character.toUpperCase(code);
                }
                mInputConnection.commitText(String.valueOf(code),1);
        }
        refreshShiftState();
    }

    @Override
    public void onText(CharSequence text) {
    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }

    private void playClick(int keyCode){
        AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
        switch(keyCode){
            case 32:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:
            case 10:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default: am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }
    }

    private void setAllShifted(int value){
        boolean shifted = value == STATE_SHIFT || value == STATE_CAPS;
        mKeyboard.setShifted(shifted);
        mKeyboardView.invalidateAllKeys();
    }

    private boolean needShift(){
        String text = mInputConnection.getTextBeforeCursor(2, InputConnection.GET_TEXT_WITH_STYLES).toString();
        return (text.isEmpty() || text.endsWith(". ") || text.endsWith("? ") || text.endsWith("! "));
    }

    private void refreshShiftState(){
        if (mState == STATE_SHIFT){
            mState = STATE_NO_SHIFT;
        }
        if (needShift()){
            mState = STATE_SHIFT;
        }
        Log.d(TAG, "State is " + String.valueOf(mState));
        setAllShifted(mState);
    }

    private void setCurrentKeyboard(int value){
        switch (value){
            case KEYBOARD_EN:
                mCurrentKeyboard = KEYBOARD_EN;
                mCurrentLanguage = EN;
                mKeyboard = new Keyboard(this, R.xml.qwerty_en);
                mKeyboardView.setKeyboard(mKeyboard);
                setAllShifted(mState);
                break;
            case KEYBOARD_RU:
                mCurrentKeyboard = KEYBOARD_RU;
                mCurrentLanguage = RU;
                mKeyboard = new Keyboard(this, R.xml.qwerty_ru);
                mKeyboardView.setKeyboard(mKeyboard);
                setAllShifted(mState);
                break;
            case KEYBOARD_SYMBOLS:
                mCurrentKeyboard = KEYBOARD_SYMBOLS;
                mKeyboard = new Keyboard(this, R.xml.keyboard_symbols);
                mKeyboardView.setKeyboard(mKeyboard);
                setAllShifted(mState);
                break;
        }
    }
}
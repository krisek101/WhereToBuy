package simpleapp.wheretobuy.helpers;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import simpleapp.wheretobuy.R;
import simpleapp.wheretobuy.activities.MapActivity;
import simpleapp.wheretobuy.constants.Constants;

public class ListenerHelper {

    private MapActivity parentActivity;
    private GestureDetector gestureDetector;

    public ListenerHelper(MapActivity parentActivity ) {
        gestureDetector = new GestureDetector(parentActivity, new SingleTapConfirm());
        this.parentActivity = parentActivity;
    }

    public void setListener(View viewElement, String type) {
        switch (type) {
            case "click":
                chooseClickListener(viewElement);
                break;
            case "touch":
                chooseTouchListener(viewElement);
                break;
            case "seekBarChange":
                chooseSeekBarChangeListener(viewElement);
                break;
        }
    }

    private void chooseSeekBarChangeListener(View viewElement){
    }

    private void chooseClickListener(View viewElement) {
        final int id = viewElement.getId();
        viewElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (id) {
                    case R.id.search_mic:
                        String language =  "pl-PL";
                        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,language);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language);
                        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, language);
                        parentActivity.startActivityForResult(intent, Constants.SPEECH_REQUEST_CODE);
                        break;
                }
            }
        });
    }

    private void chooseTouchListener(final View viewElement) {
        final int id = viewElement.getId();
        switch (id) {
        }
    }

    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }

    }
}
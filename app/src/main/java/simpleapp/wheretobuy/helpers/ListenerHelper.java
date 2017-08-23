package simpleapp.wheretobuy.helpers;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import simpleapp.wheretobuy.R;
import simpleapp.wheretobuy.activities.MapActivity;
import simpleapp.wheretobuy.constants.Constants;
import simpleapp.wheretobuy.constants.UsefulFunctions;

public class ListenerHelper {

    private MapActivity parentActivity;
    private GestureDetector gestureDetector;

    public ListenerHelper(MapActivity parentActivity) {
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
        }
    }

    private void chooseClickListener(View viewElement) {
        final int id = viewElement.getId();
        viewElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (id) {
                    case R.id.search_mic:
                        String language = "pl-PL";
                        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, language);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language);
                        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, language);
                        parentActivity.startActivityForResult(intent, Constants.SPEECH_REQUEST_CODE);
                        break;
                    case R.id.getMyLocationButton:
                        if (parentActivity.getMyLocationButtonClicked) {
                            parentActivity.hideFabOptions();
                        } else {
                            parentActivity.showFabOptions();
                        }
                        break;
                    case R.id.goToShopButton:
                        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + parentActivity.tempPosition.latitude + "," + parentActivity.tempPosition.longitude);
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        try {
                            parentActivity.startActivity(mapIntent);
                        } catch (Exception e) {
                            gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + parentActivity.tempPosition.latitude + "," + parentActivity.tempPosition.longitude);
                            Log.v("ADRES", gmmIntentUri.toString());
                            Intent callIntent = new Intent(Intent.ACTION_VIEW);
                            callIntent.setData(gmmIntentUri);
                            parentActivity.startActivity(callIntent);
                        }
                        break;
                    case R.id.getMyLocation:
                        parentActivity.checkUsersSettingGPS();
                        parentActivity.hideFabOptions();
                        break;
                    case R.id.setUserLocation:
                        parentActivity.changeUserLocation();
                        parentActivity.hideFabOptions();
                        break;
                }
            }
        });
    }

    private void chooseTouchListener(final View viewElement) {
        final int id = viewElement.getId();
        switch (id) {
            case R.id.footer:
                viewElement.setOnTouchListener(new View.OnTouchListener() {
                    float y;
                    int topOffset = UsefulFunctions.getStatusBarHeight(parentActivity);

                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        float toY;
                        int screenHeight = UsefulFunctions.getScreenHeight(parentActivity);

                        if (gestureDetector.onTouchEvent(motionEvent)) {
                            if (parentActivity.footerOpened) {
                                toY = screenHeight - topOffset;
                                parentActivity.footerOpened = false;
                                viewElement.setBackground(parentActivity.getResources().getDrawable(R.drawable.footer_rounded));
                            } else {
                                viewElement.setBackgroundColor(Color.parseColor("#f4f4f4"));
                                toY = parentActivity.footerTop + viewElement.getHeight();
                                parentActivity.footerOpened = true;
                            }
                            parentActivity.footerSlider.animate().y(toY).setDuration(100).start();
                            view.animate().y(toY - viewElement.getHeight()).setDuration(100).start();
                            return false;
                        } else {
                            switch (motionEvent.getActionMasked()) {
                                case MotionEvent.ACTION_DOWN:
                                    y = motionEvent.getRawY() - view.getY() - view.getHeight();
                                    break;
                                case MotionEvent.ACTION_MOVE:
                                    if (motionEvent.getRawY() - y < parentActivity.footerTop + viewElement.getHeight()) {
                                        toY = parentActivity.footerTop + viewElement.getHeight();
                                    } else {
                                        toY = motionEvent.getRawY() - y;
                                    }

                                    parentActivity.footerSlider.animate().y(toY).setDuration(0).start();
                                    view.animate().y(toY - viewElement.getHeight()).setDuration(0).start();
                                    break;
                                case MotionEvent.ACTION_UP:
                                    if (parentActivity.footerOpened) {
                                        if (motionEvent.getRawY() - y > 0.05 * parentActivity.footerSlider.getHeight()) {
                                            toY = screenHeight - topOffset;
                                            parentActivity.footerOpened = false;
                                            viewElement.setBackground(parentActivity.getResources().getDrawable(R.drawable.footer_rounded));
                                        } else {
                                            toY = parentActivity.footerTop + viewElement.getHeight();
                                            parentActivity.footerOpened = true;
                                            viewElement.setBackgroundColor(Color.parseColor("#f4f4f4"));
                                        }
                                    } else {
                                        if (motionEvent.getRawY() - y < 0.95 * parentActivity.footerSlider.getHeight()) {
                                            toY = parentActivity.footerTop + viewElement.getHeight();
                                            parentActivity.footerOpened = true;
                                            viewElement.setBackgroundColor(Color.parseColor("#f4f4f4"));
                                        } else {
                                            toY = screenHeight - topOffset;
                                            parentActivity.footerOpened = false;
                                            viewElement.setBackground(parentActivity.getResources().getDrawable(R.drawable.footer_rounded));
                                        }
                                    }
                                    parentActivity.footerSlider.animate().y(toY).setDuration(100).start();
                                    view.animate().y(toY - viewElement.getHeight()).setDuration(100).start();
                                    break;
                                default:
                                    return false;
                            }
                        }
                        return true;
                    }
                });
                break;
        }
    }

    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }

    }
}
package simpleapp.wheretobuy.helpers;

import android.view.View;

import simpleapp.wheretobuy.R;
import simpleapp.wheretobuy.activities.MapActivity;

public class CommunicationHelper {

    private MapActivity mapActivity;

    public CommunicationHelper(MapActivity mapActivity){
        this.mapActivity = mapActivity;
    }

    public void showInfo(String text){
        clearBackground();
        mapActivity.stateCommunication.setTextColor(mapActivity.getResources().getColor(R.color.black));
        mapActivity.stateCommunication.setText(text);
    }

    public void showError(String text){
        clearBackground();
        mapActivity.stateCommunication.setTextColor(mapActivity.getResources().getColor(R.color.red));
        mapActivity.stateCommunication.setText(text);
    }

    private void clearBackground(){
        mapActivity.stateLoading.setVisibility(View.GONE);
        mapActivity.stateFinish.setVisibility(View.GONE);
        mapActivity.stateCommunication.setVisibility(View.VISIBLE);
    }

}
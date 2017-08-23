package simpleapp.wheretobuy.constants;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import simpleapp.wheretobuy.activities.MapActivity;

public class ClearableAutoCompleteTextView extends android.support.v7.widget.AppCompatAutoCompleteTextView
        implements TextWatcher {
    private MapActivity mapActivity;

    public ClearableAutoCompleteTextView(Context context) {
        super(context);
    }

    public ClearableAutoCompleteTextView(Context context, AttributeSet attrs,
                                         int defStyle) {
        super(context, attrs, defStyle);
    }

    public ClearableAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private Drawable clearButton = null;
    private Drawable loadingGif = null;

    public void setActivity(MapActivity mapActivity){
        this.mapActivity = mapActivity;
    }

    public void setClearButton(final Drawable clearButton, final boolean b) {
        Bitmap bitmap = ((BitmapDrawable) clearButton).getBitmap();
        this.clearButton = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 30, 30, true));
        this.clearButton.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);

        final ClearableAutoCompleteTextView _this = this;
        if(b) {
            this.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    this.clearButton, null);
        }
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (_this.getCompoundDrawables()[2] == null) {
                    return false;
                }
                if (event.getAction() != MotionEvent.ACTION_UP) {
                    return false;
                }
                if (event.getX() > _this.getWidth() - _this.getPaddingRight()
                        - _this.clearButton.getIntrinsicWidth()) {
                    _this.setText("");
                    _this.setCompoundDrawables(null, null, null, null);
                    if(mapActivity != null) {
                        mapActivity.clearResults();
                    }
                }
                return false;
            }

        });

        this.addTextChangedListener(this);
    }

    public void setLoadingGif(Drawable loadingGif) {
        this.loadingGif = loadingGif;
        this.setCompoundDrawablesWithIntrinsicBounds(null, null,
                loadingGif, null);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() > 0) {
            // Set the bounds of the clear button
            this.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    clearButton, null);
        } else {
            this.setCompoundDrawables(null, null, null, null);
        }
    }

}
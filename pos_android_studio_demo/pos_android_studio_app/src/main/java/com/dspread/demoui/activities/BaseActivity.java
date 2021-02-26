package com.dspread.demoui.activities;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
//import android.support.v7.view.menu.ActionMenuItemView;
//import android.support.v7.widget.ActionMenuView;
//import android.support.v7.widget.AppCompatImageView;
//import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.dspread.demoui.R;

import java.util.ArrayList;

/**
 * BaseActivity used for to build all activity
 */

public abstract class BaseActivity extends AppCompatActivity {
    public static final String TAG = "BaseActivity";

    protected Toolbar toolbar;
    private TextView txt_toolbar_title;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            savedInstanceState.clear();
            savedInstanceState = null;
        }
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        toolbar =  findViewById(R.id.toolbar);
        if (toolbar != null) {
            txt_toolbar_title = toolbar.findViewById(R.id.txt_toolbar_title);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); //show the left arrow
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setDefaultToolbarColor();
            toolbar.setPadding(0, 0, 0, 0);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onToolbarLinstener();
                }
            });
        }
    }

    public Toolbar getToolbar(){
        if(toolbar!=null){
            return this.toolbar;
        }
        return null;
    }

    public abstract void onToolbarLinstener();

    protected abstract int getLayoutId();

    //protected abstract int getFragmentContainer();

    public void setActionBarIcon(int iconRes) {
        toolbar.setNavigationIcon(iconRes);
    }

    @Override
    public void setTitle(int titleResource) {
        setTitle(getResources().getString(titleResource));
    }

    public void setTitle(String title) {
        if(title != null && !title.equals("")) {
            if (txt_toolbar_title != null) {
                txt_toolbar_title.setText(title);
                toolbar.setTitle("");
            } else if (toolbar != null) {
                toolbar.setTitle(title);
                txt_toolbar_title.setText("");
            }
        }
    }

    public void setDefaultToolbarColor() {
        setToolbarBgColor(ContextCompat.getColor(this, R.color.ds_col_11));
        setToolbarIconColor(ContextCompat.getColor(this, R.color.ds_col_30));
        setStatusBarColor(ContextCompat.getColor(this, R.color.ds_col_11));
    }

    public void setCustomToolbarColor(String strCustomColor) {
        if (strCustomColor != null) {
            int customColor = Color.parseColor(strCustomColor);
            // set toolbar bg color
            setToolbarBgColor(customColor);
            // set toolbar text color
            int midColor = getResources().getColor(R.color.custom_middle_color);
            if (customColor >= midColor) {
                //                setToolbarTextColor(getResources().getColor(R.color.custom_dark_color));
                setToolbarIconColor(getResources().getColor(R.color.custom_dark_color));
            } else {
                //                setToolbarTextColor(getResources().getColor(R.color.custom_light_color));
                setToolbarIconColor(getResources().getColor(R.color.custom_light_color));
            }
            setStatusBarColor(customColor);
        }
    }

    public void setToolbarBgColor(int color) {
        if (toolbar != null) {
            toolbar.setBackgroundColor(color);
        }
    }

    public void setToolbarTextColor(int color) {
        if (toolbar != null) {
            // change title text color
            toolbar.setTitleTextColor(color);
            // change toolbar background color
            Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
            upArrow.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
        }
    }

    public void showProgressDialog() {
        showProgressDialog(null);
    }

    public void showProgressDialog(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
//        progressDialog.setCancelable(false);
        progressDialog.show(); // must call show() before setContentView()
//        progressDialog.setCancelable(false);
//        progressDialog.setCanceledOnTouchOutside(false);
        if (message == null) {
            progressDialog.getWindow().setBackgroundDrawableResource(R.color.transparency);
            progressDialog.setContentView(R.layout.progress_barlayout);
        } else if ("".equals(message)) {
            progressDialog.setMessage(getResources().getString(R.string.process_dialog_loading));
        } else {
            progressDialog.setMessage(message);
        }
        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @TargetApi(21)
    public void setStatusBarColor(int color) {
        if (isAboveKITKAT()) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //            window.setStatusBarColor(color);
        }
    }

    public int getStatusBarHeight() {
        int result = 0;
        if (isAboveKITKAT()) {
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = getResources().getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }

    public boolean isAboveKITKAT() {
        boolean isHigher = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            isHigher = true;
        }
        return isHigher;
    }

    /**
     * Use this method to colorize toolbar icons to the desired target color
     *
     * @param toolbarIconsColor the target color of toolbar icons
     */
    public void setToolbarIconColor(int toolbarIconsColor) {
        if (toolbar != null) {
            final PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(toolbarIconsColor, PorterDuff.Mode.SRC_ATOP);//MULTIPLY

            for (int i = 0; i < toolbar.getChildCount(); i++) {
                final View v = toolbar.getChildAt(i);

                //Step 1 : Changing the color of back button (or open drawer button).
                if (v instanceof ImageButton) {
                    //Action Bar back button
                    ((ImageButton) v).getDrawable().setColorFilter(colorFilter);
                }

                if (v instanceof ActionMenuView) {
                    for (int j = 0; j < ((ActionMenuView) v).getChildCount(); j++) {
                        //Step 2: Changing the color of any ActionMenuViews - icons that
                        //are not back button, nor text, nor overflow menu icon.
                        final View innerView = ((ActionMenuView) v).getChildAt(j);

                        if (innerView instanceof ActionMenuItemView) {
                            int drawablesCount = ((ActionMenuItemView) innerView).getCompoundDrawables().length;
                            for (int k = 0; k < drawablesCount; k++) {
                                if (((ActionMenuItemView) innerView).getCompoundDrawables()[k] != null) {
                                    final int finalK = k;

                                    //Important to set the color filter in seperate thread,
                                    //by adding it to the message queue
                                    //Won't work otherwise.
                                    innerView.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            ((ActionMenuItemView) innerView).getCompoundDrawables()[finalK].setColorFilter(colorFilter);
                                        }
                                    });
                                }
                            }
                        }
                    }
                }

                //Step 3: Changing the color of title and subtitle.
                if(txt_toolbar_title != null) {
                    txt_toolbar_title.setTextColor(toolbarIconsColor);
                }
                toolbar.setTitleTextColor(toolbarIconsColor);
                toolbar.setSubtitleTextColor(toolbarIconsColor);

                //Step 4: Changing the color of the Overflow Menu icon.
                setOverflowButtonColor(this, colorFilter);
            }
        }
    }

    private void setOverflowButtonColor(final AppCompatActivity activity, final PorterDuffColorFilter colorFilter) {
        final String overflowDescription = activity.getString(R.string.abc_action_menu_overflow_description);
        final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        final ViewTreeObserver viewTreeObserver = decorView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final ArrayList<View> outViews = new ArrayList<View>();
                decorView.findViewsWithText(outViews, overflowDescription,
                        View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
                if (outViews.isEmpty()) {
                    return;
                }
                AppCompatImageView overflow = (AppCompatImageView) outViews.get(0);
                overflow.setColorFilter(colorFilter);
                removeOnGlobalLayoutListener(decorView, this);
            }
        });
    }

    private void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        } else {
            v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        }
    }

}

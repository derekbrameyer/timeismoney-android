package com.doomonafireball.timeismoney.android.fragment;

import com.doomonafireball.timeismoney.android.R;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockDialogFragment;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import oak.widget.TextViewWithFont;
import roboguice.inject.InjectView;

/**
 * User: derek Date: 6/9/13 Time: 11:26 PM
 */
public class AboutFragment extends RoboSherlockDialogFragment {

    @InjectView(R.id.about_copy) private TextView aboutCopy;

    public AboutFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().setTitle(R.string.about);

        try {
            ((TextView) getDialog().findViewById(android.R.id.title))
                    .setTypeface(
                            TextViewWithFont.getStaticTypeFace(getActivity(), getString(R.string.default_font_bold)));

        } catch (Exception e) {
            // No-op
        }

        String appVersionName = "x.x";
        int appVersionCode = 0;
        try {
            appVersionName = getActivity().getApplication().getPackageManager()
                    .getPackageInfo(getActivity().getApplication().getPackageName(), 0).versionName;
            appVersionCode = getActivity().getApplication().getPackageManager()
                    .getPackageInfo(getActivity().getApplication().getPackageName(), 0).versionCode;

        } catch (PackageManager.NameNotFoundException e) {
            //Failed
        }
        aboutCopy.append(" " + appVersionName + " b" + appVersionCode);
    }
}

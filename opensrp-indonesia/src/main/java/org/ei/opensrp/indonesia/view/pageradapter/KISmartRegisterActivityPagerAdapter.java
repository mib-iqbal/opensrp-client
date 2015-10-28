package org.ei.opensrp.indonesia.view.pageradapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.ei.opensrp.indonesia.view.fragment.NativeKISmartRegisterFragment;
import org.ei.opensrp.view.dialog.DialogOption;
import org.ei.opensrp.view.dialog.OpenFormOption;
import org.ei.opensrp.view.fragment.DisplayFormFragment;

import static org.ei.opensrp.indonesia.AllConstantsINA.FormNames.KARTU_IBU_REGISTRATION;

/**
 * Created by koros on 10/23/15.
 */
public class KISmartRegisterActivityPagerAdapter extends FragmentPagerAdapter {
    public static final String ARG_PAGE = "page";
    String[] dialogOptions;

    public KISmartRegisterActivityPagerAdapter(FragmentManager fragmentManager, String[] dialogOptions) {
        super(fragmentManager);
        this.dialogOptions = dialogOptions;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new NativeKISmartRegisterFragment();
                break;

            default:
                String formName = dialogOptions[position - 1]; // account for the base fragment
                DisplayFormFragment f = new DisplayFormFragment();
                f.setFormName(formName);
                fragment = f;
                break;
        }

        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return dialogOptions.length + 1; // index 0 is always occupied by the base fragment
    }
}

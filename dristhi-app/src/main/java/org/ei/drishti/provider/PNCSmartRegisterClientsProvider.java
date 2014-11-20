package org.ei.drishti.provider;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;
import org.ei.drishti.R;
import org.ei.drishti.view.activity.SecuredActivity;
import org.ei.drishti.view.contract.*;
import org.ei.drishti.view.controller.PNCSmartRegisterController;
import org.ei.drishti.view.dialog.*;
import org.ei.drishti.view.viewHolder.*;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static org.ei.drishti.Context.getInstance;

public class PNCSmartRegisterClientsProvider implements SmartRegisterClientsProvider {

    private final LayoutInflater inflater;
    private final SecuredActivity activity;
    private final View.OnClickListener onClickListener;
    private final ProfilePhotoLoader photoLoader;

    private ServiceModeOption currentServiceModeOption;
    private final AbsListView.LayoutParams clientViewLayoutParams;

    protected PNCSmartRegisterController controller;

    public PNCSmartRegisterClientsProvider(SecuredActivity activity,
                                           View.OnClickListener onClickListener,
                                           PNCSmartRegisterController controller) {
        this.onClickListener = onClickListener;
        this.controller = controller;
        this.activity = activity;
        this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        photoLoader = new ECProfilePhotoLoader(activity.getResources(),
                activity.getResources().getDrawable(R.drawable.woman_placeholder));

        clientViewLayoutParams = new AbsListView.LayoutParams(MATCH_PARENT,
                (int) activity.getResources().getDimension(R.dimen.list_item_height));
    }

    @Override
    public View getView(SmartRegisterClient smartRegisterClient, View convertView, ViewGroup viewGroup) {
        ViewGroup itemView;
        NativePNCSmartRegisterViewHolder viewHolder;
        if (convertView == null) {
            itemView = (ViewGroup) inflater().inflate(R.layout.smart_register_pnc_client, null);
            viewHolder = new NativePNCSmartRegisterViewHolder(itemView);
            itemView.setTag(viewHolder);
        } else {
            itemView = (ViewGroup) convertView;
            viewHolder = (NativePNCSmartRegisterViewHolder) itemView.getTag();
        }

        PNCSmartRegisterClient client = (PNCSmartRegisterClient) smartRegisterClient;

        setupClientProfileView(client, viewHolder);
        setupThayiNumberView(client, viewHolder);

        viewHolder.hideAllServiceModeOptions();
        currentServiceModeOption.setupListView(client, viewHolder, onClickListener);

        itemView.setLayoutParams(clientViewLayoutParams);
        return itemView;
    }

    private void setupClientProfileView(PNCSmartRegisterClient client, NativePNCSmartRegisterViewHolder viewHolder) {
        viewHolder.profileInfoLayout().bindData(client, photoLoader);
        viewHolder.profileInfoLayout().setOnClickListener(onClickListener);
        viewHolder.profileInfoLayout().setTag(client);
    }

    private void setupThayiNumberView(PNCSmartRegisterClient client, NativePNCSmartRegisterViewHolder viewHolder) {
        viewHolder.txtThayiNumberView().setText(client.thayiNumber());
    }

    @Override
    public SmartRegisterClients getClients() {
        PNCClients clients = controller.getClients();

        return clients;
    }

    @Override
    public SmartRegisterClients updateClients(FilterOption villageFilter, ServiceModeOption serviceModeOption,
                                              FilterOption searchFilter, SortOption sortOption) {

        return getClients().applyFilter(villageFilter, serviceModeOption, searchFilter, sortOption);
    }

    @Override
    public void onServiceModeSelected(ServiceModeOption serviceModeOption) {
        currentServiceModeOption = serviceModeOption;
    }

    @Override
    public OnClickFormLauncher newFormLauncher(String formName, String entityId, String metaData) {
        return new OnClickFormLauncher(activity, formName, entityId, metaData);
    }

    public LayoutInflater inflater() {
        return inflater;
    }
}

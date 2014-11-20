package org.ei.drishti.view.customControls;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.ei.drishti.R;
import org.ei.drishti.view.contract.PNCSmartRegisterClient;

public class DeliveryInfoView extends LinearLayout {
    private TextView txtDeliveryDateView;
    private TextView txtDeliveryPlaceView;
    private TextView txtDeliveryTypeView;

    @SuppressWarnings("UnusedDeclaration")
    public DeliveryInfoView(Context context) {
        this(context, null, 0);
    }

    @SuppressWarnings("UnusedDeclaration")
    public DeliveryInfoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DeliveryInfoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void initialize() {
        txtDeliveryDateView = (TextView) findViewById(R.id.txt_delivery_date);
        txtDeliveryPlaceView = (TextView) findViewById(R.id.txt_delivery_place);
        txtDeliveryTypeView = (TextView) findViewById(R.id.txt_delivery_type);
    }

    // #TODO: make these names generic, so this layout can be reused in all the registers
    public void bindData(PNCSmartRegisterClient client) {
        txtDeliveryDateView.setText(client.deliveryDate());
        txtDeliveryPlaceView.setText(client.deliveryPlace());
        txtDeliveryTypeView.setText(client.deliveryType());
    }
}

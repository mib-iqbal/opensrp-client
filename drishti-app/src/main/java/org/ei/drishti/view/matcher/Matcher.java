package org.ei.drishti.view.matcher;

import org.ei.drishti.domain.Alert;
import org.ei.drishti.view.AfterChangeListener;

public interface Matcher {
    void setOnChangeListener(AfterChangeListener afterChangeListener);

    Object currentValue();

    boolean matches(Alert alert);
}

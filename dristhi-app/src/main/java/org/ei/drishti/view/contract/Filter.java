package org.ei.drishti.view.contract;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.ei.drishti.view.dialog.FilterClause;
import org.ei.drishti.view.dialog.FilterOption;
import org.ei.drishti.view.dialog.ServiceModeOption;
import org.ei.drishti.view.dialog.SortOption;

import java.util.ArrayList;
import java.util.List;

public class Filter<T> {
    public List<T> applyFilterWithClause(List<T> sourceList, final FilterClause<T> filterOption) {
        List<T> results = new ArrayList<T>();
        Iterables.addAll(results, Iterables.filter(sourceList, new Predicate<T>() {
            @Override
            public boolean apply(T object) {
                return filterOption.filter(object);
            }
        }));
        return results;
    }
}

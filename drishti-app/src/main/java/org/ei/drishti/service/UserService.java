package org.ei.drishti.service;

import org.ei.drishti.Context;
import org.ei.drishti.repository.AllSettings;
import org.ei.drishti.repository.Repository;

import static org.ei.drishti.event.Event.ON_LOGOUT;

public class UserService {
    private CommCareService commCareService;
    private final Repository repository;
    private final AllSettings allSettings;

    public UserService(CommCareService commCareService, Repository repository, AllSettings allSettings) {
        this.commCareService = commCareService;
        this.repository = repository;
        this.allSettings = allSettings;
    }

    public boolean isValidLocalLogin(String userName, String password) {
        return allSettings.fetchRegisteredANM().equals(userName) && repository.canUseThisPassword(password);
    }

    public boolean isValidRemoteLogin(String userName, String password) {
        return commCareService.isValidUser(userName, password);
    }

    public void loginWith(String userName, String password) {
        setupContextForLogin(userName, password);
        allSettings.registerANM(userName);
    }

    public boolean hasARegisteredUser() {
        return !allSettings.fetchRegisteredANM().equals("");
    }

    public void logout() {
        logoutSession();
        allSettings.registerANM("");
        allSettings.savePreviousFetchIndex("0");
        repository.deleteRepository();
    }

    public void logoutSession() {
        Context.getInstance().session().expire();
        ON_LOGOUT.notifyListeners(true);
    }

    public boolean hasSessionExpired() {
        return Context.getInstance().session().hasExpired();
    }

    protected void setupContextForLogin(String userName, String password) {
        Context.getInstance().session().start(Context.getInstance().session().lengthInMilliseconds());
        Context.getInstance().session().setPassword(password);
    }

}

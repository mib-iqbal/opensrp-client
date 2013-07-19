package org.ei.drishti.view.controller;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import org.ei.drishti.AllConstants;
import org.ei.drishti.domain.Child;
import org.ei.drishti.domain.EligibleCouple;
import org.ei.drishti.domain.Mother;
import org.ei.drishti.repository.AllBeneficiaries;
import org.ei.drishti.repository.AllEligibleCouples;
import org.ei.drishti.util.Cache;
import org.ei.drishti.util.CacheableData;
import org.ei.drishti.util.EasyMap;
import org.ei.drishti.view.contract.ECChildClient;
import org.ei.drishti.view.contract.ECClient;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Collections.sort;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.ei.drishti.AllConstants.ANCRegistrationFields.EDD;
import static org.ei.drishti.AllConstants.DEFAULT_WOMAN_IMAGE_PLACEHOLDER_PATH;
import static org.ei.drishti.AllConstants.ECRegistrationFields.*;

public class ECSmartRegisterController {
    public static final String STATUS_TYPE_FIELD = "type";
    public static final String STATUS_DATE_FIELD = "date";
    public static final String EC_STATUS = "ec";
    public static final String ANC_STATUS = "anc";
    public static final String PNC_STATUS = "pnc";
    public static final String PNC_FP_STATUS = "pnc/fp";
    public static final String STATUS_EDD_FIELD = "edd";
    private final AllEligibleCouples allEligibleCouples;
    private final AllBeneficiaries allBeneficiaries;
    private final Cache<String> cache;
    private final static String EC_CLIENTS_LIST = "ECClientsList";

    public ECSmartRegisterController(AllEligibleCouples allEligibleCouples, AllBeneficiaries allBeneficiaries, Cache<String> cache) {
        this.allEligibleCouples = allEligibleCouples;
        this.allBeneficiaries = allBeneficiaries;
        this.cache = cache;
    }

    public String get() {
        return cache.get(EC_CLIENTS_LIST, new CacheableData<String>() {
            @Override
            public String fetch() {
                List<EligibleCouple> ecs = allEligibleCouples.all();
                List<ECClient> ecClients = new ArrayList<ECClient>();

                for (EligibleCouple ec : ecs) {
                    String photoPath = isBlank(ec.photoPath()) ? DEFAULT_WOMAN_IMAGE_PLACEHOLDER_PATH : ec.photoPath();
                    ECClient ecClient = new ECClient(ec.caseId(), ec.wifeName(), ec.husbandName(), ec.village(), ec.ecNumber())
                            .withDateOfBirth(ec.getDetail(WOMAN_DOB))
                            .withFPMethod(ec.getDetail(CURRENT_FP_METHOD))
                            .withFamilyPlanningMethodChangeDate(ec.getDetail(FAMILY_PLANNING_METHOD_CHANGE_DATE))
                            .withIUDPlace(ec.getDetail(IUD_PLACE))
                            .withIUDPerson(ec.getDetail(IUD_PERSON))
                            .withNumberOfCondomsSupplied(ec.getDetail(NUMBER_OF_CONDOMS_SUPPLIED))
                            .withNumberOfCentchromanPillsDelivered(ec.getDetail(NUMBER_OF_CENTCHROMAN_PILLS_DELIVERED))
                            .withNumberOfOCPDelivered(ec.getDetail(NUMBER_OF_OCP_DELIVERED))
                            .withCaste(ec.getDetail(CASTE))
                            .withEconomicStatus(ec.getDetail(ECONOMIC_STATUS))
                            .withNumberOfPregnancies(ec.getDetail(NUMBER_OF_PREGNANCIES))
                            .withParity(ec.getDetail(PARITY))
                            .withNumberOfLivingChildren(ec.getDetail(NUMBER_OF_LIVING_CHILDREN))
                            .withNumberOfStillBirths(ec.getDetail(NUMBER_OF_STILL_BIRTHS))
                            .withNumberOfAbortions(ec.getDetail(NUMBER_OF_ABORTIONS))
                            .withIsHighPriority(ec.isHighPriority())
                            .withPhotoPath(photoPath)
                            .withHighPriorityReason(ec.getDetail(HIGH_PRIORITY_REASON))
                            .withIsOutOfArea(ec.isOutOfArea());
                    updateStatusInformation(ec, ecClient, "fp");
                    updateChildrenInformation(ecClient);
                    ecClients.add(ecClient);
                }
                sortByName(ecClients);
                return new Gson().toJson(ecClients);
            }
        });
    }

    private void updateChildrenInformation(ECClient ecClient) {
        List<Child> children = allBeneficiaries.findAllChildrenByECId(ecClient.entityId());
        sortByDateOfBirth(children);
        Iterable<Child> youngestTwoChildren = Iterables.skip(children, children.size() < 2 ? 0 : children.size() - 2);
        for (Child child : youngestTwoChildren) {
            ecClient.addChild(new ECChildClient(child.caseId(), child.gender(), child.dateOfBirth()));
        }
    }

    private void sortByDateOfBirth(List<Child> children) {
        sort(children, new Comparator<Child>() {
            @Override
            public int compare(Child child, Child anotherChild) {
                return LocalDate.parse(child.dateOfBirth()).compareTo(LocalDate.parse(anotherChild.dateOfBirth()));
            }
        });
    }

    private void sortByName(List<ECClient> ecClients) {
        sort(ecClients, new Comparator<ECClient>() {
            @Override
            public int compare(ECClient oneECClient, ECClient anotherECClient) {
                return oneECClient.wifeName().compareToIgnoreCase(anotherECClient.wifeName());
            }
        });
    }

    //#TODO: Needs refactoring
    private void updateStatusInformation(EligibleCouple eligibleCouple, ECClient ecClient, String FP_STATUS) {
        Mother mother = allBeneficiaries.findMotherWithOpenStatusByECId(eligibleCouple.caseId());

        if (mother == null && !eligibleCouple.hasFPMethod()) {
            ecClient.withStatus(EasyMap.create(STATUS_TYPE_FIELD, EC_STATUS)
                    .put(STATUS_DATE_FIELD, eligibleCouple.getDetail(REGISTRATION_DATE)).map());
            return;
        }

        if (mother == null && eligibleCouple.hasFPMethod()) {
            ecClient.withStatus(EasyMap.create(STATUS_TYPE_FIELD, FP_STATUS)
                    .put(STATUS_DATE_FIELD, eligibleCouple.getDetail("familyPlanningMethodChangeDate")).map());
            return;
        }

        if (mother != null && mother.isANC()) {
            ecClient.withStatus(EasyMap.create(STATUS_TYPE_FIELD, ANC_STATUS)
                    .put(STATUS_DATE_FIELD, mother.referenceDate())
                    .put(STATUS_EDD_FIELD, LocalDate.parse(mother.getDetail(EDD), DateTimeFormat.forPattern(AllConstants.FORM_DATE_TIME_FORMAT)).toString()).map());
            return;
        }

        if (mother != null && mother.isPNC() && !eligibleCouple.hasFPMethod()) {
            ecClient.withStatus(EasyMap.create(STATUS_TYPE_FIELD, PNC_STATUS)
                    .put(STATUS_DATE_FIELD, mother.referenceDate()).map());
            return;
        }

        if (mother != null && mother.isPNC() && eligibleCouple.hasFPMethod()) {
            ecClient.withStatus(EasyMap.create(STATUS_TYPE_FIELD, PNC_FP_STATUS)
                    .put(STATUS_DATE_FIELD, mother.referenceDate())
                    .put("fpMethodDate", eligibleCouple.getDetail("familyPlanningMethodChangeDate")).map());
            return;
        }
    }
}

package org.smartregister.chw.interactor;

import org.joda.time.DateTime;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.core.rule.PNCHealthFacilityVisitRule;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.domain.PNCHealthFacilityVisitSummary;
import org.smartregister.chw.domain.PncBaby;
import org.smartregister.dao.AbstractDao;
import org.smartregister.domain.Alert;
import org.smartregister.immunization.domain.ServiceWrapper;
import org.smartregister.repository.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultPncHomeVisitInteractorFlvTest extends BaseHomeVisitInteractorFlvTest {

    private DefaultPncHomeVisitInteractorFlv interactor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        interactor = Mockito.mock(DefaultPncHomeVisitInteractorFlv.class, Mockito.CALLS_REAL_METHODS);

        Whitebox.setInternalState(interactor, "editMode", false);
        Whitebox.setInternalState(interactor, "context", context);
        Whitebox.setInternalState(interactor, "memberObject", memberObject);
        Whitebox.setInternalState(interactor, "actionList", actionList);

        CoreConstants.JSON_FORM.setLocaleAndAssetManager(locale, assetManager);
        Mockito.doReturn(title).when(context).getString(Mockito.anyInt());

        Mockito.doReturn(builder).when(builder).withOptional(Mockito.anyBoolean());
        Mockito.doReturn(builder).when(builder).withBaseEntityID(Mockito.any());
        Mockito.doReturn(builder).when(builder).withDetails(Mockito.any());
        Mockito.doReturn(builder).when(builder).withDestinationFragment(Mockito.any());
        Mockito.doReturn(builder).when(builder).withHelper(Mockito.any());
        //Mockito.doReturn(builder).when(builder).withDisabledMessage(Mockito.any());
        //Mockito.doReturn(builder).when(builder).withValidator(Mockito.any());
        Mockito.doReturn(builder).when(builder).withProcessingMode(Mockito.any());
        Mockito.doReturn(builder).when(builder).withFormName(Mockito.any());
        //Mockito.doReturn(builder).when(builder).withJsonPayload(Mockito.any());
        Mockito.doReturn(builder).when(builder).withPayloadType(Mockito.any());
        Mockito.doReturn(builder).when(builder).withPayloadDetails(Mockito.any());
        Mockito.doReturn(builder).when(builder).withScheduleStatus(Mockito.any());
        Mockito.doReturn(builder).when(builder).withSubtitle(Mockito.any());

        Mockito.doReturn(builder).when(interactor).getBuilder(Mockito.any());

        Mockito.doReturn(ancHomeVisitAction).when(builder).build();
        Mockito.doReturn(visitRepository).when(interactor).getVisitRepository();

        JSONObject jsonObject = Mockito.mock(JSONObject.class);
        Mockito.doReturn(jsonObject).when(interactor).getFormJson(Mockito.any(), Mockito.any());
    }

    @Test
    public void testCalculateActionsPopulatesServicesForChild() throws BaseAncHomeVisitAction.ValidationException {

        Mockito.doReturn(context).when(view).getContext();
        Mockito.doReturn(false).when(view).getEditMode();

        Repository repository = Mockito.mock(Repository.class);
        ReflectionHelpers.setStaticField(AbstractDao.class, "repository", repository);

        List<PncBaby> children = getSampleKids(2);
        Mockito.doReturn(children).when(interactor).getChildren(Mockito.any());

        LinkedHashMap<String, BaseAncHomeVisitAction> services = interactor.calculateActions(view, memberObject, callBack);

        Assert.assertTrue(services.size() > 0);
    }

    @Test
    public void testGetDetailsReturnsDBResultsEditing() {
        Whitebox.setInternalState(interactor, "editMode", true);

        String baseID = "12345";
        String eventName = "Sample Event Name";

        Mockito.doReturn(baseID).when(memberObject).getBaseEntityId();

        ReflectionHelpers.callInstanceMethod(interactor, "getDetails"
                , ReflectionHelpers.ClassParameter.from(String.class, eventName));

        Mockito.verify(visitRepository).getLatestVisit(baseID, eventName);
    }

    @Test
    public void testEvaluateDangerSignsMother() {

        ReflectionHelpers.callInstanceMethod(interactor, "evaluateDangerSignsMother");

        Mockito.verify(actionList).put(Mockito.anyString(), Mockito.any(BaseAncHomeVisitAction.class));
    }

    private List<PncBaby> getSampleKids(int x) {
        List<PncBaby> children = new ArrayList<>();
        int w = x;
        while (w > 0) {
            children.add(new PncBaby("12345", "Fname " + x, "Lname " + x, "Mname " + x, new Date(), "yes"));
            w--;
        }
        return children;
    }

    @Test
    public void testEvaluateDangerSignsBaby() {

        int x = 4;
        List<PncBaby> children = getSampleKids(x);
        Whitebox.setInternalState(interactor, "children", children);

        ReflectionHelpers.callInstanceMethod(interactor, "evaluateDangerSignsBaby");
        Mockito.verify(actionList, Mockito.times(x)).put(Mockito.anyString(), Mockito.any(BaseAncHomeVisitAction.class));
    }

    @Test
    public void testEvaluatePNCHealthFacilityVisit() {

        PNCHealthFacilityVisitSummary visitSummary = Mockito.mock(PNCHealthFacilityVisitSummary.class);
        Mockito.doReturn(visitSummary).when(interactor).getLastHealthFacilityVisitSummary();

        PNCHealthFacilityVisitRule rule = Mockito.mock(PNCHealthFacilityVisitRule.class);
        Mockito.doReturn("1").when(rule).getVisitName();
        Mockito.doReturn(rule).when(interactor).getNextPNCHealthFacilityVisit(Mockito.any(), Mockito.any());


        ReflectionHelpers.callInstanceMethod(interactor, "evaluatePNCHealthFacilityVisit");
        Mockito.verify(actionList).put(Mockito.anyString(), Mockito.any(BaseAncHomeVisitAction.class));
    }

    @Test
    public void testEvaluateChildVaccineCard() {

        int x = 3;
        List<PncBaby> children = getSampleKids(x);
        Whitebox.setInternalState(interactor, "children", children);

        ReflectionHelpers.callInstanceMethod(interactor, "evaluateChildVaccineCard");
        Mockito.verify(actionList, Mockito.times(x)).put(Mockito.anyString(), Mockito.any(BaseAncHomeVisitAction.class));
    }

    /*
    @Test
    public void testEvaluateImmunization() {

        int x = 6;
        List<PncBaby> children = getSampleKids(x);
        Whitebox.setInternalState(interactor, "children", children);

        List<VaccineWrapper> wrappers = new ArrayList<>();
        VaccineWrapper vaccineWrapper = new VaccineWrapper();
        vaccineWrapper.setName("scheduleName");
        vaccineWrapper.setAlert(new Alert("caseID", "scheduleName", "visitCode", AlertStatus.urgent, "2019-01-01", null));
        wrappers.add(vaccineWrapper);
        Mockito.doReturn(wrappers).when(interactor).getWrappers(Mockito.any());

        ReflectionHelpers.callInstanceMethod(interactor, "evaluateImmunization");
        Mockito.verify(actionList, Mockito.times(x)).put(Mockito.anyString(), Mockito.any(BaseAncHomeVisitAction.class));
    }
     */

    @Test
    public void testEvaluateUmbilicalCord() {
        int x = 5;
        List<PncBaby> children = getSampleKids(x);
        Whitebox.setInternalState(interactor, "children", children);

        ReflectionHelpers.callInstanceMethod(interactor, "evaluateUmbilicalCord");
        Mockito.verify(actionList, Mockito.times(x)).put(Mockito.anyString(), Mockito.any(BaseAncHomeVisitAction.class));
    }

    @Test
    public void testEvaluateExclusiveBreastFeeding() {
        int x = 4;
        List<PncBaby> children = getSampleKids(x);
        Whitebox.setInternalState(interactor, "children", children);

        Map<String, ServiceWrapper> serviceWrapperMap = new HashMap<>();
        ServiceWrapper serviceWrapper = Mockito.mock(ServiceWrapper.class);
        Mockito.doReturn("Exclusive 1").when(serviceWrapper).getName();
        Mockito.doReturn(new DateTime()).when(serviceWrapper).getVaccineDate();
        serviceWrapperMap.put("Exclusive breastfeeding", serviceWrapper);

        Alert alert = Mockito.mock(Alert.class);
        Mockito.doReturn("2019-01-01").when(alert).startDate();
        Mockito.doReturn(alert).when(serviceWrapper).getAlert();
        Mockito.doReturn(serviceWrapperMap).when(interactor).getWrapperMap(Mockito.any());

        ReflectionHelpers.callInstanceMethod(interactor, "evaluateExclusiveBreastFeeding");
        Mockito.verify(actionList, Mockito.times(x)).put(Mockito.anyString(), Mockito.any(BaseAncHomeVisitAction.class));
    }

    @Test
    public void testEvaluateKangarooMotherCare() {
        int x = 7;
        List<PncBaby> children = getSampleKids(x);
        Whitebox.setInternalState(interactor, "children", children);

        ReflectionHelpers.callInstanceMethod(interactor, "evaluateKangarooMotherCare");
        Mockito.verify(actionList, Mockito.times(x)).put(Mockito.anyString(), Mockito.any(BaseAncHomeVisitAction.class));
    }

    @Test
    public void testEvaluateFamilyPlanning() {
        ReflectionHelpers.callInstanceMethod(interactor, "evaluateFamilyPlanning");
        Mockito.verify(actionList).put(Mockito.anyString(), Mockito.any(BaseAncHomeVisitAction.class));
    }

    @Test
    public void testEvaluateObservationAndIllnessMother() {
        ReflectionHelpers.callInstanceMethod(interactor, "evaluateObservationAndIllnessMother");
        Mockito.verify(actionList).put(Mockito.anyString(), Mockito.any(BaseAncHomeVisitAction.class));
    }

    @Test
    public void testEvaluateObservationAndIllnessBaby() {
        int x = 3;
        List<PncBaby> children = getSampleKids(x);
        Whitebox.setInternalState(interactor, "children", children);

        ReflectionHelpers.callInstanceMethod(interactor, "evaluateObservationAndIllnessBaby");
        Mockito.verify(actionList, Mockito.times(x)).put(Mockito.anyString(), Mockito.any(BaseAncHomeVisitAction.class));
    }
}

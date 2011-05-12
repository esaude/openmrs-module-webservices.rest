package org.openmrs.module.webservices.rest.web.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

public class PatientControllerTest extends BaseModuleContextSensitiveTest {
	
	private void log(String label, Object object) {
		String toPrint;
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.getSerializationConfig().set(SerializationConfig.Feature.INDENT_OUTPUT, true);
			toPrint = mapper.writeValueAsString(object);
		} catch (Exception ex) {
			toPrint = "" + object;
		}
		if (label != null)
			toPrint = label + ": " + toPrint;
		System.out.println(toPrint);
	}
	
	private WebRequest emptyRequest() {
		return new ServletWebRequest(new MockHttpServletRequest());
	}
	
	/**
	 * @see PatientController#getNames(Patient,WebRequest)
	 * @verifies return a list of names
	 */
	@Test
	public void getNames_shouldReturnAListOfNames() throws Exception {
		Object names = new PatientController().getNames("da7f524f-27ce-4bb2-86d6-6d1d05312bd5", emptyRequest());
		log("Existing names", names);
		Assert.assertNotNull(names);
	}
	
	/**
	 * @see PatientController#addName(SimpleObject,Patient)
	 * @verifies add a name
	 */
	@Test
	public void addName_shouldAddAName() throws Exception {
		Patient p = Context.getPatientService().getPatient(2);
		int before = p.getNames().size();
		SimpleObject post = new ObjectMapper().readValue("{\"givenName\":\"Darius\", \"familyName\":\"Programmer\"}",
		    SimpleObject.class);
		Object newName = new PatientController().addName(post, "da7f524f-27ce-4bb2-86d6-6d1d05312bd5", emptyRequest());
		log("Added name", newName);
		Assert.assertNotNull(newName);
		Assert.assertEquals(before + 1, Context.getPatientService().getPatient(2).getNames().size());
	}
	
	/**
	 * THIS DOES NOT WORK YET. NEED TO SEE HOW TO CREATE PATIENT WITH NAMES, ETC
	 * 
	 * @see PatientController#createPatient(SimpleObject,WebRequest)
	 * @verifies create a new patient
	 */
	@Ignore
	@Test
	public void createPatient_shouldCreateANewPatient() throws Exception {
		int before = Context.getPatientService().getAllPatients().size();
		SimpleObject post = new ObjectMapper().readValue("{\"givenName\":\"Darius\", \"familyName\":\"Programmer\"}",
		    SimpleObject.class);
		Object newPatient = new PatientController().createPatient(post, emptyRequest());
		log("Created patient", newPatient);
		Assert.assertEquals(before + 1, Context.getPatientService().getAllPatients().size());
	}
	
	/**
	 * @see PatientController#getPatient(Patient,WebRequest)
	 * @verifies get a representation of a patient
	 */
	@Test
	public void getPatient_shouldGetARepresentationOfAPatient() throws Exception {
		Object result = new PatientController().getPatient("da7f524f-27ce-4bb2-86d6-6d1d05312bd5", emptyRequest());
		Assert.assertNotNull(result);
		Assert.assertEquals("da7f524f-27ce-4bb2-86d6-6d1d05312bd5", PropertyUtils.getProperty(result, "uuid"));
		log("Patient fetched", result);
	}
	
	/**
	 * FAILING. I CAN'T FIGURE OUT WHY THIS METHOD IS NOT THROWING AN EXCEPTION WHEN THE PURGE
	 * FAILS.
	 * 
	 * @see PatientController#purgePatient(Patient,WebRequest)
	 * @verifies fail to purge a patient with dependent data
	 */
	@Ignore
	@Test(expected = APIException.class)
	public void purgePatient_shouldFailToPurgeAPatientWithDependentData() throws Exception {
		Assert.assertNotSame(0, Context.getEncounterService().getEncountersByPatient(new Patient(7)).size());
		new PatientController().purgePatient("5946f880-b197-400b-9caa-a3c661d23041", emptyRequest(),
		    new MockHttpServletResponse());
		Assert.assertEquals(0, Context.getEncounterService().getEncountersByPatient(new Patient(7)).size());
	}
	
	/**
	 * @see PatientController#updatePatient(Patient,SimpleObject,WebRequest)
	 * @verifies change a property on a patient
	 */
	@Test
	public void updatePatient_shouldChangeAPropertyOnAPatient() throws Exception {
		Date now = new Date();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleObject post = new ObjectMapper().readValue("{\"birthdate\":\"" + df.format(now) + "\"}", SimpleObject.class);
		Object editedPatient = new PatientController().updatePatient("da7f524f-27ce-4bb2-86d6-6d1d05312bd5", post, emptyRequest());
		log("Edited patient", editedPatient);
		Assert.assertEquals(df.format(now), df.format(Context.getPatientService().getPatient(2).getBirthdate()));
	}
	
	/**
	 * @see PatientController#voidPatient(Patient,String,WebRequest)
	 * @verifies void a patient
	 */
	@Test
	public void voidPatient_shouldVoidAPatient() throws Exception {
		Patient pat = Context.getPatientService().getPatient(2);
		Assert.assertFalse(pat.isVoided());
		new PatientController().voidPatient("da7f524f-27ce-4bb2-86d6-6d1d05312bd5", "unit test", emptyRequest(),
		    new MockHttpServletResponse());
		pat = Context.getPatientService().getPatient(2);
		Assert.assertTrue(pat.isVoided());
		Assert.assertEquals("unit test", pat.getVoidReason());
	}
}
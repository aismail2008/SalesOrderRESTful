package com.code.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.code.dal.DataAccess;
import com.code.dal.orm.Customer;
import com.code.enums.CustomerColumns;
import com.code.enums.FlagsEnum;
import com.code.enums.QueryNamesEnum;
import com.code.exceptions.BusinessException;
import com.code.exceptions.NoDataException;

@Path("/customers")
public class CustomerService extends BaseService {

	/**
	 * Create or update Customer record
	 * @param data
	 * @return Customer JSON Object
	 */
	@POST
	@Path("/update")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	public static Response updateCustomer(String data) {
		JSONObject result = new JSONObject();
		try {
			JSONObject jsonData = new JSONObject(data);
			Customer c = getCustomerFromJson(jsonData);
			if (c.getId() == -1) {
				c.setId(null);
				c.setCurrentCredit("0");
				DataAccess.addEntity(c);
				jsonData.put(CustomerColumns.id.getValue(), c.getId());
				jsonData.put(CustomerColumns.currentCredit.getValue(), "0");
			} else {
				DataAccess.updateEntity(c);
			}
			result = jsonData;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(Status.OK).entity(result.toString()).build();
	}

	/**
	 * Get Customer JSON Object by code
	 * @param code
	 * @return Customer JSON Object
	 */
	@POST
	@Path("/get")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	public static Response getCustomer(String code){
		JSONObject result = new JSONObject();
		try {
			JSONObject jsonData = new JSONObject(code);
			Customer c = searchCustomers(FlagsEnum.ALL.getCode(), jsonData.getString(CustomerColumns.code.getValue())).get(0);
			result = getJsonFromCustomer(c);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(Status.OK).entity(result.toString()).build();
	}

	/**
	 * Delete Customer Object by code
	 * @param code
	 * @return empty JSON
	 */
	@POST
	@Path("/delete")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	public static Response deleteCustomer(String code) {
		JSONObject result = new JSONObject();
		try {
			JSONObject jsonData = new JSONObject(code);
			Customer c = searchCustomers(FlagsEnum.ALL.getCode(), jsonData.getString(CustomerColumns.code.getValue())).get(0);
			DataAccess.deleteEntity(c);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(Status.OK).entity(result.toString()).build();
	}

	/**
	 * Listing all Customers as JSON Array
	 * @return Customers JSON Array
	 */
	@POST
	@Path("/list")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response listCustomers() {
		JSONObject result = new JSONObject();
		try {
			JSONArray customerJsonList = new JSONArray();
			List<Customer> CustomerList = getAllCustomer();
			for (Customer Customer : CustomerList) {
				customerJsonList.put(getJsonFromCustomer(Customer));
			}
			result.put("customerList", customerJsonList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(Status.OK).entity(result.toString()).build();
	}

	/**
	 * Convert Customer Object to JSON
	 * @param customer
	 * @return Customers JSON
	 * @throws BusinessException
	 */
	private static JSONObject getJsonFromCustomer(Customer customer) throws BusinessException {
		JSONObject customerJson = new JSONObject();
		try {
			customerJson.put(CustomerColumns.phoneOne.getValue(), customer.getPhoneOne());
			customerJson.put(CustomerColumns.phoneTwo.getValue(), customer.getPhoneTwo());
			customerJson.put(CustomerColumns.name.getValue(), customer.getName());
			customerJson.put(CustomerColumns.currentCredit.getValue(), customer.getCurrentCredit());
			customerJson.put(CustomerColumns.creditLimit.getValue(), customer.getCreditLimit());
			customerJson.put(CustomerColumns.address.getValue(), customer.getAddress());
			customerJson.put(CustomerColumns.code.getValue(), customer.getCode());
			customerJson.put(CustomerColumns.id.getValue(), customer.getId());
		} catch (JSONException e) {
			throw new BusinessException("error_jsonException");
		}
		return customerJson;
	}

	/**
	 * Convert Customer JSON to Object
	 * @param customerJson
	 * @return Customer Object
	 * @throws BusinessException
	 */
	private static Customer getCustomerFromJson(JSONObject customerJson) throws BusinessException {
		Customer customer = new Customer();
		try {
			customer.setCode(customerJson.getString(CustomerColumns.code.getValue()));
			customer.setId(customerJson.getLong(CustomerColumns.id.getValue()));
			customer.setPhoneOne(customerJson.getString(CustomerColumns.phoneOne.getValue()));
			customer.setPhoneTwo(customerJson.getString(CustomerColumns.phoneTwo.getValue()));
			customer.setName(customerJson.getString(CustomerColumns.name.getValue()));
			customer.setCurrentCredit(customerJson.getString(CustomerColumns.currentCredit.getValue()));
			customer.setCreditLimit(customerJson.getString(CustomerColumns.creditLimit.getValue()));
			customer.setAddress(customerJson.getString(CustomerColumns.address.getValue()));
		} catch (JSONException e) {
			throw new BusinessException("error_jsonException");
		}
		return customer;
	}

	// ----------Queries------------//
	/**
	 * General Query to load customers by id and/or code
	 * @param customerId
	 * @param code
	 * @return List of Customers
	 * @throws BusinessException
	 */
	private static List<Customer> searchCustomers(long customerId, String code) throws BusinessException {
		List<Customer> customerList = new ArrayList<Customer>();
		try {
			Map<String, Object> qParams = new HashMap<String, Object>();
			qParams.put("P_CUSTOMER_ID", customerId);
			qParams.put("P_CODE", code == null ? FlagsEnum.ALL.getCode() + "" : code);
			customerList = DataAccess.executeNamedQuery(Customer.class, QueryNamesEnum.CUSTOMERS_SEARCH_CUSTOMERS.getCode(), qParams);
			return customerList;
		} catch (NoDataException e) {
			return new ArrayList<Customer>();
		} catch (Exception e) {
			e.printStackTrace();
			throw new BusinessException("error_DBError");
		}
	}

	/**
	 * Get All Customers
	 * @return List of Customers
	 * @throws BusinessException
	 */
	public static List<Customer> getAllCustomer() throws BusinessException {
		return searchCustomers(FlagsEnum.ALL.getCode(), null);
	}

	/**
	 * Get Customer by Id
	 * @param customerId
	 * @return
	 * @throws BusinessException
	 */
	public static Customer getCustomerById(long customerId) throws BusinessException {
		List<Customer> customerList = searchCustomers(customerId, null);
		if (customerList.size() == 0) {
			throw new BusinessException("error_CustomerNotExist");
		}

		return customerList.get(0);
	}
}

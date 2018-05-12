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

import com.code.dal.CustomSession;
import com.code.dal.DataAccess;
import com.code.dal.orm.Customer;
import com.code.dal.orm.OrderLine;
import com.code.dal.orm.Product;
import com.code.dal.orm.SalesOrder;
import com.code.dal.orm.SalesOrderData;
import com.code.enums.FlagsEnum;
import com.code.enums.OrderLineColumns;
import com.code.enums.QueryNamesEnum;
import com.code.enums.SalesOrderColumns;
import com.code.exceptions.BusinessException;
import com.code.exceptions.NoDataException;

@Path("/salesorders")
public class SalesOrderService extends BaseService {
	/**
	 * Delete Sales order if exist by : 1- loads it's all order lines and delete
	 * them then updating product quantity 2- delete sales order and updating
	 * customer credit;
	 * 
	 * @param data
	 * @return response JSON of required data
	 */
	@POST
	@Path("/delete")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	public static Response deleteSalesOrder(String data) {
		JSONObject result = new JSONObject();
		CustomSession session = DataAccess.getSession();
		try {
			JSONObject jsonData = new JSONObject(data);
			
			SalesOrder s = searchSalesOrders(jsonData.getString(SalesOrderColumns.orderNumber.getValue())).get(0);
			
			List<OrderLine> olList = searchOrderLines(s.getId(), FlagsEnum.ALL.getCode());
			
			List<Product> productList = new ArrayList<Product>();
			for (int i = 0; i < olList.size(); i++) {
				OrderLine ol = olList.get(i);
				Product p = ProductService.getProductById(ol.getProductId());
				long newQuan = Long.valueOf(p.getQuantity()) + Long.valueOf(ol.getProductQuantity());
				p.setQuantity(newQuan + "");
				productList.add(p);
			}
			
			Customer c = CustomerService.getCustomerById(s.getCustomerId());
			double newCurrentCredit = Double.parseDouble(c.getCurrentCredit()) - Double.parseDouble(s.getTotalPrice());
			c.setCurrentCredit(newCurrentCredit + "");
			
			session.beginTransaction();
			
			for (int i = 0; i < olList.size(); i++) {
				DataAccess.deleteEntity(olList.get(i), session);
			}
			for (int i = 0; i < productList.size(); i++) {
				DataAccess.updateEntity(productList.get(i), session);
			}
			DataAccess.updateEntity(c, session);

			session.flushTransaction();
			DataAccess.deleteEntity(s, session);

			session.commitTransaction();
		} catch (Exception e) {
			session.rollbackTransaction();
			e.printStackTrace();
		} finally {
			session.close();
		}
		return Response.status(Status.OK).entity(result.toString()).build();
	}

	/**
	 * Updating Sales order if exist by : 1- check products balance in store is
	 * sufficient to requested order line 2- check customer Current Credit is
	 * sufficient to requested sales order 3- updating/insert sales order and
	 * updating customer Current Credit 4- updating/insert order lines of this
	 * sales order and updating product quantity
	 * 
	 * @param data
	 * @return response JSON of required data
	 */
	@POST
	@Path("/update")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	public static Response updateSalesOrder(String data) {
		CustomSession session = DataAccess.getSession();
		JSONObject result = new JSONObject();
		try {
			session.beginTransaction();

			JSONObject jsonData = new JSONObject(data);
			JSONObject salesOrderJson = (JSONObject) jsonData.get("salesOrderJson");

			SalesOrderData s = getSalesOrderFromJson(salesOrderJson);

			Customer c = CustomerService.getCustomerById(s.getCustomerId());

			if (s.getId() == null || s.getId() == -1) { // New Object
				s.setId(null);
				DataAccess.addEntity(s.getSalesOrder(), session);
				s.setId(s.getSalesOrder().getId());
				salesOrderJson.put(SalesOrderColumns.id.getValue(), s.getId());
				double newCurrentCredit = Double.parseDouble(c.getCurrentCredit()) + Double.parseDouble(s.getTotalPrice());
				c.setCurrentCredit(newCurrentCredit + "");
			} else { // updating old object
				SalesOrderData oldSalesOrder = searchSalesOrders(s.getId(), null).get(0);
				double addCurrentCredit = Double.parseDouble(c.getCurrentCredit()) + Double.parseDouble(s.getTotalPrice()) - Double.parseDouble(oldSalesOrder.getTotalPrice());
				c.setCurrentCredit(addCurrentCredit + "");
				DataAccess.updateEntity(s.getSalesOrder(), session);
			}
			if (Double.parseDouble(c.getCreditLimit()) < Double.parseDouble(c.getCurrentCredit())) {
				throw new BusinessException("invalid credit balance");
			}

			DataAccess.updateEntity(c, session);

			JSONArray orderItemsJsonArr = (JSONArray) jsonData.get("orderItemsJsonArr");
			for (int i = 0; i < orderItemsJsonArr.length(); i++) {
				JSONObject obj = (JSONObject) orderItemsJsonArr.get(i);
				List<OrderLine> olList = searchOrderLines(s.getId(), obj.getLong(OrderLineColumns.productId.getValue()));
				// if exist update else insert new order line
				if (olList.size() == 0) {
					OrderLine ol = new OrderLine();
					ol.setProductId(obj.getLong(OrderLineColumns.productId.getValue()));
					ol.setProductQuantity(obj.getString(OrderLineColumns.productQuantity.getValue()));
					ol.setSalesOrderId(s.getId());
					ol.setTotalPrice(obj.getString(OrderLineColumns.totalPrice.getValue()));
					ol.setUnitPrice(obj.getString(OrderLineColumns.unitPrice.getValue()));
					DataAccess.addEntity(ol, session);

					Product p = ProductService.getProductById(ol.getProductId());
					long newQuan = Long.valueOf(p.getQuantity()) - Long.valueOf(ol.getProductQuantity());
					if (newQuan < 0) {
						throw new BusinessException("invalid product store quantity");
					}
					p.setQuantity(newQuan + "");
					DataAccess.updateEntity(p, session);
				} else {
					OrderLine ol = olList.get(0);
					long difference = Long.parseLong(obj.getString(OrderLineColumns.productQuantity.getValue())) - Long.parseLong(ol.getProductQuantity());

					ol.setProductQuantity(obj.getString(OrderLineColumns.productQuantity.getValue()));
					ol.setTotalPrice(obj.getString(OrderLineColumns.totalPrice.getValue()));
					ol.setUnitPrice(obj.getString(OrderLineColumns.unitPrice.getValue()));
					DataAccess.updateEntity(ol, session);

					Product p = ProductService.getProductById(ol.getProductId());
					long newQuan = Long.valueOf(p.getQuantity()) - difference;
					if (newQuan < 0) {
						throw new BusinessException("invalid product store quantity");
					}
					p.setQuantity(newQuan + "");
					DataAccess.updateEntity(p, session);
				}
			}

			session.commitTransaction();
			result = jsonData;
		} catch (Exception e) {
			session.rollbackTransaction();
			e.printStackTrace();
		} finally {
			session.close();
		}
		return Response.status(Status.OK).entity(result.toString()).build();
	}

	/**
	 * Get Sales Order by OrderNumber
	 * 
	 * @param orderNumber
	 * @return response JSON of required data
	 * @throws BusinessException
	 */
	@POST
	@Path("/get")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	public static Response getSalesOrder(String orderNumber) throws BusinessException {
		JSONObject result = new JSONObject();
		try {
			JSONObject jsonData = new JSONObject(orderNumber);
			SalesOrderData s = searchSalesOrders(FlagsEnum.ALL.getCode(), jsonData.getString(SalesOrderColumns.orderNumber.getValue())).get(0);
			result.put("salesOrderJson", getJsonFromSalesOrder(s));

			// filling array of orderLines
			JSONArray orderLineArr = new JSONArray();
			List<OrderLine> orderLinesList = searchOrderLines(s.getId(), FlagsEnum.ALL.getCode());
			for (OrderLine ol : orderLinesList) {
				orderLineArr.put(getJsonFromOrderLine(ol));
			}
			result.put("orderItemsJsonArr", orderLineArr);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(Status.OK).entity(result.toString()).build();
	}

	/**
	 * List all Sales Order
	 * 
	 * @return response JSON of required data
	 * @throws BusinessException
	 */
	@POST
	@Path("/list")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response listSalesOrder() {
		JSONObject result = new JSONObject();
		try {
			JSONArray salesOrderJsonList = new JSONArray();
			List<SalesOrderData> salesOrderDataList = searchSalesOrders(FlagsEnum.ALL.getCode(), null);
			for (SalesOrderData salesOrder : salesOrderDataList) {
				salesOrderJsonList.put(getJsonFromSalesOrder(salesOrder));
			}
			result.put("salesorderList", salesOrderJsonList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(Status.OK).entity(result.toString()).build();
	}

	/**
	 * Converts model data to equivalent JSON
	 * 
	 * @param salesOrder
	 * @return equivalent JSONObject
	 * @throws BusinessException
	 */
	private static JSONObject getJsonFromOrderLine(OrderLine ol) throws BusinessException {
		JSONObject orderLineJson = new JSONObject();
		try {
			orderLineJson.put(OrderLineColumns.productId.getValue(), ol.getProductId());
			orderLineJson.put(OrderLineColumns.productQuantity.getValue(), ol.getProductQuantity());
			orderLineJson.put(OrderLineColumns.unitPrice.getValue(), ol.getUnitPrice());
			orderLineJson.put(OrderLineColumns.totalPrice.getValue(), ol.getTotalPrice());
		} catch (JSONException e) {
			throw new BusinessException("error_jsonException");
		}
		return orderLineJson;
	}

	/**
	 * Converts model data to equivalent JSON
	 * 
	 * @param salesOrder
	 * @return equivalent JSONObject
	 * @throws BusinessException
	 */
	private static JSONObject getJsonFromSalesOrder(SalesOrderData salesOrder) throws BusinessException {
		JSONObject salesOrderJson = new JSONObject();
		try {
			salesOrderJson.put(SalesOrderColumns.totalPrice.getValue(), salesOrder.getTotalPrice());
			salesOrderJson.put(SalesOrderColumns.orderNumber.getValue(), salesOrder.getOrderNumber());
			salesOrderJson.put(SalesOrderColumns.customerId.getValue(), salesOrder.getCustomerId());
			salesOrderJson.put(SalesOrderColumns.customerCode.getValue(), salesOrder.getCustomerCode());
			salesOrderJson.put(SalesOrderColumns.id.getValue(), salesOrder.getId());
		} catch (JSONException e) {
			throw new BusinessException("error_jsonException");
		}
		return salesOrderJson;
	}

	/**
	 * Converts JSON to equivalent model data
	 * 
	 * @param salesOrderJson
	 * @return equivalent SalesOrderData
	 * @throws BusinessException
	 */
	private static SalesOrderData getSalesOrderFromJson(JSONObject salesOrderJson) throws BusinessException {
		SalesOrderData salesOrder = new SalesOrderData();
		try {
			salesOrder.setTotalPrice(salesOrderJson.getString(SalesOrderColumns.totalPrice.getValue()));
			salesOrder.setOrderNumber(salesOrderJson.getString(SalesOrderColumns.orderNumber.getValue()));
			salesOrder.setCustomerId(salesOrderJson.getLong(SalesOrderColumns.customerId.getValue()));
			salesOrder.setId(salesOrderJson.getLong(SalesOrderColumns.id.getValue()));
		} catch (JSONException e) {
			throw new BusinessException("error_jsonException");
		}
		return salesOrder;
	}

	// ----------Queries------------//
	/**
	 * Query DB for all SalesOrderData
	 * 
	 * @param id
	 * @param orderNumber
	 * @return list of SalesOrderData
	 * @throws BusinessException
	 */
	private static List<SalesOrderData> searchSalesOrders(long id, String orderNumber) throws BusinessException {
		List<SalesOrderData> salesOrderDataList = new ArrayList<SalesOrderData>();
		try {
			Map<String, Object> qParams = new HashMap<String, Object>();
			qParams.put("P_ID", id);
			qParams.put("P_ORDER_NUMBER", orderNumber == null || orderNumber.length() == 0 ? FlagsEnum.ALL.getCode() + "" : orderNumber);
			salesOrderDataList = DataAccess.executeNamedQuery(SalesOrderData.class, QueryNamesEnum.SALES_ORDERS_SEARCH_SALES_ORDERS.getCode(), qParams);
			return salesOrderDataList;
		} catch (NoDataException e) {
			return new ArrayList<SalesOrderData>();
		} catch (Exception e) {
			e.printStackTrace();
			throw new BusinessException("error_DBError");
		}
	}

	/**
	 * Query DB for all SalesOrderData
	 * 
	 * @param id
	 * @param orderNumber
	 * @return list of SalesOrderData
	 * @throws BusinessException
	 */
	private static List<SalesOrder> searchSalesOrders(String orderNumber) throws BusinessException {
		List<SalesOrder> salesOrderDataList = new ArrayList<SalesOrder>();
		try {
			Map<String, Object> qParams = new HashMap<String, Object>();
			qParams.put("P_ORDER_NUMBER", orderNumber);
			salesOrderDataList = DataAccess.executeNamedQuery(SalesOrder.class, QueryNamesEnum.SALES_ORDERS_SEARCH_SALES_ORDERS_BY_NUMBER.getCode(), qParams);
			return salesOrderDataList;
		} catch (NoDataException e) {
			return new ArrayList<SalesOrder>();
		} catch (Exception e) {
			e.printStackTrace();
			throw new BusinessException("error_DBError");
		}
	}

	/**
	 * Query DB for all OrderLine
	 * 
	 * @param salesOrderId
	 * @param productId
	 * @return list of OrderLine
	 * @throws BusinessException
	 */
	private static List<OrderLine> searchOrderLines(long salesOrderId, long productId) throws BusinessException {
		List<OrderLine> orderLinesList = new ArrayList<OrderLine>();
		try {
			Map<String, Object> qParams = new HashMap<String, Object>();
			qParams.put("P_SALES_ORDER_ID", salesOrderId);
			qParams.put("P_PRODUCT_ID", productId);
			orderLinesList = DataAccess.executeNamedQuery(OrderLine.class, QueryNamesEnum.ORDER_LINES_SEARCH_ORDER_LINES.getCode(), qParams);
			return orderLinesList;
		} catch (NoDataException e) {
			return new ArrayList<OrderLine>();
		} catch (Exception e) {
			e.printStackTrace();
			throw new BusinessException("error_DBError");
		}
	}
}

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
import com.code.dal.orm.Product;
import com.code.enums.FlagsEnum;
import com.code.enums.ProductColumns;
import com.code.enums.QueryNamesEnum;
import com.code.exceptions.BusinessException;
import com.code.exceptions.NoDataException;

@Path("/products")
public class ProductService extends BaseService {
	/**
	 * Create or update Product record
	 * @param data
	 * @return Product JSON Object
	 * @throws BusinessException
	 */
	@POST
	@Path("/update")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	public static Response updateProduct(String data) {
		JSONObject result = new JSONObject();
		try {
			JSONObject jsonData = new JSONObject(data);
			Product p = getProductFromJson(jsonData);
			if (p.getId() == -1) {
				p.setId(null);
				DataAccess.addEntity(p);
				jsonData.put(ProductColumns.id.getValue(), p.getId());
			} else {
				DataAccess.updateEntity(p);
			}
			result = jsonData;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(Status.OK).entity(result.toString()).build();
	}

	/**
	 * Get Product JSON Object by code
	 * @param code
	 * @return Product JSON Object
	 */
	@POST
	@Path("/get")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	public static Response getProduct(String code) {
		JSONObject result = new JSONObject();
		try {
			Product p = null;
			JSONObject jsonData = new JSONObject(code);
			if(jsonData.has(ProductColumns.code.getValue()))
				 p = searchProducts(FlagsEnum.ALL.getCode(), jsonData.getString(ProductColumns.code.getValue())).get(0);
			else
				p = searchProducts(jsonData.getLong(ProductColumns.id.getValue()), null).get(0);
			result = getJsonFromProduct(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(Status.OK).entity(result.toString()).build();
	}

	/**
	 * Delete Product by code
	 * @param code
	 * @return empty JSON Object
	 */
	@POST
	@Path("/delete")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	public static Response deleteProduct(String code) {
		JSONObject result = new JSONObject();
		try {
			JSONObject jsonData = new JSONObject(code);
			Product p = searchProducts(FlagsEnum.ALL.getCode(), jsonData.getString(ProductColumns.code.getValue())).get(0);
			DataAccess.deleteEntity(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(Status.OK).entity(result.toString()).build();
	}

	/**
	 * Listing all Products as JSON Array
	 * @return Products JSON Array
	 */
	@POST
	@Path("/list")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response listProducts() {
		JSONObject result = new JSONObject();
		try {
			JSONArray productJsonList = new JSONArray();
			List<Product> productList = getAllProduct();
			for (Product product : productList) {
				productJsonList.put(getJsonFromProduct(product));
			}
			result.put("productList", productJsonList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(Status.OK).entity(result.toString()).build();
	}

	/**
	 * Convert product Object to JSON
	 * @param product
	 * @return product JSON
	 * @throws BusinessException
	 */
	private static JSONObject getJsonFromProduct(Product product) throws BusinessException {
		JSONObject productJson = new JSONObject();
		try {
			productJson.put(ProductColumns.description.getValue(), product.getDescription());
			productJson.put(ProductColumns.price.getValue(), product.getPrice());
			productJson.put(ProductColumns.quantity.getValue(), product.getQuantity());
			productJson.put(ProductColumns.code.getValue(), product.getCode());
			productJson.put(ProductColumns.id.getValue(), product.getId());
		} catch (JSONException e) {
			throw new BusinessException("error_jsonException");
		}
		return productJson;
	}

	/**
	 * Convert product JSON to Object
	 * @param productJson
	 * @return product Object
	 * @throws BusinessException
	 */
	private static Product getProductFromJson(JSONObject productJson) throws BusinessException {
		Product product = new Product();
		try {
			product.setDescription(productJson.getString(ProductColumns.description.getValue()));
			product.setPrice(productJson.getString(ProductColumns.price.getValue()));
			product.setQuantity(productJson.getString(ProductColumns.quantity.getValue()));
			product.setCode(productJson.getString(ProductColumns.code.getValue()));
			product.setId(productJson.getLong(ProductColumns.id.getValue()));
		} catch (JSONException e) {
			throw new BusinessException("error_jsonException");
		}
		return product;
	}

	// ----------Queries------------//
	/**
	 * General Query to load Product by id and/or code
	 * @param productId
	 * @param code
	 * @return List of Products
	 * @throws BusinessException
	 */
	private static List<Product> searchProducts(long productId, String code) throws BusinessException {
		List<Product> productList = new ArrayList<Product>();
		try {
			Map<String, Object> qParams = new HashMap<String, Object>();
			qParams.put("P_PRODUCT_ID", productId);
			qParams.put("P_CODE", code == null ? FlagsEnum.ALL.getCode() + "" : code);
			productList = DataAccess.executeNamedQuery(Product.class, QueryNamesEnum.PRODUCTS_SEARCH_PRODUCTS.getCode(), qParams);
			return productList;
		} catch (NoDataException e) {
			return new ArrayList<Product>();
		} catch (Exception e) {
			e.printStackTrace();
			throw new BusinessException("error_DBError");
		}
	}

	/**
	 * Get All Products
	 * @return List of Products
	 * @throws BusinessException
	 */
	public static List<Product> getAllProduct() throws BusinessException {
		return searchProducts(FlagsEnum.ALL.getCode(), null);
	}

	/**
	 * Get Product by Id
	 * @param productId
	 * @return Product Object
	 * @throws BusinessException
	 */
	public static Product getProductById(long productId) throws BusinessException {
		List<Product> productList = searchProducts(productId, null);
		if (productList.size() == 0) {
			throw new BusinessException("error_productNotExist");
		}

		return productList.get(0);
	}
}

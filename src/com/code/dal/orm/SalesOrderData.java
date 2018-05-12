package com.code.dal.orm;

import javax.persistence.Transient;
import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.code.dal.BaseEntity;

@NamedQueries({
	  @NamedQuery(name = "SalesOrderData.searchSalesOrders", 
	              query= " select s " +
	              		 " from SalesOrderData s " +
	            		 " where (:P_ID = -1 or :P_ID = s.id)" +
	            		 " and (:P_ORDER_NUMBER = '-1' or :P_ORDER_NUMBER = s.orderNumber)" +
	              		 " order by s.id "
  )
})

@SuppressWarnings("serial")
@Entity
@Table(name = "SALES_ORDER_VW")
public class SalesOrderData extends BaseEntity implements Serializable {
	private Long id;
	private Long customerId;
	private String orderNumber;
	private String totalPrice;
	private String customerCode;
	
	private SalesOrder salesOrder;
	
	public SalesOrderData(){
		salesOrder = new SalesOrder();
	}

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.salesOrder.setId(id);
		this.id = id;
	}

	@Basic
	@Column(name = "CUSTOMER_ID")
	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.salesOrder.setCustomerId(customerId);
		this.customerId = customerId;
	}

	@Basic
	@Column(name = "ORDER_NUMBER")
	public String getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		this.salesOrder.setOrderNumber(orderNumber);
		this.orderNumber = orderNumber;
	}

	@Basic
	@Column(name = "TOTAL_PRICE")
	public String getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(String totalPrice) {
		this.salesOrder.setTotalPrice(totalPrice);
		this.totalPrice = totalPrice;
	}

	@Basic
	@Column(name = "CUSTOMER_CODE")
	public String getCustomerCode() {
		return customerCode;
	}

	public void setCustomerCode(String customerCode) {
		this.customerCode = customerCode;
	}

	@Transient
	public SalesOrder getSalesOrder() {
		return salesOrder;
	}

	public void setSalesOrder(SalesOrder salesOrder) {
		this.salesOrder = salesOrder;
	}
}

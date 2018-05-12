package com.code.dal.orm;

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
	  @NamedQuery(name = "SalesOrder.searchSalesOrders", 
	              query= " select s " +
	              		 " from SalesOrder s " +
	            		 " where (s.orderNumber = :P_ORDER_NUMBER)" +
	              		 " order by s.id "
)
})

@SuppressWarnings("serial")
@Entity
@Table(name = "SALES_ORDERS")
public class SalesOrder extends BaseEntity implements Serializable {
	private Long id;
	private Long customerId;
	private String orderNumber;
	private String totalPrice;

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Basic
	@Column(name = "CUSTOMER_ID")
	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	@Basic
	@Column(name = "ORDER_NUMBER")
	public String getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	@Basic
	@Column(name = "TOTAL_PRICE")
	public String getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(String totalPrice) {
		this.totalPrice = totalPrice;
	}
}

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
	  @NamedQuery(name = "OrderLine.searchOrderLines", 
	              query= " select o " +
	              		 " from OrderLine o " +
	            		 " where (:P_SALES_ORDER_ID = -1 or :P_SALES_ORDER_ID = o.salesOrderId)" +
	            		 " and (:P_PRODUCT_ID = -1 or :P_PRODUCT_ID = o.productId)" +
	              		 " order by o.id "
)
})

@SuppressWarnings("serial")
@Entity
@Table(name = "ORDER_LINES")
public class OrderLine extends BaseEntity implements Serializable {
	private Long id;
	private Long salesOrderId;
	private Long productId;
	private String productQuantity;
	private String unitPrice;
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
	@Column(name = "SALES_ORDER_ID")
	public Long getSalesOrderId() {
		return salesOrderId;
	}

	public void setSalesOrderId(Long salesOrderId) {
		this.salesOrderId = salesOrderId;
	}

	@Basic
	@Column(name = "PRODUCT_ID")
	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	@Basic
	@Column(name = "PRODUCT_QUANTITY")
	public String getProductQuantity() {
		return productQuantity;
	}

	public void setProductQuantity(String productQuantity) {
		this.productQuantity = productQuantity;
	}

	@Basic
	@Column(name = "UNIT_PRICE")
	public String getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(String unitPrice) {
		this.unitPrice = unitPrice;
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

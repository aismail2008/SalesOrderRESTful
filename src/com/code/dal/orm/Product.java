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
	  @NamedQuery(name = "product.searchProducts", 
	              query= " select p " +
	              		 " from Product p " +
	              		 " where (:P_PRODUCT_ID = -1 or p.id = :P_PRODUCT_ID) " +
	              		 " and (:P_CODE = '-1' or p.code = :P_CODE) " +
	              		 " order by p.id "
    )
})

@SuppressWarnings("serial")
@Entity
@Table(name = "PRODUCTS")
public class Product extends BaseEntity implements Serializable {
	private Long id;
	private String code;
	private String price;
	private String quantity;
	private String description;

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
	@Column(name = "CODE")
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Basic
	@Column(name = "PRICE")
	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	@Basic
	@Column(name = "QUANTITY")
	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	@Basic
	@Column(name = "DESCRIPTION")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}

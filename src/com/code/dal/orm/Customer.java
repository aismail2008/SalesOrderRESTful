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
	  @NamedQuery(name = "customer.searchCustomers", 
	              query= " select c " +
	              		 " from Customer c " +
	              		 " where (:P_CUSTOMER_ID = -1 or c.id = :P_CUSTOMER_ID) " +
	              		 " and (:P_CODE = '-1' or c.code = :P_CODE) " +
	              		 " order by c.id "
  )
})

@SuppressWarnings("serial")
@Entity
@Table(name = "CUSTOMERS")
public class Customer extends BaseEntity implements Serializable {
	private Long id;
	private String name;
	private String code;
	private String address;
	private String phoneOne;
	private String phoneTwo;
	private String creditLimit;
	private String currentCredit;

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
	@Column(name = "NAME")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
	@Column(name = "ADDRESS")
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Basic
	@Column(name = "PHONE_ONE")
	public String getPhoneOne() {
		return phoneOne;
	}

	public void setPhoneOne(String phoneOne) {
		this.phoneOne = phoneOne;
	}

	@Basic
	@Column(name = "PHONE_TWO")
	public String getPhoneTwo() {
		return phoneTwo;
	}

	public void setPhoneTwo(String phoneTwo) {
		this.phoneTwo = phoneTwo;
	}

	@Basic
	@Column(name = "CREDIT_LIMIT")
	public String getCreditLimit() {
		return creditLimit;
	}

	public void setCreditLimit(String creditLimit) {
		this.creditLimit = creditLimit;
	}

	@Basic
	@Column(name = "CURRENT_CREDIT")
	public String getCurrentCredit() {
		return currentCredit;
	}

	public void setCurrentCredit(String currentCredit) {
		this.currentCredit = currentCredit;
	}
}

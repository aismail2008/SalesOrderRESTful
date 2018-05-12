package com.code.enums;


public enum OrderLineColumns {
	unitPrice("unitPrice"),
	totalPrice("totalPrice"),
	productQuantity("productQuantity"), 
	productId("productId"), 
	salesOrderId("salesOrderId"), 
	id("id");
	
	private String value;
    
    private OrderLineColumns(String value){
        this.value = value;
    }
  
    public String getValue(){return value;}
}

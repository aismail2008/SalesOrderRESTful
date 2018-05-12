package com.code.enums;


public enum ProductColumns {
	code("code"), 
	description("description"),
	price("price"), 
	quantity("quantity"), 
	id("id");

	private String value;
    
    private ProductColumns(String value){
        this.value = value;
    }
  
    public String getValue(){return value;}
}

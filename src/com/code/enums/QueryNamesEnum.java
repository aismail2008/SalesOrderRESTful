
package com.code.enums;

public enum QueryNamesEnum {
    
    PRODUCTS_SEARCH_PRODUCTS("product.searchProducts"),
    
    CUSTOMERS_SEARCH_CUSTOMERS("customer.searchCustomers"),
    
    SALES_ORDERS_SEARCH_SALES_ORDERS("SalesOrderData.searchSalesOrders"),
    SALES_ORDERS_SEARCH_SALES_ORDERS_BY_NUMBER("SalesOrder.searchSalesOrders"),
    
    ORDER_LINES_SEARCH_ORDER_LINES("OrderLine.searchOrderLines")
   ;
    
    private String code;
      
    private QueryNamesEnum(String code){
        this.code = code;
    }
  
    public String getCode(){return code;}
}
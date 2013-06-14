<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd"> 
<html>
   <head>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
      <title>Cashier</title>
   </head>
   <body>
      <p>
      	Address to pay: <b><c:out value="${btcAddress}"/></b>
      </p>
      
      <p>
      	<a href="/btcashier/cashier/verify/<c:out value="${sale.merchant.mid}"/>/<c:out value="${token}"/>/<c:out value="${btcAddress}"/>">verifier</a>
      </p>
      
   </body>
</html>
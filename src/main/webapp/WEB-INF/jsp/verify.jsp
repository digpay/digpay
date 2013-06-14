<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd"> 
<html>
   <head>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
      <title>Cashier - verify</title>
   </head>
   <body>
      <p>
      
        <c:forEach items="${deposits}" var="deposit">
        	  Time: <c:out value="${deposit.prettyPrintTime}"></c:out><br/>  
              Amount: <c:out value="${deposit.prettyPrintAmount}"></c:out><br/>
              Tx: <c:out value="${deposit.transactionIdAndN}"></c:out><br/>
              <hr/>
        </c:forEach>  
      	
      </p>
   </body>
</html>
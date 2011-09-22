<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" trimDirectiveWhitespaces="true"
import="com.zipeg.gae.*"
%>
<!DOCTYPE html>
<html>
  <head>
    <%@include file="head.jspf"%>
  </head>
  <body>
    <% if (!str.isEmpty(Context.get().view)) { %>
      <jsp:include page="<%=\"/WEB-INF/views/\" + Context.get().view + \".jspf\"%>" />
    <% } %>
    <%=Context.get().body()%>
  </body>
</html>

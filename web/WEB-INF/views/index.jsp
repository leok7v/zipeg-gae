<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" trimDirectiveWhitespaces="true"
import="com.zipeg.gae.*"
%>
<html>
  <head><title>index.jsp</title></head>
  <body>
  <p>
  This is index.jsp - it is jsp script w/o controller<br />
  that is rendered via servlet/dispatcher invocation.<br />
  svn revision = <%=Context.get().revision%>
  </p>
  <p><a href="<%=Context.get().server%>test.html" >test.html</a></p>
  <p><a href="<%=Context.get().server%>hello?date=1961-12-31T23:59:59.999Z&number=153&big=9223372036854775807&string=world&b=true">hello test</a></p>
  <p><a href="<%=Context.get().server%>ping?date=1961-12-31T23:59:59.999Z&number=153&big=9223372036854775807&string=world&b=true">ping test</a></p>
  <p><a href="<%=Context.get().server%>mvc?date=1961-12-31T23:59:59.999Z&number=153&big=9223372036854775807&string=world&b=true">mvc test</a></p>
  <p><a href="<%=Context.get().server%>fragment">fragment (fragment.jspf) test</a></p>
  <p><a href="<%=Context.get().server%>layout/page.jsp" >/layout/page.jsp</a> should not be accessible</p>
  </body>
</html>
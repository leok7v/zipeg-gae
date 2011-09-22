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
  <p><a href="<%=Context.get().serverURL%>test.html" >test.html</a></p>
  <p><a href="<%=Context.get().serverURL%>hello?date=1961-12-31T23:59:59.999Z&number=153&big=9223372036854775807&string=world&b=true">hello test</a></p>
  <p><a href="<%=Context.get().serverURL%>ping?date=1961-12-31T23:59:59.999Z&number=153&big=9223372036854775807&string=world&b=true">ping test</a></p>
  <p><a href="<%=Context.get().serverURL%>mvc?date=1961-12-31T23:59:59.999Z&number=153&big=9223372036854775807&string=world&b=true">mvc test</a></p>
  <p><a href="<%=Context.get().serverURL%>fragment">fragment (fragment.jspf) test</a></p>
  <p><a href="<%=Context.get().serverURL%>layout/page.jsp" >/layout/page.jsp</a> should not be accessible</p>
  <br />
  <p><a href="<%=Context.get().serverURL%>signin" >signin</a></p>
  <p><a href="<%=Context.get().serverURL%>signout" >signout</a></p>
  </body>
</html>
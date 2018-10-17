<%@ page import="java.util.Properties" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head><title>Customer Version</title></head>

<%
    Properties versionProperties = new Properties();
    versionProperties.load(getClass().getResourceAsStream("/version.properties"));
%>

<body>
CI Project Name = <%= versionProperties.getProperty("build.project") %><br/>
CI Build Number = <%= versionProperties.getProperty("build.number") %><br/>
CI Build VCS Number = <%= versionProperties.getProperty("build.revision") %>
Project Version = <%= versionProperties.getProperty("pom.version") %>
</body>
</html>

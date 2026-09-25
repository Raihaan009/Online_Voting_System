<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:if test="${not empty sessionScope.currentUser && !sessionScope.currentUser.profileComplete}">
    <c:redirect url="/voter/profile?warning=Please+complete+your+official+student+profile+before+participating+in+any+elections." />
</c:if>
<jsp:forward page="/voter/vote.jsp" />

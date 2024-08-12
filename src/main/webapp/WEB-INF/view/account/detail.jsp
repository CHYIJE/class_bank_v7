<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<!-- header.jsp  -->
<%@ include file="/WEB-INF/view/layout/header.jsp"%>

<!-- start of content.jsp(xxx.jsp)   -->
<div class="col-sm-8">
	<h2>계좌 상세 보기(인증)</h2>
	<h5>Bank App에 오신걸 환영합니다</h5>

	<div class="bg-light p-md-5">
		<div class="user--box">
			${principal.username}님 계좌 <br> 계좌 번호 : ${account.number} <br> 잔액 :
			<fmt:formatNumber value="${account.balance}" type="currency" />
		</div>
		<br>

		<div>
			<a href="/account/detail/${account.id}?type=all&currentPageNum=1" class="btn btn-outline-primary">전체</a>&nbsp; <a
				href="/account/detail/${account.id}?type=deposit&currentPageNum=1" class="btn btn-outline-primary">입금</a>&nbsp; <a
				href="/account/detail/${account.id}?type=withdrawal&currentPageNum=1" class="btn btn-outline-primary">출금</a>&nbsp;
		</div>
		&nbsp;
		<table class="table table-striped">
			<thead>
				<tr>
					<th>ID</th>
					<th>날짜</th>
					<th>보낸 이</th>
					<%--sender --%>
					<th>받은 이</th>
					<%--receiver --%>
					<th>입출금 금액</th>
					<%--amount --%>
					<th>계좌 잔액</th>
					<%--balance --%>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="historyAccount" items="${historyList}">
					<tr>
						<th>${historyAccount.id}</th>
						<th><fmt:formatDate value="${historyAccount.createdAt}" pattern="yyyy년 MM월 dd일 hh시 mm분 ss초" /></th>
						<%--연-월-일 시:분:초 --%>
						<th>${historyAccount.sender}</th>
						<%--보낸 이 --%>
						<th>${historyAccount.receiver}</th>
						<%--받은 이 --%>
						<th><fmt:formatNumber value="${historyAccount.amount}" type="currency" /></th>
						<%--입출금 금액 --%>
						<th><fmt:formatNumber value="${historyAccount.balance}" type="currency" /></th>
						<%--현재 계좌 잔액 --%>
					</tr>
				</c:forEach>
			</tbody>
		</table>
		<br>
		<div class="d-flex justify-content-center">
			<ul class="pagination">
				<%--previous page link--%>
				<li class="page-item  <c:if test='${currentPage == 1}'> disabled</c:if>">
				<a class="page-link" href="?type=${type}&page=${currentPage-1}&size=${size}">Previous</a>
				</li>
				<%--page numbers--%>
				<!-- [Previous]  1 2 3 4 5 6 7 8   [Next] -->
				<c:forEach begin="1" end="${totalPages}" var="page">
					<li class="page-item <c:if test='${page == currentPage}'> active </c:if>">
					<a class="page-link" href="?type=${type}&page=${page}&size=${size}">${page}</a>
					</li> 
				</c:forEach>
				<%--next page link--%>
				<li class="page-item <c:if test='${currentPage == totalPages}'> disabled</c:if>">
				<a class="page-link" href="?type=${type}&page=${currentPage+1}&size=${size}">next</a>
				</li>
			</ul>
		</div>

	</div>
</div>
<!-- end of col-sm-8  -->
</div>
</div>

<!-- footer.jsp  -->
<%@ include file="/WEB-INF/view/layout/footer.jsp"%>
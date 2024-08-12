<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!-- header.jsp  -->
<%@ include file="/WEB-INF/view/layout/header.jsp"%>

<!-- start of content.jsp(xxx.jsp)   -->
<div class="col-sm-8">
	<h2>계좌 상세 보기(인증)</h2>
	<h5>Bank App에 오신걸 환영합니다</h5>
	<div class="bg-light p-md-5">
		<div class="user--box">
			${princiapl.username}님 계좌<br> 계좌번호 : ${account.number} <br> 잔액 : ${account.formatKoreanWon(account.balance)}원
		</div>
		<br>
		<div>
			<a href="/account/detail/${account.id}?type=all" class="btn btn-outline-primary">전체</a>&nbsp;
			<a href="/account/detail/${account.id}?type=deposit" class="btn btn-outline-primary">입금</a>&nbsp;
			<a href="/account/detail/${account.id}?type=withdrawal" class="btn btn-outline-primary">출금</a>&nbsp;
		</div>
		<table class="table table-striped">
			<thead>
				<tr>
					<th>날짜</th>
					<th>보낸이</th>
					<th>받는이</th>
					<th>입출금 금액</th>
					<th>계좌잔액</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="historyAccount" items= "${historyList}">
				<tr>
					<th><fmt:formatDate value="${historyAccount.createdAt}" pattern="yyyy-mm-dd"/></th>
					<th>${historyAccount.sender}</th>
					<th>${historyAccount.receiver}</th>
					<th><fmt:formatNumber value="${historyAccount.amount}" pattern=" #,### 원"/></th>
					<th><fmt:formatNumber value="${historyAccount.balance}" pattern="#,### 원"/></th>
				</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>

</div>
<!-- end of col-sm-8  -->
</div>
</div>

<!-- footer.jsp  -->
<%@ include file="/WEB-INF/view/layout/footer.jsp"%>
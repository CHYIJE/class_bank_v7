package com.tenco.bank.repository.interfaces;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tenco.bank.repository.model.Account;

// AccountRepository 인터페이스와 account.xml 파일을 매칭 시킨다.
@Mapper
public interface AccountRepository {
	public int insert(Account account);

	public int updateById(Account account);

	public int deletById(Integer id, String name);

	// interface 파라미터명과 xml에 사용할 변수명을 다르게 사용해야 된다면 @param 애노테이션을
	// 사용할 수 있다. 그리고 2개 이상의 파라미터를 사용할 경우 반드시 사용하자!
	public List<Account> findByUserId(@Param("userId") Integer principalId);

	// --> account id 값으로 계좌 정보 조회
	public Account findByNumber(@Param("number") String id);

	// 코드 추가 예정
	public Account findByAccountId(Integer accountId);
	
	public List<Account> findByAccountIdForPage(@Param("userId")Integer principalId, 
												@Param("limit")Integer limit,
												@Param("offset")Integer offset);
	
}

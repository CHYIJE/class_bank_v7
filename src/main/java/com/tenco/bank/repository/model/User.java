package com.tenco.bank.repository.model;



import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class User {
	private Integer id;
	private String username;
	private String password;
	private String fullname;
	private String originFileName;
	private String uploadFileName;
	private Timestamp createdAt;
		
	public String setUpUserImage() {
		 
				if(originFileName == null) {
					return "https://picsum.photos/id/1/350";
				} else if (originFileName != null && uploadFileName != null) {
					return "/images/uploads/" + uploadFileName;
				} else if (uploadFileName == null) {
					return this.originFileName;
				}
				
				return uploadFileName;
	}
	
}

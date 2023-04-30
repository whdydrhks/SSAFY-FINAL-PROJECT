package com.ssafy.nanumi.api.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class UserJoinDTO {

    private String email;
    private String nickname;
    private String password;
    private long address_id;

    @Builder
    public UserJoinDTO(String email, String nickname, String password, long address_id) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.address_id = address_id;
    }
}

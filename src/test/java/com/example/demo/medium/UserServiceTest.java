package com.example.demo.medium;


import com.example.demo.common.domain.exception.CertificationCodeNotMatchedException;
import com.example.demo.common.domain.exception.ResourceNotFoundException;
import com.example.demo.user.domain.User;
import com.example.demo.user.domain.UserStatus;
import com.example.demo.user.domain.UserCreate;
import com.example.demo.user.domain.UserUpdate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@TestPropertySource("classpath:test-application.properties")
@SqlGroup({
    @Sql(value = "/sql/user-service-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    ,@Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
class UserServiceTest {

    @Autowired
    private com.example.demo.user.service.UserService userService;
    @MockBean   // 기존 JavaMailSender의 빈 객체를 Mock으로 선언된 객체로 덮어씌우는 것
    private JavaMailSender javaMailSender;

    @Test
    void getByEmail은_ACTIVE_상태인_유저를_찾아올_수_있다() {
        User result = userService.getByEmail("jeongdalma@gmail.com");

        Assertions.assertThat(result.getNickname()).isEqualTo("jeongdalma");
    }

    @Test
    void getByEmail은_PENDING_상태인_유저를_찾아올_수_없다() {

        Assertions.assertThatThrownBy(() -> {
            userService.getByEmail("jeongdalma2@gmail.com");
        }).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById는_ACTIVE_상태인_유저를_찾아올_수_있다() {
        User result = userService.getById(1L);

        Assertions.assertThat(result.getNickname()).isEqualTo("jeongdalma");
    }

    @Test
    void getById는_PENDING_상태인_유저를_찾아올_수_없다() {

        Assertions.assertThatThrownBy(() -> {
            userService.getById(2L);
        }).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void 유저를_생성할_수_있다() {
        UserCreate user = UserCreate.builder()
                .email("test@gmail.com")
                .address("Seoul")
                .nickname("test")
                .build();
        BDDMockito.doNothing()
                .when(javaMailSender)
                .send(any(SimpleMailMessage.class));

        User createdUser = userService.create(user);

        Assertions.assertThat(createdUser.getId()).isNotNull();
        Assertions.assertThat(createdUser.getStatus()).isEqualTo(UserStatus.PENDING);
        // UUID 값을 테스트하고 싶지만 현재 테스트할 수 있는 방법이 없음
    }

    @Test
    void 유저를_수정할_수_있다() {
        UserUpdate update = UserUpdate.builder()
                .address("Busan")
                .nickname("test2")
                .build();

        User user = userService.update(1L, update);

        Assertions.assertThat(user.getId()).isNotNull();
        Assertions.assertThat(user.getAddress()).isEqualTo("Busan");
        Assertions.assertThat(user.getNickname()).isEqualTo("test2");
    }

    @Test
    void user를_로그인_시키면_마지막_로그인_시간이_변경된다() {
        userService.login(1L);

        User user = userService.getById(1L);
        Assertions.assertThat(user.getLastLoginAt()).isGreaterThan(0L);
        // 최종 로그인 시간을 테스트할 수 있는 방법이 현재 없음
    }

    @Test
    void PENDING_상태의_사용자는_인증_코드로_ACTIVE_시킬_수_있다() {
        userService.verifyEmail(2, "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaab");

        User user = userService.getById(2L);
        Assertions.assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void PENDING_상태의_사용자는_잘못된_인증_코드를_받으면_예외를_던진다() {
        Assertions.assertThatThrownBy(() -> {
            userService.verifyEmail(2, "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaab22");
        }).isInstanceOf(CertificationCodeNotMatchedException.class);
    }
}

package com.example.demo.user.controller;

import com.example.demo.common.domain.exception.CertificationCodeNotMatchedException;
import com.example.demo.common.domain.exception.ResourceNotFoundException;
import com.example.demo.mock.TestContainer;
import com.example.demo.user.controller.port.AuthenticationService;
import com.example.demo.user.controller.port.UserReadService;
import com.example.demo.user.controller.response.MyProfileResponse;
import com.example.demo.user.controller.response.UserResponse;
import com.example.demo.user.domain.User;
import com.example.demo.user.domain.UserStatus;
import com.example.demo.user.domain.UserUpdate;
import com.example.demo.user.infrastructure.UserEntity;
import com.example.demo.user.infrastructure.UserJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class UserControllerTest {

    @Test
    void 사용자는_특정_유저의_정보를_개인정보는_소거된_채_전달_받을_수_있다() {
        TestContainer testContainer = TestContainer.builder().build();

        testContainer.userRepository.save(User.builder()
            .id(1L)
            .email("jeongdalma@gmail.com")
            .nickname("jeongdalma")
            .address("Seoul")
            .certificationCode("test-code")
            .status(UserStatus.ACTIVE)
            .lastLoginAt(0L)
            .build()
        );
        ResponseEntity<UserResponse> response = testContainer.userController.getUserById(1);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).getId()).isEqualTo(1L);
        assertThat(Objects.requireNonNull(response.getBody()).getEmail()).isEqualTo("jeongdalma@gmail.com");
        assertThat(Objects.requireNonNull(response.getBody()).getNickname()).isEqualTo("jeongdalma");
        assertThat(Objects.requireNonNull(response.getBody()).getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void 사용자는_존재하지_않는_유저의_아이디로_api를_호출할_경우_404_응답을_받는다() {
        TestContainer testContainer = TestContainer.builder()
                .build();

        assertThatThrownBy(() -> {
            testContainer.userController.getUserById(1);
        }).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void 사용자는_인증_코드로_계정을_활성화_시킬_수_있다() {
        // given
        TestContainer testContainer = TestContainer.builder()
                .build();
        testContainer.userRepository.save(User.builder()
                .id(1L)
                .email("jeongdalma@gmail.com")
                .nickname("jeongdalma")
                .address("Seoul")
                .status(UserStatus.PENDING)
                .certificationCode("test-code")
                .lastLoginAt(100L)
                .build());

        // when
        ResponseEntity<Void> result = testContainer.userController.verifyEmail(1, "test-code");

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(302));
        assertThat(testContainer.userRepository.getById(1).getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void 사용자의_인증_코드가_일치하지_않을_경우_권한_없음_예외를_내려준다() {
        // given
        TestContainer testContainer = TestContainer.builder()
                .build();
        testContainer.userRepository.save(User.builder()
                .id(1L)
                .email("jeongdalma@gmail.com")
                .nickname("jeongdalma")
                .address("Seoul")
                .status(UserStatus.PENDING)
                .certificationCode("test-code")
                .lastLoginAt(100L)
                .build());

        // when
        // then
        assertThatThrownBy(() -> {
            testContainer.userController.verifyEmail(1, "unknown-code");
        }).isInstanceOf(CertificationCodeNotMatchedException.class);
    }

    @Test
    void 사용자는_내_정보를_수정할_수_있다() {
        // given
        TestContainer testContainer = TestContainer.builder()
                .build();
        testContainer.userRepository.save(User.builder()
                .id(1L)
                .email("jeongdalma@gmail.com")
                .nickname("jeongdalma")
                .address("Seoul")
                .status(UserStatus.ACTIVE)
                .certificationCode("test-code")
                .lastLoginAt(100L)
                .build());

        // when
        ResponseEntity<MyProfileResponse> result = testContainer.myInfoController.update("jeongdalma@gmail.com", UserUpdate.builder()
                .address("Busan")
                .nickname("updated jeongdalma")
                .build());

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getEmail()).isEqualTo("jeongdalma@gmail.com");
        assertThat(result.getBody().getNickname()).isEqualTo("updated jeongdalma");
        assertThat(result.getBody().getLastLoginAt()).isEqualTo(100);
        assertThat(result.getBody().getAddress()).isEqualTo("Busan");
        assertThat(result.getBody().getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

}

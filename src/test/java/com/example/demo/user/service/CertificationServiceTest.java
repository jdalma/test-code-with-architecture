package com.example.demo.user.service;

import com.example.demo.mock.FakeMailSender;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class CertificationServiceTest {

    @Test
    void 이메일과_컨텐츠가_제대로_만들어져서_보내지는지_테스트한다() {
        FakeMailSender fakeMailSender = new FakeMailSender();
        CertificationService certificationService = new CertificationService(fakeMailSender);

        certificationService.sendCertificationEmail("jeongdalma@gmail.com", 1L, "test-code");

        Assertions.assertThat(fakeMailSender.email).isEqualTo("jeongdalma@gmail.com");
        Assertions.assertThat(fakeMailSender.title).isEqualTo("Please certify your email address");
        Assertions.assertThat(fakeMailSender.content).isEqualTo(
                "Please click the following link to certify your email address: http://localhost:8080/api/users/1/verify?certificationCode=test-code");
    }
}

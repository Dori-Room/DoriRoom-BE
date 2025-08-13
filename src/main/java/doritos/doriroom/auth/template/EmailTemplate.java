package doritos.doriroom.auth.template;

import org.springframework.stereotype.Component;

@Component
public class EmailTemplate {
    public static class Subject { // 이메일 제목
        public static final String VERIFICATION = "[DoriRoom] 이메일 인증번호입니다";
        public static final String PASSWORD_RESET = "[DoriRoom] 비밀번호 재설정 링크입니다";
    }

    public String createVerificationEmailContent(String verificationCode) { // 이메일 content 예시
        return """
            <div style="max-width: 600px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif;">
                <h2 style="color: #333;">이메일 인증</h2>
                <p>안녕하세요! DoriRoom입니다.</p>
                <p>아래 인증번호를 입력하여 이메일 인증을 완료해주세요.</p>
                <div style="background-color: #f5f5f5; padding: 20px; text-align: center; margin: 20px 0;">
                    <h1 style="color: #007bff; margin: 0; letter-spacing: 5px;">%s</h1>
                </div>
                <p><strong>인증번호는 5분간 유효합니다.</strong></p>
                <p>감사합니다.</p>
            </div>
            """.formatted(verificationCode);
    }

    public String createPasswordResetEmailContent(String resetLink) {
        return """
            <div style="max-width: 600px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif;">
                <h2 style="color: #333;">비밀번호 재설정</h2>
                <p>안녕하세요! DoriRoom입니다.</p>
                <p>아래 링크를 클릭하여 비밀번호를 재설정해주세요.</p>
                <div style="text-align: center; margin: 30px 0;">
                    <a href="%s" style="background-color: #007bff; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px;">
                        비밀번호 재설정하기
                    </a>
                </div>
                <p><strong>링크는 10분간 유효합니다.</strong></p>
                <p>감사합니다.</p>
            </div>
            """.formatted(resetLink);
    }
}

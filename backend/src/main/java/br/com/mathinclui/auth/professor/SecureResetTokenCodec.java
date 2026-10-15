package br.com.mathinclui.auth.professor;

import java.nio.charset.StandardCharsets; import java.security.MessageDigest; import java.security.NoSuchAlgorithmException; import java.security.SecureRandom; import java.util.Base64; import java.util.HexFormat;
public final class SecureResetTokenCodec {
    private final SecureRandom random=new SecureRandom();
    public String generate(){ byte[] bytes=new byte[32]; random.nextBytes(bytes); return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); }
    public String hash(String rawToken){ try { var digest=MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8)); return HexFormat.of().formatHex(digest); } catch(NoSuchAlgorithmException exception){ throw new IllegalStateException("SHA-256 unavailable",exception); } }
}

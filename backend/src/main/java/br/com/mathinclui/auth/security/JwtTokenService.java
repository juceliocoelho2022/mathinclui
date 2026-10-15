package br.com.mathinclui.auth.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class JwtTokenService {
    private final JwtEncoder encoder; private final JwtDecoder decoder; private final Duration accessTtl; private final Duration refreshTtl;
    public JwtTokenService(JwtEncoder encoder, SecretKey secretKey, @Value("${app.security.jwt.access-ttl-seconds:900}") long accessTtlSeconds, @Value("${app.security.jwt.refresh-ttl-seconds:604800}") long refreshTtlSeconds) {
        this.encoder=encoder; var rawDecoder=NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build(); rawDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("mathinclui")); this.decoder=rawDecoder; this.accessTtl=Duration.ofSeconds(accessTtlSeconds); this.refreshTtl=Duration.ofSeconds(refreshTtlSeconds);
    }
    public TokenValue issueAccess(AuthPrincipal principal){ return issue(principal,"access",accessTtl,UUID.randomUUID().toString()); }
    public TokenValue issueRefresh(AuthPrincipal principal){ return issue(principal,"refresh",refreshTtl,UUID.randomUUID().toString()); }
    public RefreshTokenClaims parseRefresh(String token){ var jwt=decoder.decode(token); if(!"refresh".equals(jwt.getClaimAsString("token_type"))) throw new IllegalArgumentException("Token is not a refresh token"); var subjectTypeClaim=jwt.getClaimAsString("subject_type"); var subject=jwt.getSubject(); var jti=jwt.getId(); if(subjectTypeClaim==null||subject==null||jti==null||!subject.contains(":")) throw new IllegalArgumentException("Refresh token claims are incomplete"); var subjectType=SubjectType.valueOf(subjectTypeClaim); var subjectId=Long.parseLong(subject.substring(subject.indexOf(':')+1)); if(!subject.equals(subjectType.name()+":"+subjectId)) throw new IllegalArgumentException("Refresh token subject is inconsistent"); return new RefreshTokenClaims(jti,subjectType,subjectId,jwt.getExpiresAt()); }
    private TokenValue issue(AuthPrincipal principal,String type,Duration ttl,String jti){ var now=Instant.now(); var expiresAt=now.plus(ttl); var claims=JwtClaimsSet.builder().issuer("mathinclui").issuedAt(now).expiresAt(expiresAt).id(jti).subject(principal.subjectType().name()+":"+principal.id()).claim("token_type",type).claim("subject_type",principal.subjectType().name()).claim("role",principal.role()).claim("name",principal.displayName()).claim("login",principal.login()).build(); var header=JwsHeader.with(MacAlgorithm.HS256).build(); var value=encoder.encode(JwtEncoderParameters.from(header,claims)).getTokenValue(); return new TokenValue(value,jti,expiresAt); }
    public record TokenValue(String value,String jti,Instant expiresAt){}
    public record RefreshTokenClaims(String jti,SubjectType subjectType,Long subjectId,Instant expiresAt){}
}

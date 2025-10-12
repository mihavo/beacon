package io.authservice.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import java.io.FileReader;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import lombok.Getter;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class JWTUtility {

  private final PublicKey publicKey;

  private final PrivateKey privateKey;

  @Value("${jwt.expiration-mins}")
  private long expirationMins;

  public JWTUtility(@Value("${jwt.private-key-path}") String privateKeyPath,
      @Value("${jwt.public-key-path}") String publicKeyPath) throws IOException {
    privateKey = readPrivateKey(privateKeyPath);
    publicKey = readPublicKey(publicKeyPath);
  }

  public String generateToken(String subject) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + expirationMins * 60 * 1000);
    return Jwts.builder().subject(subject).issuedAt(now).expiration(expiry)
        .signWith(privateKey, SIG.RS256).compact();
  }

  private PrivateKey readPrivateKey(String filePath) throws IOException {
    try (FileReader fileReader = new FileReader(filePath); PemReader pemReader = new PemReader(
        fileReader)) {
      PemObject pemObject = pemReader.readPemObject();
      return new JcaPEMKeyConverter().getPrivateKey(
          PrivateKeyInfo.getInstance(pemObject.getContent()));
    }
  }

  private PublicKey readPublicKey(String filePath) throws IOException {
    try (FileReader fileReader = new FileReader(filePath); PemReader pemReader = new PemReader(
        fileReader)) {
      PemObject pemObject = pemReader.readPemObject();
      return new JcaPEMKeyConverter().getPublicKey(
          SubjectPublicKeyInfo.getInstance(pemObject.getContent()));
    }
  }
}

package co.com.bookhive.gateway.testdata;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

public final class SessionTokens {

	public static final String PROJECT = "bookhive-511618";
	public static final String ISSUER = "https://session.firebase.google.com/" + PROJECT;
	public static final String KEY_ID = "llave-1";
	public static final String ACCOUNT = "Pw2lnRkb4iYbBvFYELKPLxQ2KjD2";
	public static final Instant NOW = Instant.parse("2026-10-20T20:00:00Z");

	private static final KeyPair KEYS = newKeys();
	private static final KeyPair OTHER_KEYS = newKeys();

	private SessionTokens() {
		super();
	}

	public static RSAPublicKey publicKey() {
		return (RSAPublicKey) KEYS.getPublic();
	}

	public static JWTClaimsSet.Builder validClaims() {
		return new JWTClaimsSet.Builder()
				.issuer(ISSUER)
				.audience(PROJECT)
				.subject(ACCOUNT)
				.issueTime(Date.from(NOW.minusSeconds(60)))
				.expirationTime(Date.from(NOW.plusSeconds(1200)));
	}

	public static String valid() {
		return signed(validClaims().build());
	}

	public static String signed(final JWTClaimsSet claims) {
		return signedWith(claims, KEYS);
	}

	public static String signedByOtherKey(final JWTClaimsSet claims) {
		return signedWith(claims, OTHER_KEYS);
	}

	public static String signedWithSecret(final JWTClaimsSet claims) {
		try {
			final SignedJWT token = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.HS256).keyID(KEY_ID).build(), claims);
			token.sign(new MACSigner("una-clave-compartida-de-al-menos-treinta-y-dos-bytes"));
			return token.serialize();
		} catch (final JOSEException exception) {
			throw new IllegalStateException(exception);
		}
	}

	private static String signedWith(final JWTClaimsSet claims, final KeyPair keys) {
		try {
			final SignedJWT token = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(KEY_ID).build(), claims);
			token.sign(new RSASSASigner(keys.getPrivate()));
			return token.serialize();
		} catch (final JOSEException exception) {
			throw new IllegalStateException(exception);
		}
	}

	private static KeyPair newKeys() {
		try {
			final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048);
			return generator.generateKeyPair();
		} catch (final NoSuchAlgorithmException exception) {
			throw new IllegalStateException(exception);
		}
	}

}

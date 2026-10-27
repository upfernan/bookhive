package co.com.bookhive.access.services.output.ports.entities;

public record VerifiedIdentityEntity(String identity, String email, boolean emailVerified, String sessionToken) {
}

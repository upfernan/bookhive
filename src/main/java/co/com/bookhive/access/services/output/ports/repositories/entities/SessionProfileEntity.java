package co.com.bookhive.access.services.output.ports.repositories.entities;

import java.util.List;

public record SessionProfileEntity(String email, String institution, List<String> roles) {
}

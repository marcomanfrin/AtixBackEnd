package marcomanfrin.softwareops.DTO.users;

public record ChangePasswordRequest(String oldPassword, String newPassword) {}
